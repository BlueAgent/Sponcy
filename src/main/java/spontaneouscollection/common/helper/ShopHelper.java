package spontaneouscollection.common.helper;

import spontaneouscollection.SpontaneousCollection;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Manages connections to the shops database.
 * Creates one connection per thread.
 */
public class ShopHelper implements Closeable {
    public static final String DB_FILE = "shops.db";
    public static final int SEMA_MAX = 5;
    public static final int CLEANUP_COOLDOWN_MILLIS = 10000;
    public static final String[] SQLS_CREATE_TABLES;
    public static final String SQL_CREATE_OR_GET_OWNER_ID = "";

    static {
        ArrayList<String> sql = new ArrayList<>();
        try (
                InputStream is = SQLiteHelper.class.getClassLoader().getResourceAsStream("shops.sql");
        ) {
            for (String s : StreamHelper.readString(is).split(";")) {
                s = s.trim();
                if (s.length() == 0) continue;
                sql.add(s);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shops.sql", e);
        }
        SQLS_CREATE_TABLES = sql.toArray(new String[0]);
    }

    protected boolean closing = false;
    protected Semaphore connectionSema = new Semaphore(SEMA_MAX);
    protected ConcurrentHashMap<Thread, Connection> connections = new ConcurrentHashMap<>();
    protected Thread connectionCleanupThread;

    public ShopHelper() {
        connectionCleanupThread = new Thread(this::connectionCleanupThread);
        connectionCleanupThread.setName("Connection Cleanup Thread");
        connectionCleanupThread.setDaemon(true);
        connectionCleanupThread.start();
    }

    /**
     * Gets a new connection for the current thread.
     * Please close the connection if your thread does not reuse connection.
     *
     * @return the connection
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException {
        Thread t = Thread.currentThread();
        Connection conn = connections.get(t);
        if (conn != null && !conn.isClosed()) return conn;
        connectionSema.acquireUninterruptibly();
        try {
            if (closing) throw new SQLException("Connections closing...");
            conn = SQLiteHelper.connect(SpontaneousCollection.MOD_ID, DB_FILE);
            conn.setAutoCommit(false);
            connections.put(t, conn);
        } finally {
            connectionSema.release();
        }
        return conn;
    }

    private void connectionCleanupThread() {
        try {
            while (!closing) {
                //Prevent creation of new connections temporarily
                connectionSema.acquireUninterruptibly(SEMA_MAX);
                for (Map.Entry<Thread, Connection> entry : connections.entrySet()) {
                    Thread t = entry.getKey();
                    if (t.isAlive()) continue;
                    Connection conn = entry.getValue();
                    try {
                        if (!conn.isClosed()) conn.close();
                    } catch (SQLException e) {
                        new RuntimeException("Failed to close connection while cleaning up stopped thread: " + entry.getKey().getName(), e)
                                .printStackTrace();
                    }
                    connections.remove(t);
                }
                connectionSema.release(SEMA_MAX);
                Thread.sleep(CLEANUP_COOLDOWN_MILLIS);
            }
        } catch (InterruptedException e) {
            //Cleanup Thread Stopped
        }
    }

    /**
     * This will make all further getConnection methods fail, and currents connections will be closed.
     */
    @Override
    public void close() {
        //Acquire all the permits
        connectionSema.acquireUninterruptibly(SEMA_MAX);
        closing = true;
        connectionSema.release(SEMA_MAX);
        //No more new connections should be made now
        for (Map.Entry<Thread, Connection> entry : connections.entrySet()) {
            try {
                entry.getValue().close();
            } catch (SQLException e) {
                new RuntimeException("Failed to close connection for thread: " + entry.getKey().getName(), e).printStackTrace();
            }
            entry.getKey().interrupt();
        }
        if (connectionCleanupThread.isAlive())
            connectionCleanupThread.interrupt();
    }

    public void execute(String sql) throws SQLException {
        SQLiteHelper.execute(getConnection(), sql);
    }

    public int[] executeBatch(String... sqls) throws SQLException {
        Connection conn = getConnection();
        Statement s = conn.createStatement();
        for (String sql : sqls)
            s.addBatch(sql);
        int[] result = s.executeBatch();
        conn.commit();
        return result;
    }

    public void createTables() throws SQLException {
        executeBatch(SQLS_CREATE_TABLES);
    }
}
