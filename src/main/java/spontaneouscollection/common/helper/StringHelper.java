package spontaneouscollection.common.helper;


import java.util.Locale;
import java.util.regex.Pattern;

public class StringHelper {

    public static final Pattern MATCH_INSERT_UNDERSCORE =
            Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", Pattern.MULTILINE);

    public static String camelCaseToUnderscore(String input)
    {
        return MATCH_INSERT_UNDERSCORE.matcher(input).replaceAll("_").toLowerCase();
    }
}
