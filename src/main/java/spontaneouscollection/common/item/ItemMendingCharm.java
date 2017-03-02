package spontaneouscollection.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import spontaneouscollection.common.SCConfig.MendingCharm;
import spontaneouscollection.common.helper.CostHelper;
import spontaneouscollection.common.helper.EnchantHelper;
import spontaneouscollection.common.helper.ExperienceHelper;

import java.util.LinkedList;
import java.util.UUID;


/**
 * Repairs items that have Mending on them using experience
 */
public class ItemMendingCharm extends ItemBase {

    public ItemMendingCharm() {
        setMaxStackSize(1);
    }

    public static double getDurabilityFromXp(double experience) {
        return experience * MendingCharm.durability_per_xp;
    }

    public static double getXpFromDurability(double durability) {
        return durability / MendingCharm.durability_per_xp;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) return;
        if (worldIn.getTotalWorldTime() % MendingCharm.operation_time != 0) return;
        if (!(entityIn instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entityIn;

        //Might have issues with floating point precision
        //Is required for the floating point durability per xp though
        double durabilityToRepair = 0;
        double durabilityMaximum = Math.min(getDurabilityFromXp(player.experienceTotal), MendingCharm.max_durability);
        if (durabilityMaximum <= 0) return;
        //Find all items to repair
        LinkedList<ItemStack> queue = new LinkedList<>();
        for (Slot slot : player.inventoryContainer.inventorySlots) {
            //Stop searching after going over the limit
            //Unless debug is on, in which case it checks all the items to find the actual durabilityToRepair
            if (!MendingCharm.debug && durabilityToRepair >= durabilityMaximum)
                break;
            //Exclude the crafting slots
            if (slot.slotNumber < 5) continue;
            ItemStack itemStack = slot.getStack();
            if (itemStack == null) continue; //TODO: 1.11 update null check
            //Make sure it is using metadata for durability
            if (!itemStack.isItemDamaged()) continue;
            //Check if it has Mending
            if (MendingCharm.requires_mending && EnchantHelper.getEnchantmentLevel(itemStack, Enchantments.MENDING) == 0)
                continue;
            //Enqueue
            queue.addLast(itemStack);
            durabilityToRepair += itemStack.getItemDamage();
        }
        if (durabilityToRepair >= durabilityMaximum)
            durabilityToRepair = durabilityMaximum;
        //Exit if there's nothing to repair
        if (durabilityToRepair <= 0) return;
        double xpRequired = getXpFromDurability(durabilityToRepair);
        //Round based on efficiency config
        xpRequired = MendingCharm.repair_efficiently ? Math.floor(xpRequired) : Math.ceil(xpRequired);
        //Efficiency may cause it to round down to zero here
        if (xpRequired <= 0) return;
        CostHelper costHelper = new CostHelper(getDurabilityFromXp(xpRequired));
        while (!queue.isEmpty() && costHelper.getToMax() > 0) {
            //Dequeue
            ItemStack itemStack = queue.removeFirst();
            int toRepair = (int) Math.min(itemStack.getItemDamage(), costHelper.getToMax());
            if (costHelper.add(toRepair)) {
                itemStack.setItemDamage(itemStack.getItemDamage() - toRepair);
            }
        }
        ExperienceHelper.addXp(player, (int) -xpRequired);
        if (MendingCharm.debug)
            player.addChatComponentMessage(new TextComponentString(
                    String.format("%dxp => %d/%d", (int) xpRequired, (int) costHelper.getTotal(), (int) durabilityToRepair)
            ));
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        System.out.println(UUID.randomUUID());
        return EnumActionResult.SUCCESS;
    }
}
