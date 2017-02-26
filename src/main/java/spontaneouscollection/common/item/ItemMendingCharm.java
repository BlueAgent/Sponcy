package spontaneouscollection.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import spontaneouscollection.common.helper.ExperienceHelper;

public class ItemMendingCharm extends ItemBase {

    public ItemMendingCharm() {
        setMaxStackSize(1);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote) return;
        if (!(entityIn instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entityIn;
        if (player.isSneaking()) {
            ExperienceHelper.addXp(player, 100);
        } else {
            ExperienceHelper.addXp(player, -100);
        }
        //player.addChatMessage(new TextComponentString(String.format("exp=%f,lvl=%d,tot=%d", player.experience, player.experienceLevel, player.experienceTotal)));
    }
}
