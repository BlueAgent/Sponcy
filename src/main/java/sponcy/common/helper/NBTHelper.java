package sponcy.common.helper;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.nbt.*;
import net.minecraft.util.ReportedException;
import net.minecraftforge.common.util.Constants;
import sponcy.Sponcy;
import sponcy.common.SponcyConfig;

import javax.annotation.Nonnull;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NBTHelper {
    // From NBTBase.createNewByType(int)
    public enum NBTTagType {
        END(0),
        BYTE(1),
        SHORT(2),
        INT(3),
        LONG(4),
        FLOAT(5),
        DOUBLE(6),
        BYTE_ARRAY(7),
        STRING(8),
        LIST(9),
        COMPOUND(10),
        INT_ARRAY(11),
        LONG_ARRAY(12);

        static final Map<Integer, NBTTagType> map = EnumSet.allOf(NBTTagType.class).stream()
                .collect(Collectors.toMap(NBTTagType::getId, Function.identity()));
        final int id;

        NBTTagType(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static String describe(int id) {
            return (map.containsKey(id) ? map.get(id).toString() : "Unknown") + "(" + id + ")";
        }
    }

    private final NBTTagCompound tag;

    private NBTHelper(NBTTagCompound tag) {
        this.tag = tag;
    }

    public NBTTagCompound getInternal() {
        return tag;
    }

    private boolean checkKey(String key, NBTTagType type) {
        return checkKey(key, type.getId());
    }

    private boolean checkKey(String key, int type) {
        if(tag.hasKey(key, type)) return true;
        // Has key, but of the wrong type
        if(tag.hasKey(key)) {
            RuntimeException ex = new RuntimeException("Expected " + NBTTagType.describe(type) + " but got " + NBTTagType.describe(tag.getTagId(key)) + " for key " + key);
            if(SponcyConfig.General.default_invalid_nbt) {
                // Log it but don't crash
                Sponcy.log.error(ex);
            } else {
                // CRASH!
                throw new ReportedException(createCrashReport(tag, key, type, ex));
            }
        }
        return false;
    }

    // STATIC METHODS

    public static NBTHelper wrap(NBTTagCompound tag) {
        return new NBTHelper(tag);
    }

    public static byte[] toByteArray(@Nonnull NBTTagCompound tag) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            try (DataOutputStream dout = new DataOutputStream(out)) {
                CompressedStreamTools.write(tag, dout);
                return out.toByteArray();
            }
        }
    }

    public static byte[] toByteArrayCompressed(@Nonnull NBTTagCompound tag) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CompressedStreamTools.writeCompressed(tag, out);
            return out.toByteArray();
        }
    }

    public static NBTTagCompound fromByteArray(@Nonnull byte[] input) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(input)) {
            try (DataInputStream din = new DataInputStream(in)) {
                return CompressedStreamTools.read(din);
            }
        }
    }

    public static NBTTagCompound fromByteArrayCompressed(@Nonnull byte[] input) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(input)) {
            return CompressedStreamTools.readCompressed(in);
        }
    }

    public static NBTTagCompound fromBlob(Blob blob) throws SQLException {
        try {
            return NBTHelper.fromByteArrayCompressed(blob.getBytes(1, (int) blob.length()));
        } catch (IOException e) {
            throw new SQLException("Failed converting Blob back into NBT", e);
        }
    }

    /**
     * From NBTTagCompound.java createCrashReport
     */
    public static CrashReport createCrashReport(final NBTTagCompound tag, final String key, final int expectedType, RuntimeException ex)
    {
        CrashReport crashreport = CrashReport.makeCrashReport(ex, "Reading NBT data");
        CrashReportCategory crashreportcategory = crashreport.makeCategoryDepth("Corrupt NBT tag", 1);
        crashreportcategory.addDetail("Tag type found", () -> NBTTagType.describe(tag.getTagId(key)));
        crashreportcategory.addDetail("Tag type expected", () -> NBTTagType.describe(expectedType));
        crashreportcategory.addCrashSection("Tag name", key);
        return crashreport;
    }

    // Mirror of used NBTTagCompound methods

    public boolean getBoolean(String key, boolean def) {
        return getByte(key, (byte)(def ? 1 : 0)) != 0;
    }

    public NBTHelper setBoolean(String key, boolean val) {
        tag.setByte(key,(byte)(val ? 1 : 0));
        return this;
    }

    public byte getByte(String key, byte def) {
        if(!checkKey(key, NBTTagType.BYTE))
            tag.setByte(key, def);
        return tag.getByte(key);
    }

    public NBTHelper setByte(String key, byte val) {
        tag.setByte(key, val);
        return this;
    }

    public int getShort(String key, short def) {
        if(!checkKey(key, NBTTagType.SHORT))
            tag.setShort(key, def);
        return tag.getShort(key);
    }

    public NBTHelper setShort(String key, short val) {
        tag.setShort(key, val);
        return this;
    }

    public int getInteger(String key, int def) {
        if(!checkKey(key, NBTTagType.INT))
            tag.setInteger(key, def);
        return tag.getInteger(key);
    }

    public NBTHelper setInteger(String key, int val) {
        tag.setInteger(key, val);
        return this;
    }

    public long getLong(String key, long def) {
        if(!checkKey(key, NBTTagType.LONG))
            tag.setLong(key, def);
        return tag.getLong(key);
    }

    /**
     * @return self
     */
    public NBTHelper setLong(String key, long val) {
        tag.setLong(key, val);
        return this;
    }

    public float getFloat(String key, float def) {
        if(!checkKey(key, NBTTagType.FLOAT))
            tag.setFloat(key, def);
        return tag.getFloat(key);
    }

    /**
     * @return self
     */
    public NBTHelper setFloat(String key, float val) {
        tag.setFloat(key, val);
        return this;
    }

    public double getDouble(String key, double def) {
        if(!checkKey(key, NBTTagType.DOUBLE))
            tag.setDouble(key, def);
        return tag.getDouble(key);
    }

    /**
     * @return self
     */
    public NBTHelper setDouble(String key, double val) {
        tag.setDouble(key, val);
        return this;
    }

    public byte[] getByteArray(String key, byte[] def) {
        if(!checkKey(key, NBTTagType.BYTE_ARRAY))
            tag.setByteArray(key, def);
        return tag.getByteArray(key);
    }

    /**
     * @return self
     */
    public NBTHelper setByteArray(String key, byte[] val) {
        tag.setByteArray(key, val);
        return this;
    }

    public String getString(String key, String def) {
        if(!checkKey(key, NBTTagType.STRING))
            tag.setString(key, def);
        return tag.getString(key);
    }

    /**
     * @return self
     */
    public NBTHelper setString(String key, String val) {
        tag.setString(key, val);
        return this;
    }

    public NBTTagList getList(String key, NBTTagType type) {
        if(!checkKey(key, NBTTagType.LIST))
            tag.setTag(key, new NBTTagList());
        return tag.getTagList(key, type.getId());
    }

    /**
     * Probably shouldn't use this?
     * @return self
     */
    public NBTHelper setList(String key, NBTTagList val) {
        tag.setTag(key, val);
        return this;
    }

    public NBTHelper getCompound(String key) {
        if(!checkKey(key, NBTTagType.COMPOUND))
            tag.setTag(key, new NBTTagCompound());
        return wrap(tag.getCompoundTag(key));
    }

    /**
     * Probably shouldn't use this?
     * @return self
     */
    public NBTHelper setCompound(String key, NBTTagCompound val) {
        tag.setTag(key, val);
        return this;
    }

    public int[] getIntArray(String key, int[] def) {
        if(!checkKey(key, NBTTagType.INT))
            tag.setIntArray(key, def);
        return tag.getIntArray(key);
    }

    /**
     * @return self
     */
    public NBTHelper setIntArray(String key, int[] val) {
        tag.setIntArray(key, val);
        return this;
    }

    // Are these even used?
    public NBTTagLongArray getLongArray(String key, long[] def) {
        if(!checkKey(key, NBTTagType.INT))
            tag.setTag(key, new NBTTagLongArray(def).copy());
        return (NBTTagLongArray) tag.getTag(key);
    }

    /**
     * @return self
     */
    public NBTHelper setLongArray(String key, NBTTagLongArray val) {
        tag.setTag(key, val);
        return this;
    }
}
