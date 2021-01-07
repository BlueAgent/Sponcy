package sponcy.common.sql;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import sponcy.common.data.DataItemStack;
import sponcy.common.helper.NBTHelper;
import sponcy.common.helper.SQLiteHelper;
import sponcy.common.helper.ShopHelper;

import javax.annotation.Nonnull;
import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static sponcy.common.helper.ShopHelper.TABLE_ITEM;

public class ShopItem {
    public static final String SQL_INSERT_OR_UPDATE_ITEM =
            "insert or replace into " + TABLE_ITEM + " (`owner`, `item_id`, `meta`, `nbt`) values(?, ?, ?, ?)";
    public static final String SQL_SELECT_WHERE_ID =
            "select * from `" + TABLE_ITEM + "` WHERE id=?";
    public static final String SQL_SELECT_WHERE_OWNER =
            "select * from `" + TABLE_ITEM + "` WHERE owner=?";
    public static final String SQL_SELECT_WHERE_OWNER_ITEM =
            "select * from `" + TABLE_ITEM + "` WHERE owner=? AND item_id=? AND meta=? AND nbt=?";
    public static final String SQL_SELECT_All =
            "select * from `" + TABLE_ITEM + "`";
    protected final int id;
    protected final int owner;
    protected final ShopHelper shop;
    protected final String item_id;
    protected final int meta;
    protected final NBTTagCompound nbt;
    protected int amount;

    protected ShopItem(ShopHelper shop, int id, int owner, String item_id, int meta, NBTTagCompound nbt, int amount) throws SQLException {
        this.shop = shop;
        this.id = id;
        this.owner = owner;
        this.item_id = item_id;
        this.meta = meta;
        this.nbt = nbt == null ? null : nbt.copy();
        this.amount = 0;
    }

    private static ShopItem get(ShopHelper shop, ResultSet result) throws SQLException {
        return new ShopItem(shop,
                result.getInt("id"),
                result.getInt("owner"),
                result.getString("item_id"),
                result.getInt("meta"),
                NBTHelper.fromBlob(result.getBlob("nbt")),
                result.getInt("amount")
        );
    }

    public static ShopItem get(ShopHelper shop, final int id) throws SQLException {
        Connection conn = shop.getConnection();
        return SQLiteHelper.rollbackAndThrowWithCommit(conn, () -> {
            try (PreparedStatement select = conn.prepareStatement(SQL_SELECT_WHERE_ID)) {
                select.setInt(1, id);
                select.execute();
                try (ResultSet result = select.executeQuery()) {
                    if (!result.next()) throw new SQLException(String.format("Item with id=%d could not be found", id));
                    assert id == result.getInt("id");
                    return get(shop, result);
                }
            }
        });
    }

    public static ShopItem get(ShopHelper shop, final int owner, @Nonnull final String item_id, final int meta, final NBTTagCompound nbt) throws SQLException {
        Connection conn = shop.getConnection();
        final Blob nbtblob;
        try {
            nbtblob = nbt == null ? null : new SerialBlob(NBTHelper.toByteArrayCompressed(nbt));
        } catch (IOException e) {
            throw new SQLException("Failed get NBT bytes", e);
        }
        return SQLiteHelper.rollbackAndThrowWithCommit(conn, () -> {
            int id;
            int amount;
            //Update or insert the row
            try (PreparedStatement insert = conn.prepareStatement(SQL_INSERT_OR_UPDATE_ITEM)) {
                insert.setInt(1, owner);
                insert.setString(2, item_id);
                insert.setInt(3, meta);
                //if (nbtblob == null) {
                //    insert.setNull(4, Types.BLOB);
                //} else {
                insert.setBlob(4, nbtblob);
                //}
                insert.execute();
            }
            //Get the actual information
            try (PreparedStatement select = conn.prepareStatement(SQL_SELECT_WHERE_OWNER_ITEM)) {
                select.setInt(1, owner);
                select.setString(2, item_id);
                select.setInt(3, meta);
                select.setBlob(4, nbtblob);
                try (ResultSet result = select.executeQuery()) {
                    if (!result.next()) throw new SQLException("Expected a result after successfully inserting.");
                    id = result.getInt("id");
                    amount = result.getInt("amount");
                    if (result.getInt("owner") != owner) {
                        throw new SQLException("Owner was not updated?");
                    }
                    if (!result.getString("item_id").equals(item_id)) {
                        throw new SQLException("Name was not updated?");
                    }
                    if (result.getInt("meta") != meta) {
                        throw new SQLException("Meta was not updated?");
                    }
                    if (!SQLiteHelper.compareBlob(result.getBlob("nbt"), nbtblob)) {
                        throw new SQLException("NBT was not updated?");
                    }
                    return new ShopItem(shop, id, owner, item_id, meta, nbt, amount);
                }
            }
        });
    }

    public static List<ShopItem> getAll(ShopHelper shop) throws SQLException {
        Connection conn = shop.getConnection();
        List<ShopItem> items = new ArrayList<>();
        try (Statement s = conn.createStatement()) {
            try (ResultSet result = s.executeQuery(SQL_SELECT_All)) {
                while (result.next()) {
                    Blob b = result.getBlob("nbt");
                    NBTTagCompound nbt = null;
                    if (b != null) {
                        try (InputStream stream = b.getBinaryStream()) {
                            nbt = CompressedStreamTools.readCompressed(stream);
                        } catch (IOException e) {
                            throw new SQLException("Failed to read blob", e);
                        }
                    }
                    items.add(new ShopItem(
                            shop,
                            result.getInt("id"),
                            result.getInt("owner"),
                            result.getString("item_id"),
                            result.getInt("meta"),
                            nbt,
                            result.getInt("amount")));
                }
                return items;
            }
        }
    }

    public int getId() {
        return id;
    }

    public ShopOwner getOwner() throws SQLException {
        return shop.getOwner(getOwnerId());
    }

    public int getOwnerId() {
        return owner;
    }

    public ShopHelper getShop() {
        return shop;
    }

    public String getItemId() {
        return item_id;
    }

    public int getMeta() {
        return meta;
    }

    public NBTTagCompound getNbt() {
        return nbt.copy();
    }

    public int getAmount() {
        return amount;
    }

    public DataItemStack getStack() {
        return new DataItemStack(this.getItemId(), this.getMeta(), this.getNbt());
    }


}
