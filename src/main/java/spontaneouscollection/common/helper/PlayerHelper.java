package spontaneouscollection.common.helper;

import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class PlayerHelper {
    public static UUID getUUID(EntityPlayer player) {
        return player.getUUID(player.getGameProfile());
    }
}
