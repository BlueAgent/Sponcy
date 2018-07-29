package sponcy.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import sponcy.Sponcy;
import sponcy.common.CommonProxy;
import sponcy.common.registry.ItemRegistry;

import java.lang.reflect.Field;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerModels(ModelRegistryEvent event) {
        for (Field f : ItemRegistry.class.getDeclaredFields()) {
            try {
                Item item = (Item) f.get(null);
                if (item == null) {
                    Sponcy.log.info("Item not registered: " + f.getName());
                    continue;
                }
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Missing items", e);
            }
        }
    }
}
