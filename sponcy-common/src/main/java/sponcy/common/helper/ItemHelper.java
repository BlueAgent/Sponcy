package sponcy.common.helper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemHelper {
    public static final String TAG_COMMON = "sponcy";

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    public static NBTHelper getOrCreateTagHelper(ItemStack stack) {
        return NBTHelper.wrap(getOrCreateTag(stack));
    }

    public static NBTTagCompound getCommonTag(ItemStack stack) {
        return stack.getOrCreateSubCompound(TAG_COMMON);
    }

    public static NBTHelper getCommonTagHelper(ItemStack stack) {
        return NBTHelper.wrap(getCommonTag(stack));
    }
}
