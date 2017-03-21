package spontaneouscollection.common;

import net.minecraftforge.common.config.Config;
import spontaneouscollection.SpontaneousCollection;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = SpontaneousCollection.MOD_ID)
public class SCConfig {
    public static MendingCharm mending_charm;
    public static Shops shops;

    public static class MendingCharm {
        @Comment("Should the default recipe be added?")
        public static boolean recipe = true;

        @Comment("Number of ticks per operation.")
        @RangeInt(min = 1, max = 60 * 20)
        public static int operation_time = 5 * 20;

        @Comment("Total maximum durability per operation.\nMake sure this is greater than 'durability_per_xp' if 'repair_efficiently = true' or it will never repair anything.")
        @RangeInt(min = 1, max = 1000000)
        public static int max_durability = 10000;

        @Comment("Amount of durability per experience point.\nVanilla Mending repairs 2 durability per experience point.")
        @RangeDouble(min = 0.1, max = 10000)
        public static double durability_per_xp = 2;

        @Comment("Only repair items with Mending enchantment placed on them.")
        public static boolean requires_mending = true;

        @Comment("Only repair items if it uses the xp to the fullest extent.\nThis leaves items un-repaired if they are not damaged enough to use a full point of xp.")
        public static boolean repair_efficiently = true;

        @Comment("Send debug chat message showing how much xp was used and how much was repaired out of the total missing.")
        public static boolean debug = false;
    }

    public static class Shops {
        @Comment("Disable raw sql statement execution (for OPs only when enabled).")
        public static boolean disable_sc_sql = false;

        @Comment("Offloads work from the main thread where supported, but takes longer to build up a query cache, so the total cpu use will be higher. Operations in game will be more delayed (instead of responding in the same tick, it will respond in the next tick after it completes. Some operations will always run on the thread pool however.")
        public static boolean threads_enabled = true;

        @Comment("Max size of the Thread Pool")
        @RangeInt(min = 1, max = 10)
        public static int threads_count = 5;

        //TODO: Implement
        @Comment("Maximum amount of a unique item that can be stored inside the database per owner. (Not enforced by database)")
        @RangeInt(min = 64, max = 1048576)
        public static int item_max_amount = 1024;

        //TODO: Implement
        @Comment("Maximum unique items that can be stored inside the database per owner. (Not enforced by database)")
        @RangeInt(min = 1, max = 1024)
        public static int item_max_types = 64;
    }
}
