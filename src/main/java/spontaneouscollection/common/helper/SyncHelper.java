package spontaneouscollection.common.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SyncHelper {
    public static void addScheduledTask(boolean isRemote, Runnable task) {
        if (isRemote) {
            Minecraft.getMinecraft().addScheduledTask(task);
        } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(task);
        }
    }

    public static void addScheduledTask(World world, Runnable task) {
        addScheduledTask(world.isRemote, task);
    }

    /**
     * Run now if it is on the main server or client thread, otherwise runs on next tick on the appropriate side.
     *
     * @param isRemote
     * @param task
     */
    public static void addScheduledTaskOrRunNow(boolean isRemote, Runnable task) {
        if (isRemote && Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            task.run();
        } else if (!isRemote && FMLCommonHandler.instance().getMinecraftServerInstance().isCallingFromMinecraftThread()) {
            task.run();
        } else {
            addScheduledTask(isRemote, task);
        }
    }

    /**
     * Run now if it is on the main server or client thread, otherwise runs on next tick.
     *
     * @param world
     * @param task
     */
    public static void addScheduledTaskOrRunNow(World world, Runnable task) {
        addScheduledTaskOrRunNow(world.isRemote, task);
    }
}
