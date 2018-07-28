package spontaneouscollection.common.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import spontaneouscollection.common.helper.EnchantHelper;
import spontaneouscollection.common.registry.ItemRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class IngredientEnchanted extends Ingredient {
    final Map<Enchantment, Integer> enchantments;

    protected IngredientEnchanted(Map<Enchantment, Integer> enchantments) {
        super(EnchantHelper.createEnchantedItem(ItemRegistry.enchanted_item, enchantments));
        this.enchantments = ImmutableMap.copyOf(enchantments);
    }

    @Override
    public boolean apply(@Nullable ItemStack input) {
        return input != null && EnchantHelper.checkEnchantments(EnchantmentHelper.getEnchantments(input), this.enchantments);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    public static class Factory implements IIngredientFactory {
        @Nonnull
        @Override
        public Ingredient parse(JsonContext context, JsonObject json) {
            Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
            JsonObject enchantmentsObj = json.getAsJsonObject("enchantments");
            if(enchantmentsObj == null)
                throw new JsonParseException("Missing enchantments.");
            for (Map.Entry<String, JsonElement> entry : enchantmentsObj.entrySet()) {
                Enchantment enchantment = Enchantment.REGISTRY.getObject(new ResourceLocation(context.appendModId(entry.getKey())));
                int level = (int) entry.getValue().getAsShort();
                enchantments.put(enchantment, level);
            }
            return new IngredientEnchanted(enchantments);
        }
    }
}
