package spontaneouscollection.common;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = SCConfig.MODID, version = SCConfig.VERSION)
public class SpontaneousCollection {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        System.out.println("Hello World");
        System.out.printf("Mod ID: %s%n", SCConfig.MODID);
        System.out.printf("Version: %s%n", SCConfig.VERSION);
    }
}
