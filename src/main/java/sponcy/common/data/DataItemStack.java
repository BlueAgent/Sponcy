package spontaneouscollection.common.data;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Objects;

public class DataItemStack {
    protected final String id;
    protected final ResourceLocation rl;
    protected final int meta;
    protected final NBTTagCompound nbt;

    public DataItemStack(String id, int meta, NBTTagCompound nbt) {
        this.id = id;
        this.meta = meta;
        this.nbt = nbt.copy();
        this.rl = new ResourceLocation(id);
    }

    public static DataItemStack get(ItemStack template) {
        return new DataItemStack(template.getItem().getRegistryName().toString(), template.getItemDamage(), template.getTagCompound());
    }

    public Item getItem() {
        return ForgeRegistries.ITEMS.getValue(rl);
    }

    public ItemStack getStack(int amount) {
        ItemStack stack = new ItemStack(getItem(), amount);
        stack.setTagCompound(nbt.copy());
        return stack;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof DataItemStack)) return false;
        DataItemStack o = (DataItemStack) that;

        if (!id.equals(o.id)) return false;
        if (meta != o.meta) return false;
        if (nbt == null) return o.nbt == null;

        return nbt.equals(o.nbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, meta, nbt);
    }
}
