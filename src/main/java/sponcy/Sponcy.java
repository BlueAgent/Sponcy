package sponcy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sponcy.common.CommonProxy;
import sponcy.common.helper.LangHelper;
import sponcy.common.helper.SQLiteHelper;

@Mod(
        modid = Sponcy.MOD_ID,
        name = "Spontaneous Collection",
        version = Sponcy.MOD_VERSION,
        acceptedMinecraftVersions = Sponcy.MC_VERSION,
        clientSideOnly = false,
        serverSideOnly = false,
        dependencies = Sponcy.DEPENDENCIES
)
@Mod.EventBusSubscriber
public class Sponcy {
    public static final String MOD_ID = "spontaneouscollection";
    public static final String MOD_VERSION = "99999.999.999";
    public static final String MC_VERSION = "";
    public static final String DEPENDENCIES = "";
    public static final boolean DEV_ENVIRONMENT = MOD_VERSION.equals("99999.999.999");

    @SidedProxy(clientSide = "spontaneouscollection.client.ClientProxy", serverSide = "spontaneouscollection.common.CommonProxy")
    public static CommonProxy proxy;
    public static LangHelper lang = new LangHelper(Sponcy.MOD_ID + ".");
    public static Logger log = LogManager.getLogger(MOD_ID);

    @Instance(MOD_ID)
    public static Sponcy INSTANCE;

    @SubscribeEvent
    public static void resisterRegistries(RegistryEvent.NewRegistry event) {

    }

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

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        proxy.onWorldLoad(event);
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        proxy.onWorldUnload(event);
    }

    @SubscribeEvent
    public static void onWorld(WorldEvent event) {
        //System.out.println("WorldEvent: " + event.getWorld().getClass().getCanonicalName() + " " + event.getClass().getSimpleName());
    }

    @SubscribeEvent
    public static void onWorldSave(WorldEvent.Save event) {
        proxy.onWorldSave(event);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        proxy.onEntityJoinWorld(event);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println(String.format("Pre-Init %s", MOD_ID));
        System.out.println(String.format("Version %s", MOD_VERSION));
        System.out.println("Loaded SQLiteJDBC (by Taro L. Saito) at classpath: " + SQLiteHelper.load());
        Sponcy.log = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println(String.format("Init %s", MOD_ID));
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println(String.format("Post-Init %s", MOD_ID));
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @EventHandler
    public void serverStart(FMLServerStoppingEvent event) {
        proxy.serverStopping(event);
    }

    @EventHandler
    public void serverStop(FMLPostInitializationEvent event) {
        System.out.println(String.format("Post-Init %s", MOD_ID));
        proxy.postInit(event);
    }
}
