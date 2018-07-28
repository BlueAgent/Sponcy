package sponcy.common.helper;

import net.minecraft.util.text.TextComponentTranslation;

public class LangHelper {

    public static final LangHelper NO_PREFIX = new LangHelper("");
    public static String EXCEPTION = "general.exception";
    protected final String prefix;

    public LangHelper(String prefix) {
        this.prefix = prefix;
    }

    public LangHelper append(String append) {
        return new LangHelper(prefix + append);
    }

    public String getKey(String appendedResource) {
        return prefix + appendedResource;
    }

    public TextComponentTranslation getTextComponent(String appendedResource, Object... args) {
        return new TextComponentTranslation(prefix + appendedResource, args);
    }

    public TextComponentTranslation getTextComponent(Throwable t) {
        return getTextComponent(EXCEPTION, t.getClass().getSimpleName(), t.getMessage());
    }

}
