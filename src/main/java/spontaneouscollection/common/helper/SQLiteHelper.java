package spontaneouscollection.common.helper;

import net.minecraftforge.common.DimensionManager;
import org.sqlite.JDBC;
import spontaneouscollection.SpontaneousCollection;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteHelper {
    public static final String PREFIX = "jdbc:sqlite:" + SpontaneousCollection.MOD_ID + ":";

    static {
        try {
            Field f = JDBC.class.getDeclaredField("PREFIX");
            f.setAccessible(true);
            ReflectionHelper.makeFinalAccessible(f);
            f.set(null, PREFIX);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects to a database.
     *
     * @param path to database.
     * @return Connection opened.
     */
    public static Connection connect(File path) {
        String url = PREFIX + path.getAbsolutePath();
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database at: " + url, e);
        }
    }

    /**
     * Connects to a database.
     * Saved in: [root folder]/[mod_id]/[database_name].db
     * RuntimeException if fails to connect to database.
     *
     * @param modID        null or empty to save it directly in the world folder.
     * @param databaseName name of the database file.
     * @return Connection
     */
    public static Connection connect(String modID, String databaseName) {
        File saveDirectory = DimensionManager.getCurrentSaveRootDirectory();
        if (modID != null && modID.length() > 0)
            saveDirectory = new File(saveDirectory, modID);
        saveDirectory.mkdirs();
        return connect(new File(saveDirectory, databaseName + ".db"));
    }
}
