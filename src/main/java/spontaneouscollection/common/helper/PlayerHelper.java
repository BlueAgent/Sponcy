package spontaneouscollection.common.helper;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;

import java.util.UUID;

public class PlayerHelper {
    public static UUID getUUID(EntityPlayer player) {
        return player.getUUID(player.getGameProfile());
    }

    public static boolean isFake(EntityPlayerMP suspect) {
        if (suspect == null) return true;
        if (suspect.getClass() != EntityPlayerMP.class) return true;
        if (suspect.connection == null) return true;
        NetworkManager manager = suspect.connection.netManager;
        if (manager == null || manager.getClass() != NetworkManager.class) return true;
        return false;
    }

    public static boolean isFake(ICommandSender sender) {
        if (sender == null || !(sender instanceof EntityPlayerMP)) return true;
        return isFake((EntityPlayerMP) sender);
    }

    public static boolean isFake(Entity entity) {
        if (entity == null || !(entity instanceof EntityPlayerMP)) return true;
        return isFake((EntityPlayerMP) entity);
    }
}
