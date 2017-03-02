package spontaneouscollection.common.helper;

import net.minecraft.util.text.TextComponentTranslation;

public class LangHelper {

    protected final String prefix;

    public LangHelper(String prefix) {
        this.prefix = prefix;
    }

    public LangHelper append(String append) {
        return new LangHelper(prefix + append);
    }

    public TextComponentTranslation getTextComponent(String appendedResource, Object... args) {
        return new TextComponentTranslation(prefix + appendedResource, args);
    }

    public String getKey(String appendedResource) {
        return prefix + appendedResource;
    }
}
