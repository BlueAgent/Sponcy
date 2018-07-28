package sponcy.common.helper;

import net.minecraft.entity.player.EntityPlayer;
import sponcy.SpontaneousCollection;
import sponcy.common.SCConfig;
import sponcy.common.data.DataItemStack;
import sponcy.common.sql.ShopItem;
import sponcy.common.sql.ShopOwner;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Manages connections to the shops database.
 * Creates one connection per thread.
 */
public class ShopHelper implements Closeable, ThreadFactory {
    public static final String DB_FILE = "shops.db";
    public static final int SEMA_MAX = 5;
    public static final int CLEANUP_COOLDOWN_MILLIS = 10000;
    public static final String[] SQLS_CREATE_TABLES;

    public static final String TABLE_PREFIX = "Shop";
    public static final String TABLE_OWNER = TABLE_PREFIX + "Owner";
    public static final String TABLE_ITEM = TABLE_PREFIX + "Item";
    public static final String TABLE_SHOP = TABLE_PREFIX + "Shop";
    public static final String TABLE_OFFER = TABLE_PREFIX + "Offer";
    public static final String TABLE_OFFER_ITEMS = TABLE_PREFIX + "OfferItems";

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

    protected final ExecutorService executorService;
    protected final ThreadGroup threadGroup = new ThreadGroup("SCShops Workers");
    protected boolean closing = false;
    protected Semaphore connectionSema = new Semaphore(SEMA_MAX);
    protected ConcurrentHashMap<Thread, Connection> connections = new ConcurrentHashMap<>();
    protected Thread connectionCleanupThread;

    protected Map<Integer, ShopOwner> owners_id = new HashMap<>();
    protected Map<UUID, ShopOwner> owners_uuid = new HashMap<>();
    protected Map<Integer, ShopItem> items_id = new HashMap<>();
    protected Map<DataItemStack, ShopItem> items_stack = new HashMap<>();

    public ShopHelper() {
        connectionCleanupThread = new Thread(this::connectionCleanupThread);
        connectionCleanupThread.setName("[SCShops] Connection Cleanup Thread");
        connectionCleanupThread.setDaemon(true);
        connectionCleanupThread.start();
        executorService = Executors.newFixedThreadPool(SCConfig.Shops.threads_count, this);
    }

    /**
     * CAREFUL USING THIS METHOD
     * Behaviour can either be:
     * - Running synchronously on the current thread
     * - Running asynchronously on the executor service pool
     * Depends on the config
     *
     * @param r to execute
     */
    public void run(Runnable r) {
        if (SCConfig.Shops.threads_enabled)
            executorService.submit(r);
        else
            r.run();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(threadGroup, r);
        t.setName("[SCShops] Worker Thread");
        return t;
    }

    /**
     * The executor service.
     * Please respect the SCConfig.Shops.threads_enabled config.
     *
     * @return the executor service, always exists.
     */
    @Nonnull
    public ExecutorService executor() {
        return executorService;
    }

