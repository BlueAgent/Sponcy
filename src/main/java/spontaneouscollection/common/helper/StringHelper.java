package spontaneouscollection.common.helper;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class StringHelper {

    public static final Pattern MATCH_INSERT_UNDERSCORE =
            Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", Pattern.MULTILINE);
    public static final Pattern MATCH_NON_WORD =
            Pattern.compile("\\W", Pattern.CASE_INSENSITIVE);

    public static String camelCaseToUnderscore(String input) {
        return MATCH_INSERT_UNDERSCORE.matcher(input).replaceAll("_").toLowerCase();
    }

    public static String stringify(ResultSet r) throws SQLException {
        StringBuilder build = new StringBuilder();
        ResultSetMetaData meta = r.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (i != 1) build.append(',');
            build.append(meta.getColumnName(i));
            build.append('=');
            build.append(r.getString(i));
        }
        return "[" + build.toString() + "]";
    }
}
