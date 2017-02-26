package spontaneouscollection.common.item;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import spontaneouscollection.common.helper.CostHelper;
import spontaneouscollection.common.helper.ExperienceHelper;

/**
 * Repairs items that have Mending on them using experience
 */
public class ItemMendingCharm extends ItemBase {

    public ItemMendingCharm() {
        setMaxStackSize(1);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) return;
        //TODO: Configurable frequency
        if (worldIn.getTotalWorldTime() % 100 == 0) return;
        if (!(entityIn instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entityIn;

        //Use doubles for fractional experience cost
        //Vanilla mending actually gives you a free repair when there's only 1 durability missing
        CostHelper costHelp = new CostHelper(player.experienceTotal);
        int repairRemaining = player.experienceTotal * 2; //TODO: Configurable cost, overflow...?
        for (Slot slot : player.inventoryContainer.inventorySlots) {
            ItemStack itemStack = slot.getStack();
            if (itemStack == null) continue;
            if (!itemStack.isItemDamaged()) continue;
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, itemStack) == 0) continue;
            int toRepair = Math.min(repairRemaining, itemStack.getItemDamage());
            if (toRepair <= 0) continue;
            if (costHelp.add(toRepair / 2.0)) { //TODO: Configurable cost
                itemStack.setItemDamage(itemStack.getItemDamage() - toRepair);
                repairRemaining = (int) costHelp.getToMax() * 2; //TODO: Configurable cost
                if (repairRemaining <= 0) break;
            }
        }

        int totalCost = (int) Math.ceil(costHelp.getTotal());
        if (totalCost > 0) {
            ExperienceHelper.addXp(player, -totalCost);
        }
    }
}
