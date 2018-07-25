package spontaneouscollection.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class RecipeShapedEnchantables extends ShapedRecipes {

    protected final boolean disenchant;

    /**
     * @see RecipeShapedEnchantables(String, int, int, NonNullList<Ingredient>, ItemStack, boolean)
     */
    public RecipeShapedEnchantables(String group, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result) {
        this(group, width, height, ingredients, result, true);
    }

    /**
     * Same as shaped recipes, use enchanted items to specify enchantments, these will be wildcard (any item accepted).
     * Encanted books suggested for consistency.
     *
     * @param width of the recipe
     * @param height of the recipe
     * @param ingredients List holding inputs[column][row]
     * @param result of the recipe
     * @param disenchant Input items should be disenchanted instead of consumed
     */
    public RecipeShapedEnchantables(String group, int width, int height, NonNullList<Ingredient> ingredients, ItemStack result, boolean disenchant) {
        super(group, width, height, ingredients, result);
        this.disenchant = disenchant;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        if (!super.matches(inv, worldIn)) return false;
        // Adapted from ShapedRecipes.java
        for (int col = 0; col <= 3 - this.recipeWidth; ++col) {
            for (int row = 0; row <= 3 - this.recipeHeight; ++row) {
                if (this.checkEnchants(inv, col, row, true)) {
                    return true;
                }
                if (this.checkEnchants(inv, col, row, false)) {
                    return true;
                }
            }
        }
        return true;
    }

    protected boolean checkEnchants(InventoryCrafting inv, int invCol, int invRow, boolean flip) {
        // Adapted from ShapedRecipes.java
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int recipeCol = i - invCol;
                int recipeRow = j - invRow;
                Ingredient required = null;

                if (recipeCol >= 0 && recipeRow >= 0 && recipeCol < this.recipeWidth && recipeRow < this.recipeHeight) {
                    if (flip) {
                        required = this.recipeItems.get(this.recipeWidth - recipeCol - 1 + recipeRow * this.recipeWidth);
                    } else {
                        required = this.recipeItems.get(recipeCol + recipeRow * this.recipeWidth);
                    }
                }

                ItemStack current = inv.getStackInRowAndColumn(i, j);

                if (current.isEmpty() || required != null) {
                    if (current.isEmpty() && required != null || !current.isEmpty() && required == null) {
                        return false;
                    }
                    // Check that enchants are sufficient
                    if(required != null) {
                        //TODO: FIX THIS...
//                        Map<Enchantment, Integer> requiredEnchants = EnchantmentHelper.getEnchantments(required);
//                        Map<Enchantment, Integer> currentEnchants = EnchantmentHelper.getEnchantments(current);
//
//                        for(Enchantment ench : requiredEnchants.keySet()) {
//                            if(!currentEnchants.containsKey(ench)) return false;
//                            if(currentEnchants.get(ench) < requiredEnchants.get(ench)) return false;
//                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        if (!disenchant) return super.getRemainingItems(inv);
        NonNullList<ItemStack> remaining = super.getRemainingItems(inv);

        //TODO: Implement items being returned disenchanted
        for (int i = 0; i < remaining.size(); ++i)
        {
            ItemStack itemstack = inv.getStackInSlot(i);
            remaining.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }

        return remaining;
    }
}
