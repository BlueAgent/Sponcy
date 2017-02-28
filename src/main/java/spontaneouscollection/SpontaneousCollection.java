package spontaneouscollection;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import spontaneouscollection.common.CommonProxy;

@Mod(
        modid = SpontaneousCollection.MOD_ID,
        name = "Spontaneous Collection",
        version = SpontaneousCollection.MOD_VERSION,
        acceptedMinecraftVersions = SpontaneousCollection.MC_VERSION,
        clientSideOnly = false,
        serverSideOnly = false,
        dependencies = SpontaneousCollection.DEPENDENCIES
)
@Mod.EventBusSubscriber
public class SpontaneousCollection {
    public static final String MOD_ID = "spontaneouscollection";
    public static final String MOD_VERSION = "99999.999.999";
    public static final String MC_VERSION = "";
    public static final String DEPENDENCIES = "";

    @SidedProxy(clientSide = "spontaneouscollection.client.ClientProxy", serverSide = "spontaneouscollection.server.ServerProxy")
    public static CommonProxy proxy;

    @Instance(MOD_ID)
    public static SpontaneousCollection INSTANCE;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        System.out.println("Register blocks...");
        proxy.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        System.out.println("Register items...");
        proxy.registerItems(event);
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) throws Exception {
        System.out.println("Register models...");
        proxy.registerModels(event);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println(String.format("Mod ID: %s", MOD_ID));
        System.out.println(String.format("Version: %s", MOD_VERSION));
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
