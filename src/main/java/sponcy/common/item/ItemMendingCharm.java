package sponcy.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import sponcy.Sponcy;
import sponcy.common.SponcyConfig.MendingCharm;
import sponcy.common.helper.EnchantHelper;
import sponcy.common.helper.ExperienceHelper;
import sponcy.common.helper.ItemHelper;
import sponcy.common.helper.NBTHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.ToDoubleFunction;


/**
 * Repairs items that have Mending on them using experience
 */
public class ItemMendingCharm extends ItemBase {

    public static final String TAG_ACTIVE = "active_till";
    public static final int TICKS_ACTIVE = 20;

    public ItemMendingCharm() {
        setMaxStackSize(1);
        addPropertyOverride(new ResourceLocation(Sponcy.MOD_ID, "active"), (stack, world, entity) -> isActive(stack, world, entity) ? 1 : 0);
    }

    private boolean isActive(ItemStack stack, World world, EntityLivingBase clientPlayer) {
        if (world == null) {
            if (clientPlayer == null) return false;
            world = clientPlayer.world;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(TAG_ACTIVE)) return false;
        return tag.getLong(TAG_ACTIVE) > world.getTotalWorldTime();
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) return;
        if (!(entityIn instanceof EntityPlayer)) return;
        NBTHelper tag = ItemHelper.getOrCreateTagHelper(stack);
        // Repair
        if (worldIn.getTotalWorldTime() % MendingCharm.operation_time == 0) {
            if (doRepairs((EntityPlayer) entityIn, tag))
                tag.setLong(TAG_ACTIVE, worldIn.getTotalWorldTime() + TICKS_ACTIVE);

        }
        // Remove active status
        if (tag.getInternal().hasKey(TAG_ACTIVE) && tag.getLong(TAG_ACTIVE, 0) < worldIn.getTotalWorldTime()) {
            tag.getInternal().removeTag(TAG_ACTIVE);
        }
    }

    public boolean doRepairs(EntityPlayer player, NBTHelper tag) {
        //Find all items to repair
        int totalDamage = 0;
        ArrayList<ItemStack> itemsToRepair = new ArrayList<>(player.inventoryContainer.inventorySlots.size());
        for (Slot slot : player.inventoryContainer.inventorySlots) {
            // Exclude the crafting slots
            if (slot.slotNumber < 5) continue;
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmpty()) continue;
            // Make sure it is using metadata for durability
            if (!itemStack.isItemDamaged()) continue;
            // Filter out getXpRepairRatio <= 0 durability per xp (unrepairable)
            float xpRepairRatio = itemStack.getItem().getXpRepairRatio(itemStack);
            if (xpRepairRatio <= 0) continue;
            // Check if it has Mending
            if (MendingCharm.requires_mending && EnchantHelper.getEnchantmentLevel(itemStack, Enchantments.MENDING) == 0)
                continue;
            // Instantly repair when getXpRepairRatio == Float.INFINITY but don't queue up (Trigger animation though)
            if (xpRepairRatio == Float.POSITIVE_INFINITY) {
                itemStack.setItemDamage(0);
                continue;
            }

            itemsToRepair.add(itemStack);
            totalDamage += itemStack.getItemDamage();
        }

        int durabilityMaximum = Math.min(MendingCharm.max_durability, totalDamage);
        if (durabilityMaximum < 1) return false;
        double startingExperience = ExperienceHelper.getXp(player);
        double experienceMaximum = Math.min(MendingCharm.max_experience, startingExperience);
        if (experienceMaximum <= 0) return false;

        // Remove anything that can't be repaired ignoring ordering
        double repairRatioMinimum = MendingCharm.durability_per_xp / experienceMaximum;
        itemsToRepair.removeIf(
                stack -> stack.getItem().getXpRepairRatio(stack) < repairRatioMinimum
        );

        if (itemsToRepair.isEmpty()) return false;

        // Sort items by getXpRepairRatio (durability per exp point, higher values first)
        // TODO: Consider randomly repairing instead of sorting?
        itemsToRepair.sort(Comparator
                .<ItemStack>comparingDouble(stack ->
                    stack.getItem().getXpRepairRatio(stack)
                )
                .thenComparingDouble(stack ->
                        (double) stack.getItemDamage() / stack.getMaxDamage() // TODO: Maybe multiply by getXpRepairRatio?
                )
                .reversed()
        );

        //Repair items
        int totalDurabilityRepaired = 0;
        double totalExperienceUsed = 0;
        for (ItemStack itemStack : itemsToRepair) {
            // Work out the durability per xp, need to multiply by 0.5 since default for getXpRepairRatio is 2
            double duraPerExp = MendingCharm.durability_per_xp * 0.5 * itemStack.getItem().getXpRepairRatio(itemStack);
            // Get durability that can be repaired
            int toRepair = Math.min(itemStack.getItemDamage(), durabilityMaximum - totalDurabilityRepaired);
            // Get the amount of experience it would require to repair all that durability, or that we could consume
            double experienceCost = Math.min(toRepair / duraPerExp, experienceMaximum - totalExperienceUsed);
            // Get the actual amount of durability we can repair
            int actuallyRepaired = (int) Math.min(toRepair, experienceCost * duraPerExp);
            // Get the actual amount of experience it consumed
            double actualExperience = actuallyRepaired / duraPerExp;

            //Repair the item
            itemStack.setItemDamage(itemStack.getItemDamage() - actuallyRepaired);

            // Update the totals
            totalDurabilityRepaired += actuallyRepaired;
            totalExperienceUsed += actualExperience;
        }

        //Apply the experience cost of repairs
        double experienceDelta = ExperienceHelper.addXp(player, -totalExperienceUsed, false);

        if (MendingCharm.debug) {
            double error = -experienceDelta - totalExperienceUsed;
            char sign = error > 0 ? '+' : error < 0 ? '-' : '=';
            player.sendMessage(new TextComponentString(
                    String.format("%f-%f=%f (%s%f) => %d/%d",
                            startingExperience,
                            totalExperienceUsed,
                            ExperienceHelper.getXp(player),
                            sign,
                            error,
                            totalDurabilityRepaired,
                            totalDamage
                    )
            ));
        }

        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && newStack.getItem() == this && oldStack.getItem() == newStack.getItem())
            return false;
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
