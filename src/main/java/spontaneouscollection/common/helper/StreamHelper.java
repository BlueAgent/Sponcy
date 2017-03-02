package spontaneouscollection.common.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHelper {
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
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
}
