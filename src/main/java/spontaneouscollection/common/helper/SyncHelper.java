package spontaneouscollection.common.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SyncHelper {
    public static void addScheduledTask(boolean isRemote, Runnable task) {
        //world.getMinecraftServer().addScheduledTask(task);
        if (isRemote) {
            Minecraft.getMinecraft().addScheduledTask(task);
        } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(task);
        }
    }

    public static void addScheduledTask(World world, Runnable task) {
        addScheduledTask(world.isRemote, task);
    }
}
