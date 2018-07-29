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

    public static double getDurabilityFromXp(double experience) {
        return experience * MendingCharm.durability_per_xp;
    }

    public static double getXpFromDurability(double durability) {
        return durability / MendingCharm.durability_per_xp;
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
        double durabilityRemainder = tag.getDouble(TAG_REMAINING_DURABILITY, 0);
        int durabilityMaximum = (int) Math.min(getDurabilityFromXp(player.experienceTotal) + durabilityRemainder, MendingCharm.max_durability);
        //Only attempt repairs if can repair at least 1 durability
        if (durabilityMaximum < 1) return false;

        //Find all items to repair
        int totalDamage = 0;
        LinkedList<ItemStack> queue = new LinkedList<>();
        for (Slot slot : player.inventoryContainer.inventorySlots) {
            //Stop searching after going over the limit
            //Unless debug is on, in which case it checks all the items to find the actual durabilityToRepair
            if (!MendingCharm.debug && totalDamage >= durabilityMaximum)
                break;
            //Exclude the crafting slots
            if (slot.slotNumber < 5) continue;
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmpty()) continue;
            //Make sure it is using metadata for durability
            if (!itemStack.isItemDamaged()) continue;
            //Check if it has Mending
            if (MendingCharm.requires_mending && EnchantHelper.getEnchantmentLevel(itemStack, Enchantments.MENDING) == 0)
                continue;
            //Enqueue
            queue.addLast(itemStack);
            totalDamage += itemStack.getItemDamage();
        }

        //Exit if there's nothing to repair
        if (totalDamage <= 0) return false;

        //Repair items
        CostHelper repairLimiter = new CostHelper(Math.min(totalDamage, durabilityMaximum));
        while (!queue.isEmpty() && repairLimiter.getToMax() > 0) {
            //Dequeue
            ItemStack itemStack = queue.removeFirst();
            int toRepair = (int) Math.min(itemStack.getItemDamage(), repairLimiter.getToMax());
            if (repairLimiter.add(toRepair)) {
                itemStack.setItemDamage(itemStack.getItemDamage() - toRepair);
            }
        }

        //Apply the cost of repairs
        double durabilityRepaired = repairLimiter.getToMin();
        durabilityRepaired -= durabilityRemainder;
        int expCost = (int) Math.ceil(getXpFromDurability(durabilityRepaired));
        ExperienceHelper.addXp(player, -expCost);
        durabilityRepaired -= getDurabilityFromXp(expCost);

        //Save the excess durability for next time repairs are needed
        tag.setDouble(TAG_REMAINING_DURABILITY, durabilityRepaired < 0 ? -durabilityRepaired : 0);

        if (MendingCharm.debug)
            player.sendMessage(new TextComponentString(
                    String.format("%d => %d/%d", expCost, (int) repairLimiter.getToMin(), totalDamage)
            ));

        return true;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return SponcyConfig.MendingCharm.debug && ItemHelper.getCommonTagHelper(stack).getDouble(TAG_REMAINING_DURABILITY, 0) > 0.001;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (MendingCharm.durability_per_xp >= 1.0) {
            return 1.0d - (ItemHelper.getCommonTagHelper(stack).getDouble(TAG_REMAINING_DURABILITY, 0) / MendingCharm.durability_per_xp);
        } else {
            return 1.0d - (ItemHelper.getCommonTagHelper(stack).getDouble(TAG_REMAINING_DURABILITY, 0));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (!SponcyConfig.MendingCharm.debug) return;
        if (MendingCharm.durability_per_xp >= 1.0) {
            tooltip.add(String.format("%f/%f", ItemHelper.getCommonTagHelper(stack).getDouble(TAG_REMAINING_DURABILITY, 0), MendingCharm.durability_per_xp));
        } else {
            tooltip.add(String.format("%f%%", ItemHelper.getCommonTagHelper(stack).getDouble(TAG_REMAINING_DURABILITY, 0) * 100));
        }
        tooltip.add(String.valueOf(isActive(stack)));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (!slotChanged && newStack.getItem() == this && oldStack.getItem() == newStack.getItem())
            return false;
        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }
}
