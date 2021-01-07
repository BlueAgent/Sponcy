package sponcy.common.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHelper {
    private static final int BUFFER_SIZE = 4096;

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    /**
     * Reads in a UTF-8 string from the stream
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static String readString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toString("UTF-8");
    }

    /**
     * Compares input streams.
     * Reads stream b a byte at a time!!!
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean compare(InputStream a, InputStream b) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        int bbyte;
        while ((len = a.read(buf)) != -1) {
            for (int i = 0; i < len; i++) {
                bbyte = b.read();
                if (bbyte == -1) return false;
                if (buf[i] != bbyte) return false;
            }
        }
        //b isn't also at the end
        if (b.read() != -1)
            return false;

        return true;
    }
}
