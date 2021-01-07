package sponcy.common.item;

import net.minecraft.item.Item;
import sponcy.Sponcy;
import sponcy.common.helper.StringHelper;

public abstract class ItemBase extends Item {

    public static final String ITEM_PREFIX = "Item";
    public static final int ITEM_PREFIX_LENGTH = ITEM_PREFIX.length();

    public ItemBase() {
        String name = this.getClass().getSimpleName();
        if (name.startsWith(ITEM_PREFIX)) {
            name = name.substring(ITEM_PREFIX_LENGTH);
        } else {
            throw new RuntimeException(String.format("Sub-class name does not start with 'Item': %s", name));
        }
        name = StringHelper.camelCaseToUnderscore(name);
        this.setRegistryName(Sponcy.MOD_ID, name);
        this.setTranslationKey(name);
    }
}
