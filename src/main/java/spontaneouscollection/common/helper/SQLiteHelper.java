package spontaneouscollection.common.helper;

import net.minecraftforge.common.DimensionManager;
import org.sqlite.JDBC;
import org.sqlite.SQLiteConnection;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public static int[] execute(Connection conn, boolean docommit, String... sqls) throws SQLException {
        boolean commit = conn.getAutoCommit();
        if (docommit) conn.setAutoCommit(false);
        try (Statement s = conn.createStatement()) {
            for (String sql : sqls)
                s.addBatch(sql);
            int[] result = s.executeBatch();
            return result;
        } finally {
            if (docommit) conn.commit();
            conn.setAutoCommit(commit);
        }
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
        boolean commit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        Savepoint save = conn.setSavepoint();
        try {
            T yay = func.run();
            conn.commit();
            return yay;
        } catch (SQLException e) {
            conn.rollback(save);
            throw e;
        } finally {
            conn.setAutoCommit(commit);
        }
    }

    public static void rollbackAndThrowWithCommit(Connection conn, ISQLRunnable func) throws SQLException {
        rollbackAndThrowWithCommit(conn, () -> {
            func.run();
            return false;
        });
    }

    /**
     * False if failed to compare blobs
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean compareBlob(Blob a, Blob b) throws SQLException {
        long len = a.length();
        if (b.length() != len) return false;
        try (InputStream bsa = a.getBinaryStream()) {
            try (InputStream bsb = b.getBinaryStream()) {
                return StreamHelper.compare(bsa, bsb);
            }
        } catch (IOException e) {
            throw new SQLException("Failed to compare blobs", e);
        }
    }

    /**
     * Gets bytes contained in the blob
     * If null, then it destroys
     *
     * @param b
     * @return
     */
    @Nullable
    public static byte[] getBytes(Blob b) throws SQLException {
        if (b == null) return null;
        try (InputStream in = b.getBinaryStream()) {
            return StreamHelper.readBytes(in);
        } catch (IOException e) {
            throw new SQLException("Failed to get bytes from blob", e);
        }
    }

    public interface ISQLFunction<E> {
        E run() throws SQLException;
    }

    public interface ISQLRunnable {
        void run() throws SQLException;
    }
}
