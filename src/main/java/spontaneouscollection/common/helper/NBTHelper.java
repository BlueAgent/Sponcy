package spontaneouscollection.common.helper;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;

public class NBTHelper {
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
}
