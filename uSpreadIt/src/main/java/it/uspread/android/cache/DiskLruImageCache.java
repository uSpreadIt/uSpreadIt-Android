package it.uspread.android.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.uspread.android.BuildConfig;
import it.uspread.android.USpreadItApplication;

/**
 * Cache SD des images
 */
public class DiskLruImageCache {

    private static final int VALUE_COUNT = 1;
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    /** Cache stockant les images */
    private DiskLruCache mDiskCache;

    /** Répertoire absolu du cache */
    private File diskCacheDir;
    /** Taille du cache */
    private int diskCacheSize;

    /**
     * Constructeur
     *
     * @param context
     *         contexte
     * @param uniqueName
     *         nom du répertoire du cache
     * @param diskCacheSize
     *         Taille du cache
     */
    public DiskLruImageCache(final Context context, final String uniqueName, final int diskCacheSize) {
        diskCacheDir = getDiskCacheDir(context, uniqueName);
        this.diskCacheSize = diskCacheSize;
        openCache();
    }

    /**
     * Ouverture du cache
     */
    private void openCache() {
        try {
            if (mDiskCache == null || mDiskCache.isClosed()) {
                mDiskCache = DiskLruCache.open(diskCacheDir, USpreadItApplication.getInstance().getAppVersionCode(), VALUE_COUNT, diskCacheSize);
            }
        } catch (IOException e) {
            Log.d(getClass().getName(), "ERROR when opening image disk cache");
            e.printStackTrace();
        }
    }

    /**
     * Permet d'obtenir un sous répértoire dans la zone dédié au cache.<br>
     * Essaye d'utiliser le stockage externe ou à défaut l'interne.
     *
     * @param context
     *         Contexte
     * @param uniqueName
     *         Nom du répertoire dans la zone dédié au cache qui vas être utilisé
     * @return Sous répertoire dans la zone dédié au cache
     */
    private File getDiskCacheDir(final Context context, final String uniqueName) {
        final String cachePath = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() : context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Ecrit l'image dans le cache
     *
     * @param bitmap
     *         image
     * @param editor
     *         editor
     * @return vrai si réussi
     * @throws IOException
     */
    private boolean writeBitmapToFile(final Bitmap bitmap, final DiskLruCache.Editor editor)
            throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            return bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Ajoute une image au cache
     *
     * @param key
     *         Id du message
     * @param image
     *         image
     */
    public void putBitmap(final String key, final Bitmap image) {
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit(key);
            if (editor == null) {
                return;
            }

            if (writeBitmapToFile(image, editor)) {
                editor.commit();
                mDiskCache.flush();
                if (BuildConfig.DEBUG) {
                    Log.d(getClass().getName(), "Image put on disk cache " + key);
                }
            } else {
                editor.abort();
            }
        } catch (IOException e) {
            Log.d(getClass().getName(), "ERROR on: image put on disk cache " + key);
            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

    }

    /**
     * Retourne l'image du cache
     *
     * @param key
     *         Id du message
     * @return Image ou null si non trouvé
     */
    public Bitmap getBitmap(final String key) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get(key);
            if (snapshot == null) {
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                final BufferedInputStream buffIn = new BufferedInputStream(in, IO_BUFFER_SIZE);
                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (IOException e) {
            Log.d(getClass().getName(), "ERROR on: image put on disk cache " + key);
            e.printStackTrace();
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        if (BuildConfig.DEBUG) {
            if (bitmap == null) {
                Log.d(getClass().getName(), "Image not found on disk cache " + key);
            } else {
                Log.d(getClass().getName(), "Image read from disk cache " + key);
            }
        }

        return bitmap;

    }

    /**
     * Supprime l'image demandé du cache
     *
     * @param key
     *         Id du message
     */
    public void removeBitmap(final String key) {
        try {
            mDiskCache.remove(key);
            if (BuildConfig.DEBUG) {
                Log.d(getClass().getName(), "Image removed from disk cache " + key);
            }
        } catch (IOException e) {
            Log.d(getClass().getName(), "ERROR on: image remove on disk cache " + key);
            e.printStackTrace();
        }
    }

    /**
     * Vide le cache
     */
    public void clearCache() {
        try {
            mDiskCache.delete();
            openCache();
            if (BuildConfig.DEBUG) {
                Log.d(getClass().getName(), "Disk cache cleared");
            }
        } catch (IOException e) {
            Log.d(getClass().getName(), "ERROR on: clear image disk cache");
            e.printStackTrace();
        }
    }
}
