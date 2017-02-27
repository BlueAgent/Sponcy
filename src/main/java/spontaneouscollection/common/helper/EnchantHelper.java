package spontaneouscollection.common.helper;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import java.util.Map;

public class EnchantHelper {
    /**
     * Adds the enchantment to the item.
     * Takes the max of the levels if it already exists on the item.
     * Warning: Does not make a copy.
     *
     * @param itemStack   to enchant.
     * @param enchantment to add.
     * @param level       of enchantment.
     * @return the input item stack with the enchantment added.
     */
    public static ItemStack addEnchantment(ItemStack itemStack, Enchantment enchantment, int level) {
        //TODO: 1.11 update null item stack
        if (itemStack == null) return itemStack;
        if (level <= 0) return itemStack;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
        if (!enchantments.containsKey(enchantment)) {
            level = Math.max(enchantments.get(enchantment), level);
        }
        enchantments.put(enchantment, level);
        EnchantmentHelper.setEnchantments(enchantments, itemStack);
        return itemStack;
    }

    /**
     * Removes levels of an enchantment from item.
     * Use 0 or negative levels to.
     * Warning: Does not make a copy.
     *
     * @param itemStack   to remove from.
     * @param enchantment to remove.
     * @param levels      to remove, use 0 or negative to remove completely.
     * @return the input item stack with the enchantment removed.
     */
    public static ItemStack removeEnchantment(ItemStack itemStack, Enchantment enchantment, int levels) {
        //TODO: 1.11 update null item stack
        if (itemStack == null) return itemStack;
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
        if (!enchantments.containsKey(enchantment)) return itemStack;
        int level = Math.max(enchantments.remove(enchantment) - levels, 0);
        if (levels <= 0) level = 0;
        if (level > 0) {
            enchantments.put(enchantment, level);
        }
        EnchantmentHelper.setEnchantments(enchantments, itemStack);
        if (enchantments.size() == 0 && itemStack.getItem() == Items.ENCHANTED_BOOK) {
            return new ItemStack(Items.BOOK);
        }
        return itemStack;
    }

    /**
     * Safe way to check for enchantments on books and items.
     * Works for books and items.
     *
     * @param itemStack   to check.
     * @param enchantment to get.
     * @return level of the enchantment.
     */
    public static int getEnchantmentLevel(ItemStack itemStack, Enchantment enchantment) {
        //TODO: 1.11 update null item stack
        if (itemStack == null) return 0;
        if (itemStack.getItem() == null) return 0;
        //From Minecraft's EnchantmentHelper
        NBTTagList nbttaglist = itemStack.getItem() == Items.ENCHANTED_BOOK ? Items.ENCHANTED_BOOK.getEnchantments(itemStack) : itemStack.getEnchantmentTagList();
        if (nbttaglist == null) return 0;

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            if (enchantment != Enchantment.getEnchantmentByID(nbttaglist.getCompoundTagAt(i).getShort("id")))
                continue;
            return nbttaglist.getCompoundTagAt(i).getShort("lvl");
        }
        return 0;
    }
}
