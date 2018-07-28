package sponcy.common.sql;

import net.minecraft.entity.player.EntityPlayer;
import sponcy.common.helper.PlayerHelper;
import sponcy.common.helper.SQLiteHelper;
import sponcy.common.helper.ShopHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static spontaneouscollection.common.helper.ShopHelper.TABLE_OWNER;

public class ShopOwner {
    public static final String SQL_INSERT_OR_UPDATE_OWNER =
            "insert or replace into `" + TABLE_OWNER + "` (`uuid`, `name`) values(?, ?)";
    public static final String SQL_SELECT_WHERE_ID =
            "select * from `" + TABLE_OWNER + "` WHERE id=?";
    public static final String SQL_SELECT_WHERE_UUID =
            "select * from `" + TABLE_OWNER + "` WHERE uuid=?";
    public static final String SQL_SELECT_All =
            "select * from `" + TABLE_OWNER + "`";
    public static final String SQL_TRANSFER_MONEY =
            "update `" + TABLE_OWNER + "` set money=case" +
                    "when id=? then money-?" +
                    "when id=? then money+?" +
                    "else money end" +
                    "where id IN (?,?);";
    protected final int id;
    protected final UUID uuid;
    protected final ShopHelper shop;
    protected String name;
    protected long money;

    protected ShopOwner(ShopHelper shop, int id, UUID uuid, String name, long money) throws SQLException {
        this.shop = shop;
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.money = money;
    }

    public static ShopOwner get(ShopHelper shop, final int id) throws SQLException {
        Connection conn = shop.getConnection();
        return SQLiteHelper.rollbackAndThrowWithCommit(conn, () -> {
            PreparedStatement select = conn.prepareStatement(SQL_SELECT_WHERE_ID);
            select.setInt(1, id);
            ResultSet result = select.executeQuery();
            if (!result.next()) throw new SQLException("Expected a result after successfully inserting.");
            if (id != result.getInt("id")) {
                throw new SQLException("ID different from selected?");
            }
            UUID uuid = UUID.fromString(result.getString("uuid"));
            String name = result.getString("name");
            long money = result.getLong("money");
            result.close();
            select.close();
            return new ShopOwner(shop, id, uuid, name, money);
        });
    }

    public static ShopOwner get(final ShopHelper shop, final UUID uuid, final String name) throws SQLException {
        Connection conn = shop.getConnection();
        return SQLiteHelper.rollbackAndThrowWithCommit(conn, () -> {
            int id;
            long money;
            //Update or insert the row
            PreparedStatement insert = conn.prepareStatement(SQL_INSERT_OR_UPDATE_OWNER);
            insert.setString(1, uuid.toString());
            insert.setString(2, name);
            //Get the actual information
            PreparedStatement select = conn.prepareStatement(SQL_SELECT_WHERE_UUID);
            select.setString(1, uuid.toString());
            insert.execute();
            insert.close();
            ResultSet result = select.executeQuery();
            if (!result.next()) throw new SQLException("Expected a result after successfully inserting.");
            id = result.getInt("id");
            if (!uuid.equals(UUID.fromString(result.getString("uuid")))) {
                throw new SQLException("UUID was not updated?");
            }
            if (!name.equals(result.getString("name"))) {
                throw new SQLException("Name was not updated?");
            }
            money = result.getLong("money");
            result.close();
            select.close();
            return new ShopOwner(shop, id, uuid, name, money);
        });
    }

    public static ShopOwner get(ShopHelper shop, EntityPlayer player) throws SQLException {
        UUID uuid = PlayerHelper.getUUID(player);
        String name = player.getName();
        return get(shop, uuid, name);
    }

    public static List<ShopOwner> getAll(ShopHelper shop) throws SQLException {
        Connection conn = shop.getConnection();
        List<ShopOwner> owners = new ArrayList<>();
        Statement s = conn.createStatement();
        ResultSet result = s.executeQuery(SQL_SELECT_All);
        while (result.next()) {
            owners.add(new ShopOwner(
                    shop,
                    result.getInt("id"),
                    UUID.fromString(result.getString("uuid")),
                    result.getString("name"),
                    result.getLong("money")));
        }
        result.close();
        s.close();
        return owners;
    }

    @Override
    public String toString() {
        return "ShopOwner[" + id + "," + uuid + "," + name + "," + money + "]";
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ShopHelper getShop() {
        return shop;
    }

    public String getName() {
        return name;
    }

    public long getMoney() {
        return money;
    }

    public void transferMoney(ShopOwner from, ShopOwner to, long amount) throws SQLException {
        Connection conn = shop.getConnection();
        SQLiteHelper.rollbackAndThrowWithCommit(conn, () -> {
            PreparedStatement ps = conn.prepareStatement(SQL_TRANSFER_MONEY);
            //when id=? then money-?
            ps.setInt(1, from.id);
            ps.setLong(2, amount);
            //when id=? then money+?
            ps.setInt(3, to.id);
            ps.setLong(4, amount);
            //where id IN (?,?)
            ps.setInt(5, from.id);
            ps.setInt(6, from.id);
            //Two rows SHOULD have changed
            if (ps.executeUpdate() != 2) {
                throw new SQLException("Number of updates while transferring money not equal to 2?");
            }
        });
    }
}

