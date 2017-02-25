package spontaneouscollection.client;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import spontaneouscollection.SpontaneousCollection;
import spontaneouscollection.common.CommonProxy;

import java.lang.reflect.Field;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerModels(ModelRegistryEvent event) {
        for (Field f : SpontaneousCollection.Items.class.getDeclaredFields()) {
            try {
                Item item = (Item) f.get(null);
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            } catch (IllegalAccessException e) {

            }
        }
    }
}
