package spontaneouscollection.common.helper;

import net.minecraftforge.common.DimensionManager;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConnection;

import java.io.File;
import java.sql.*;
import java.util.Enumeration;

public class SQLiteHelper {
    static {
        try {
            //Registered drivers of the shaded class
            //This is so other people using the sqlite library don't conflict
            Class.forName(JDBC.class.getName());
            Enumeration<Driver> e = DriverManager.getDrivers();
            while (e.hasMoreElements()) {
                Driver d = e.nextElement();
                if (JDBC.class == d.getClass())
                    DriverManager.deregisterDriver(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String load() {
        return JDBC.class.getName();
    }

    /**
     * Connects to a database.
     *
     * @param path to database.
     * @return Connection opened.
     */
    public static SQLiteConnection connect(String path) throws SQLException {
        return new SQLiteConnection(JDBC.PREFIX + path, path);
    }

    /**
     * Connects to a database.
     *
     * @param path to database.
     * @return Connection opened.
     */
    public static SQLiteConnection connect(File path) throws SQLException {
        return connect(path.getAbsolutePath());
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
    public static SQLiteConnection connect(String modID, String databaseName) throws SQLException {
        File saveDirectory = DimensionManager.getCurrentSaveRootDirectory();
        if (modID != null && modID.length() > 0)
            saveDirectory = new File(saveDirectory, modID);
        saveDirectory.mkdirs();
        return connect(new File(saveDirectory, databaseName));
    }

    public static int[] execute(Connection conn, boolean commit, String... sqls) throws SQLException {
        conn.setAutoCommit(false);
        Statement s = conn.createStatement();
        for (String sql : sqls)
            s.addBatch(sql);
        int[] result = s.executeBatch();
        s.close();
        if (commit) conn.commit();
        return result;
    }

    public static int[] execute(Connection conn, String... sqls) throws SQLException {
        return execute(conn, true, sqls);
    }

    public static <T> T rollbackAndThrow(Connection conn, ISQLFunction<T> func) throws SQLException {
        Savepoint save = conn.setSavepoint();
        try {
            return func.run();
        } catch (SQLException e) {
            conn.rollback(save);
            throw e;
        }
    }

    public static void rollbackAndThrow(Connection conn, ISQLRunnable func) throws SQLException {
        rollbackAndThrow(conn, () -> {
            func.run();
            return false;
        });
    }

    public static <T> T rollbackAndThrowWithCommit(Connection conn, ISQLFunction<T> func) throws SQLException {
        conn.setAutoCommit(false);
        Savepoint save = conn.setSavepoint();
        try {
            T yay = func.run();
            conn.commit();
            return yay;
        } catch (SQLException e) {
            conn.rollback(save);
            throw e;
        }
    }

    public static void rollbackAndThrowWithCommit(Connection conn, ISQLRunnable func) throws SQLException {
        rollbackAndThrowWithCommit(conn, () -> {
            func.run();
            return false;
        });
    }

    public interface ISQLFunction<E> {
        E run() throws SQLException;
    }

    public interface ISQLRunnable {
        void run() throws SQLException;
    }
}