    /**
     * Gets a new connection for the current thread.
     * Please close the connection if your thread does not reuse connection.
     * Threads will be periodically cleaned up if no longer running.
     * Highly suggesting using the executor service {@link #executor()}
     * or use the run method {@link #run(Runnable)}
     *
     * @return the connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
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
                int cleaned = 0;
                //Prevent creation of new connections temporarily
                connectionSema.acquireUninterruptibly(SEMA_MAX);
                for (Map.Entry<Thread, Connection> entry : connections.entrySet()) {
                    Thread t = entry.getKey();
                    if (t.isAlive()) continue;
                    //Skip threads in the group
                    if (t.getThreadGroup() == threadGroup) continue;
                    Connection conn = entry.getValue();
                    try {
                        if (!conn.isClosed()) conn.close();
                    } catch (SQLException e) {
                        new RuntimeException("Failed to close connection while cleaning up stopped thread: " + entry.getKey().getName(), e)
                                .printStackTrace();
                    }
                    connections.remove(t);
                    cleaned++;
                }
                connectionSema.release(SEMA_MAX);
                if (cleaned > 0) System.out.println("Freed " + cleaned + " threads.");
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
        if (connectionCleanupThread.isAlive())
            connectionCleanupThread.interrupt();
        //No more new connections should be made now
        for (Map.Entry<Thread, Connection> entry : connections.entrySet()) {
            try {
                entry.getValue().close();
            } catch (SQLException e) {
                new RuntimeException("Failed to close connection for thread: " + entry.getKey().getName(), e).printStackTrace();
            }
            entry.getKey().interrupt();
        }
        executorService.shutdown();
    }

    public int[] execute(boolean commit, String... sqls) throws SQLException {
        return SQLiteHelper.execute(getConnection(), commit, sqls);
    }

    public int[] execute(String... sqls) throws SQLException {
        return execute(true, sqls);
    }

    public void initShops() throws SQLException {
        execute(SQLS_CREATE_TABLES);
        //ShopOwner
        for (ShopOwner owner : ShopOwner.getAll(this)) {
            insertOwner(owner);
        }
        //ShopItem
        for (ShopItem item : ShopItem.getAll(this)) {

        }
    }

    protected void insertOwner(ShopOwner owner) {
        owners_id.put(owner.getId(), owner);
        owners_uuid.put(owner.getUuid(), owner);
    }

    protected void insertItem(ShopItem item) {
        items_id.put(item.getId(), item);
        items_stack.put(item.getStack(), item);
    }

    //ShopOwner methods
    public ShopOwner getOwner(int id) throws SQLException {
        ShopOwner owner = owners_id.get(id);
        if (owner != null) return owner;
        owner = ShopOwner.get(this, id);
        owners_id.put(owner.getId(), owner);
        owners_uuid.put(owner.getUuid(), owner);
        return owner;
    }

    public ShopOwner getOwner(EntityPlayer player) throws SQLException {
        ShopOwner owner = owners_uuid.get(PlayerHelper.getUUID(player));
        if (owner != null) return owner;
        owner = ShopOwner.get(this, player);
        owners_id.put(owner.getId(), owner);
        owners_uuid.put(owner.getUuid(), owner);
        return owner;
    }

    public ShopOwner getOwner(UUID uuid, String name) throws SQLException {
        ShopOwner owner = owners_uuid.get(uuid);
        if (owner != null) return owner;
        owner = ShopOwner.get(this, uuid, name);
        owners_id.put(owner.getId(), owner);
        owners_uuid.put(owner.getUuid(), owner);
        return owner;
    }

    public ShopOwner getOwner(UUID uuid) throws SQLException {
        ShopOwner owner = owners_uuid.get(uuid);
        if (owner != null) return owner;
        throw new SQLException("0 uuid=" + uuid);
    }

    public ShopOwner getOwner(String name) throws SQLException {
        ShopOwner owner = owners_uuid.values().stream().filter((o) -> o.getName().equals(name)).findFirst().orElse(null);
        if (owner != null) return owner;
        throw new SQLException("0 name=: " + name);
    }

    public ShopOwner getOwnerSimilar(String name) throws SQLException {
        ShopOwner owner = owners_uuid.values().stream().filter((o) -> o.getName().equals(name)).findFirst().orElse(null);
        if (owner != null) return owner;

        String search = StringHelper.MATCH_NON_WORD.matcher(name.toLowerCase()).replaceAll("");
        owner = owners_uuid.values().stream().filter(
                (o) -> StringHelper.MATCH_NON_WORD.matcher(o.getName().toLowerCase()).replaceAll("").equals(search)
        ).findFirst().orElse(null);
        if (owner != null) return owner;

        owner = owners_uuid.values().stream().filter(
                (o) -> StringHelper.MATCH_NON_WORD.matcher(o.getName().toLowerCase()).replaceAll("").contains(search)
        ).findFirst().orElse(null);
        if (owner != null) return owner;

        throw new SQLException("0 name~=" + name);
    }

    //ShopItem Methods
    public ShopItem getItem(int id) throws SQLException {
        ShopItem item = items_id.get(id);
        if (item != null) return item;
        item = ShopItem.get(this, id);
        owners_id.put(item.getId(), item.getOwner());
        owners_uuid.put(item.getOwner().getUuid(), item.getOwner());
        return item;
    }
}
