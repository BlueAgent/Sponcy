package sponcy.common;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import sponcy.Sponcy;

@EventBusSubscriber(modid = Sponcy.MOD_ID)
public class CommonEvents {
    public static final String OLD_MOD_ID = "spontaneouscollection";

    /**
     * For converting from the old mod id "spontaneouscollection"
     *
     * @param event Missing mapping data for items
     */
    @SubscribeEvent
    public static void onMissingItemMappingsForOldModID(RegistryEvent.MissingMappings<Item> event) {
        Sponcy.log.info("Handling missing " + event.getName().getResourcePath() + " from old resource domain: " + OLD_MOD_ID);
        event.getAllMappings().stream()
                .filter(m -> m.key.getResourceDomain().equals(OLD_MOD_ID))
                .forEach(m -> {
                    ResourceLocation newKey = new ResourceLocation(Sponcy.MOD_ID, m.key.getResourcePath());
                    Item target = ForgeRegistries.ITEMS.getValue(newKey);
                    if (target != null) {
                        Sponcy.log.info(m.key + " -> " + newKey);
                        m.remap(target);
                    } else {
                        Sponcy.log.warn("Failed to map " + m.key + ". " + newKey + " does not exist.");
                    }
                });
    }

    /**
     * Sync Configuration
     *
     * @param event Configuration changed
     */
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!event.getModID().equals(Sponcy.MOD_ID)) return;
        ConfigManager.sync(Sponcy.MOD_ID, Config.Type.INSTANCE);
    }
}
