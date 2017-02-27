package spontaneouscollection.common.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import spontaneouscollection.common.SCConfig;
import spontaneouscollection.common.SCConfig.MendingCharm;
import spontaneouscollection.common.helper.CostHelper;
import spontaneouscollection.common.helper.ExperienceHelper;


/**
 * Repairs items that have Mending on them using experience
 */
public class ItemMendingCharm extends ItemBase {

    public ItemMendingCharm() {
        setMaxStackSize(1);
    }

    public static double getDurabilityFromXp(double experience) {
        return experience * SCConfig.MendingCharm.durability_per_xp;
    }

    public static double getXpFromDurability(double durability) {
        return durability / SCConfig.MendingCharm.durability_per_xp;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) return;
        if (worldIn.getTotalWorldTime() % SCConfig.MendingCharm.operation_time != 0) return;
        if (!(entityIn instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entityIn;

        //Use doubles for fractional experience cost
        //Vanilla mending actually gives you a free repair when there's only 1 durability missing
        double maxDurabilityRepaired = Math.min(getDurabilityFromXp(player.experienceTotal), MendingCharm.max_durability);
        CostHelper costHelp = new CostHelper(getXpFromDurability(maxDurabilityRepaired));
        int repairRemaining = (int) getDurabilityFromXp(costHelp.getToMax()); //Should be equal to maxDurabilityRepaired
        if (repairRemaining <= 0) return;
        for (Slot slot : player.inventoryContainer.inventorySlots) {
            ItemStack itemStack = slot.getStack();
            if (itemStack == null) continue;
            if (!itemStack.isItemDamaged()) continue;
            if (SCConfig.MendingCharm.requires_mending && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, itemStack) == 0)
                continue;
            int toRepair = Math.min(repairRemaining, itemStack.getItemDamage());
            if (toRepair <= 0) continue;
            if (costHelp.add(getXpFromDurability(toRepair))) {
                itemStack.setItemDamage(itemStack.getItemDamage() - toRepair);
                repairRemaining = (int) getDurabilityFromXp(costHelp.getToMax());
                if (repairRemaining <= 0) break;
            }
        }

        int totalCost = (int) Math.ceil(costHelp.getTotal());
        if (totalCost > 0) {
            ExperienceHelper.addXp(player, -totalCost);
        }
    }
}
