package sponcy.common;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.event.*;
import sponcy.SpontaneousCollection;
import sponcy.common.command.Commands;
import sponcy.common.helper.ShopHelper;
import sponcy.common.item.ItemDevWand;
import sponcy.common.item.ItemEnchantedItem;
import sponcy.common.item.ItemMendingCharm;
import sponcy.common.item.ItemShopManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommonProxy {

    public ShopHelper shops = null;

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        List<Block> reg = new ArrayList<>();
        event.getRegistry().registerAll(reg.toArray(new Block[0]));
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        List<Item> reg = new ArrayList<>();
        reg.add(new ItemDevWand());
        reg.add(new ItemEnchantedItem());
        reg.add(new ItemMendingCharm());
        reg.add(new ItemShopManager());
        event.getRegistry().registerAll(reg.toArray(new Item[0]));
    }

    public void registerModels(ModelRegistryEvent event) {

    }

    public void preInit(FMLPreInitializationEvent event) {

    }

    public void init(FMLInitializationEvent event) {
        //TODO: FIX mending charm recipe
//        if (SCConfig.MendingCharm.recipe)
//            GameRegistry.addRecipe(new RecipeMendingCharm(
//                    Items.GOLD_INGOT,
//                    Items.GOLD_INGOT, Items.EMERALD, Items.GOLD_INGOT,
//                    Items.GOLD_INGOT
//            ));
    }

    public void postInit(FMLPostInitializationEvent event) {

    }

    public void serverStarting(FMLServerStartingEvent event) {
        Commands.getCommands(Commands.class).forEach(event::registerServerCommand);
        System.out.println("commands registered");

        shops = new ShopHelper();
        try {
            shops.initShops();
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

    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntity();
        try {
            shops.getOwner(player);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load ShopOwner: " + player.getName(), e);
        }
    }
}
