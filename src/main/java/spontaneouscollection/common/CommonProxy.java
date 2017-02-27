package spontaneouscollection.common;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import spontaneouscollection.common.item.ItemMendingCharm;
import spontaneouscollection.common.recipe.RecipeMendingCharm;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonProxy {

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        List<Block> reg = new ArrayList<>();
        event.getRegistry().registerAll(reg.toArray(new Block[0]));
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        List<Item> reg = new ArrayList<>();
        reg.add(new ItemMendingCharm());
        event.getRegistry().registerAll(reg.toArray(new Item[0]));
    }

    public abstract void registerModels(ModelRegistryEvent event);

    public void preInit(FMLPreInitializationEvent event) {

    }

    public void init(FMLInitializationEvent event) {
        if (SCConfig.MendingCharm.recipe)
            GameRegistry.addRecipe(new RecipeMendingCharm(
                    Items.GOLD_INGOT,
                    Items.GOLD_INGOT, Items.EMERALD, Items.GOLD_INGOT,
                    Items.GOLD_INGOT
            ));
    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}
