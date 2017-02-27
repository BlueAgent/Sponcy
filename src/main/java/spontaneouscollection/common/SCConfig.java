package spontaneouscollection.common;

import net.minecraftforge.common.config.Config;
import spontaneouscollection.SpontaneousCollection;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = SpontaneousCollection.MODID)
public class SCConfig {
    public static MendingCharm mending_charm;

    public static class MendingCharm {
        @Comment("Should the default recipe be added?")
        public static boolean recipe = true;

        @Comment("Number of ticks per operation.")
        @RangeInt(min = 1, max = 200)
        public static int operation_time = 20;

        @Comment("Total maximum durability per operation.")
        @RangeInt(min = 1, max = 100000)
        public static int max_durability = 10000;

        @Comment("Amount of durability per experience point. Mending repairs 2 durability per experience point.")
        @RangeDouble(min = 0.1, max = 10000)
        public static double durability_per_xp = 2;

        @Comment("Only repair items with Mending enchantment placed on.")
        public static boolean requires_mending = true;

        //TODO: Implement efficient repair
        @Comment("[Not Implemented] Only repair item if it uses the xp to the fullest extent. This can leave items un repaired if they are not damaged enough.")
        public static boolean repair_efficiently = false;
    }
}
