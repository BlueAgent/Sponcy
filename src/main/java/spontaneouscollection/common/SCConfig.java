package spontaneouscollection.common;

import net.minecraftforge.common.config.Config;
import spontaneouscollection.SpontaneousCollection;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = SpontaneousCollection.MODID)
public class SCConfig {

    @Comment("This test_general config should be inside the general category")
    @RangeInt(min=1, max=200)
    public static int test_general = 100;

    @Comment("Comment on test_category does not work")
    public static TestCategory test_category;
    public static class TestCategory {
        @Comment("This test_inside_category config should be inside the TestCategory category")
        public static String test_inside_category = "Default String";
    }
}
