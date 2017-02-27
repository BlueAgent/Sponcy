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
        modid = SpontaneousCollection.MODID,
        name = "Spontaneous Collection",
        version = SpontaneousCollection.VERSION,
        clientSideOnly = false,
        serverSideOnly = false,
        dependencies = "required-after:Forge@[12.18.3.2185,)"
)
@Mod.EventBusSubscriber
public class SpontaneousCollection {
    public static final String MODID = "spontaneouscollection";
    public static final String VERSION = "0.0.1";

    @SidedProxy(clientSide = "spontaneouscollection.client.ClientProxy", serverSide = "spontaneouscollection.server.ServerProxy")
    public static CommonProxy proxy;

    @Instance(MODID)
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
        System.out.println(String.format("Mod ID: %s", MODID));
        System.out.println(String.format("Version: %s", VERSION));
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
