package sponcy.common.capabilities;

import sponcy.common.capabilities.expfrac.ExperienceFractionalHandler;

public class SponcyCapabilities {
    public static void registerCapabilities() {
        ExperienceFractionalHandler.register();
    }
}
