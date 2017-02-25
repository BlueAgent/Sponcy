package spontaneouscollection;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.SideOnly;
import spontaneouscollection.common.CommonProxy;
import spontaneouscollection.common.item.ItemMendingCharm;

import java.lang.reflect.Field;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

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
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "spontaneouscollection.client.ClientProxy", serverSide = "spontaneouscollection.common.CommonProxy")
    public static CommonProxy proxy;

    @Instance(MODID)
    public static SpontaneousCollection INSTANCE;

    @ObjectHolder(MODID)
    public static class Blocks
    {

    }

    @ObjectHolder(MODID)
    public static class Items
    {
        public static final Item mending_charm = null;
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        System.out.println("Hello World!!!");
        System.out.println(String.format("Mod ID: %s", MODID));
        System.out.println(String.format("Version: %s", VERSION));
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        System.out.println("Register blocks...");
        //Soon...
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        System.out.println("Register items...");
        event.getRegistry().registerAll(
            new ItemMendingCharm().setRegistryName(MODID, "mending_charm").setUnlocalizedName("mending_charm").setCreativeTab(CreativeTabs.TOOLS)
        );
    }

    @SubscribeEvent
    @SideOnly(CLIENT)
    public static void registerModels(ModelRegistryEvent event) throws Exception
    {
        for (Field f : Items.class.getDeclaredFields())
        {
            Item item = (Item)f.get(null);
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
