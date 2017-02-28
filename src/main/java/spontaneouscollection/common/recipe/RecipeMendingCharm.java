package spontaneouscollection.common.recipe;

import net.minecraft.init.Enchantments;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import spontaneouscollection.common.helper.EnchantHelper;
import spontaneouscollection.common.registry.ItemRegistry;

import javax.annotation.Nullable;

/**
 * Matches 4 items enchanted with mending in the corners and 5 items in a plus shape.
 * Removes 1 level of Mending from the items.
 */
public class RecipeMendingCharm implements IRecipe {
    private static final int[] SLOTS_MENDING = new int[]{0, 2, 6, 8};
    private static final int[] SLOTS_ITEMS = new int[]{1, 3, 4, 5, 7};
    private final Item[] ITEMS;

    public RecipeMendingCharm(Item top, Item left, Item centre, Item right, Item down) {
        ITEMS = new Item[]{top, left, centre, right, down};
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        if (inv.getSizeInventory() != 9) return false;

        //Plus shape
        for (int i = 0; i < SLOTS_ITEMS.length; i++) {
            ItemStack itemStack = inv.getStackInSlot(SLOTS_ITEMS[i]);
            //TODO: 1.11 update
            if (itemStack == null) return false;
            Item item = itemStack.getItem();
            if (item == null) return false;
            if (item != ITEMS[i]) return false;
        }

        //Corners
        for (int slot : SLOTS_MENDING) {
            ItemStack itemStack = inv.getStackInSlot(slot);
            if (EnchantHelper.getEnchantmentLevel(itemStack, Enchantments.MENDING) <= 0)
                return false;
        }

        return true;
    }

    @Nullable
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return new ItemStack(ItemRegistry.mending_charm);
    }

    @Override
    public int getRecipeSize() {
        return 9;
    }

    @Nullable
    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(ItemRegistry.mending_charm);
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        //TODO: 1.11 update
        ItemStack[] remaining = new ItemStack[9];

        //Corners
        for (int slot : SLOTS_MENDING) {
            //Should actually need to check this? Since the slots shouldn't be null
            ItemStack itemStack = inv.getStackInSlot(slot);
            //TODO: 1.11 update
            if (itemStack == null) continue;
            remaining[slot] = EnchantHelper.removeEnchantment(itemStack.copy(), Enchantments.MENDING, 1);
        }
        return remaining;
    }
}