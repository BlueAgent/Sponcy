package spontaneouscollection.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import spontaneouscollection.common.item.ItemMendingCharm;

public abstract class CommonProxy {

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
        );
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemMendingCharm()
        );
    }

    public abstract void registerModels(ModelRegistryEvent event);

    public void preInit(FMLPreInitializationEvent event) {
        //TODO: Add configuration
    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }
}
