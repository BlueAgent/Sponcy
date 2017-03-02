package spontaneouscollection.common.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * This is a very naughty class
 */
public class ReflectionHelper {
    private static final Field modifiersField;

    static {
        Field temp = null;
        try {
            temp = Field.class.getDeclaredField("modifiers");
            temp.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        modifiersField = temp;
    }

    public static void makeFinalAccessible(Field f) throws IllegalAccessException {
        modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
    }
}
