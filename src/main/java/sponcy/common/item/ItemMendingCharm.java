package sponcy.common.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import sponcy.Sponcy;
import sponcy.common.SponcyConfig;
import sponcy.common.SponcyConfig.MendingCharm;
import sponcy.common.helper.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * Repairs items that have Mending on them using experience
 */
public class ItemMendingCharm extends ItemBase {

    public static final String TAG_REMAINING_DURABILITY = "rem";
    public static final String TAG_ACTIVE = "active";
    public static final int TICKS_ACTIVE = 20;

    public ItemMendingCharm() {
        setMaxStackSize(1);
        addPropertyOverride(new ResourceLocation(Sponcy.MOD_ID, "active"), (stack, world, entity) -> isActive(stack) ? 1 : 0);
    }

    private boolean isActive(ItemStack stack) {
        return ItemHelper.getCommonTagHelper(stack).getInteger(TAG_ACTIVE, 0) > 0;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) return;
        if (!(entityIn instanceof EntityPlayer)) return;
        NBTHelper tag = ItemHelper.getCommonTagHelper(stack);
        boolean didRepair = false;
        if (worldIn.getTotalWorldTime() % MendingCharm.operation_time == 0) {
            didRepair = doRepairs((EntityPlayer) entityIn, tag);
        }
        tag.setInteger(TAG_ACTIVE, Math.max(0, didRepair ? TICKS_ACTIVE : tag.getInteger(TAG_ACTIVE, 0) - 1));
     }

    public boolean doRepairs(EntityPlayer player, NBTHelper tag) {
        //Find all items to repair
        int totalDamage = 0;
        ArrayList<ItemStack> itemsToRepair = new ArrayList<>(player.inventoryContainer.inventorySlots.size());
        for (Slot slot : player.inventoryContainer.inventorySlots) {
            //Exclude the crafting slots
            if (slot.slotNumber < 5) continue;
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmpty()) continue;
            //Make sure it is using metadata for durability
            if (!itemStack.isItemDamaged()) continue;
            //TODO: Filter out getXpRepairRatio <= 0 durability per xp (unrepairable)
            //Check if it has Mending
            if (MendingCharm.requires_mending && EnchantHelper.getEnchantmentLevel(itemStack, Enchantments.MENDING) == 0)
                continue;
            //TODO: Instantly repair getXpRepairRatio == Float.INFINITY but don't queue up
            //Enqueue
            itemsToRepair.add(itemStack);
            totalDamage += itemStack.getItemDamage();
        }

        int durabilityMaximum = Math.min(MendingCharm.max_durability, totalDamage);
        if (durabilityMaximum < 1) return false;
        double startingExperience = ExperienceHelper.getXp(player);
        double experienceMaximum = Math.min(MendingCharm.max_experience, startingExperience);
        if (experienceMaximum <= 0) return false;

        //TODO: Sort items by getXpRepairRatio (durability per exp point, higher values first) (in 1.14+?)
        itemsToRepair.sort((a, b) -> 0);

        if(itemsToRepair.isEmpty()) return false;

        //Repair items
        int totalDurabilityRepaired = 0;
        double totalExperienceUsed = 0;
        for(ItemStack itemStack : itemsToRepair) {
            // TODO: Replace 2 with getXpRepairRatio later
            double duraPerExp = MendingCharm.durability_per_xp * 0.5 * 2;
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

        if (MendingCharm.debug)
            player.sendMessage(new TextComponentString(
                    String.format("%f-%f=%f (%f) => %d/%d",
                            startingExperience,
                            totalExperienceUsed,
                            ExperienceHelper.getXp(player),
                            -experienceDelta - totalExperienceUsed,
                            totalDurabilityRepaired,
                            totalDamage
                    )
            ));

        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && newStack.getItem() == this && oldStack.getItem() == newStack.getItem())
            return false;
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
