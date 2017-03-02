package spontaneouscollection.common;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.registry.GameRegistry;
import spontaneouscollection.SpontaneousCollection;
import spontaneouscollection.common.command.Commands;
import spontaneouscollection.common.helper.ShopHelper;
import spontaneouscollection.common.item.ItemMendingCharm;
import spontaneouscollection.common.recipe.RecipeMendingCharm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class CommonProxy {

    public ShopHelper shops = null;

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

    public void serverStarting(FMLServerStartingEvent event) {
        Commands.getCommands(Commands.class).forEach(event::registerServerCommand);
        System.out.println("commands registered");

        shops = new ShopHelper();
        try {
            shops.createTables();
            System.out.println("shops initialised");
        } catch (SQLException e) {
            RuntimeException re = new RuntimeException("Failed to create shops database", e);
            if (SpontaneousCollection.DEV_ENVIRONMENT)
                re.printStackTrace();
            else
                throw re;
        }
    }

    public void serverStopping(FMLServerStoppingEvent event) {
        System.out.println("shops closing all connections...");
        shops.close();
        System.out.println("shops closed");
    }

    public void onWorldLoad(WorldEvent.Load event) {

    }

    public void onWorldUnload(WorldEvent.Unload event) {

    }

    public void onWorldSave(WorldEvent.Save event) {

    }
}
