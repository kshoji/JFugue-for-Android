package jp.kshoji.jfuguesample.util;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utilities for Asset files
 */
public class AssetUtils {

    /**
     * Obtains count of files on specified path
     *
     * @param context the context
     * @param path directory on assets
     * @return count of files
     */
    public static int getAssetFileCount(final Context context, final String path) {
        try {
            return context.getResources().getAssets().list(path).length;
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Obtains asset file content as String
     *
     * @param context the context
     * @param filename asset file name
     * @return read String
     */
    public static String getAssetFileAsString(final Context context, final String filename) {
        try {
            final InputStream inputStream = context.getResources().getAssets().open(filename);
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int count;
            final byte[] data = new byte[1024];
            while ((count = inputStream.read(data)) >= 0) {
                baos.write(data, 0, count);
            }
            return new String(baos.toByteArray());
        } catch (final IOException ignored) {

        }
        return "";
    }
}
