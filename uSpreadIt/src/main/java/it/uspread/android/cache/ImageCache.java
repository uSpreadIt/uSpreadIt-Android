package it.uspread.android.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.LruCache;

import it.uspread.android.message.MessageView;
import it.uspread.android.task.TaskLoadImageCacheOrWeb;

/**
 * Cache des images.<br>
 * L'utilisation d'un cache en mémoire vive et d'un cache en mémoire physique permet d'avoir un comprimis entre rapidité d'accès et mémoire utilisé.<br>
 * Chaque accés à un élément du cache le replace en tête ce qui fait que quand la capacité du cache devient insuffisante c'est l'élément qui n'a pas été utilisé depuis le plus
 * longtemps qui est retiré.
 *
 * @author Lone Décosterd,
 */
public class ImageCache {
    /** Taille du cache des images en mémoire vive (en byte) */
    private static final int MEMORY_CACHE_SIZE = 1024 * 1024 * 100;
    /** Taille du cache des images en mémoire disque (en byte) */
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 300;
    /** Répertoire du cache disque */
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    /** Nom du fichier ou sera stocké des infos du cache des images */
    private static final String PREF_NAME = "it.uspread.android.messages.image";
    /** Version de l'application associé aux infos stockés */
    private static final String APP_VERSION = "version";

    /**
     * Cache en mémoire vive des images. Il est plus petit que le cache SD et permet de conserver un accès rapide aux images
     */
    private LruCache<String, Bitmap> imageMemoryCache;

    /** Cache sur disque des images : permet d'éviter de trop utiser de la précieuse bande passante. Les images peuvent rester presque indéfinniment dans le cache */
    private DiskLruImageCache imageDiskCache; // TODO imageDiskCache est t'il ThreadSafe ? si c'est le cas tout les synchronized restant peuvent être viré

    /**
     * Constructeur.
     *
     * @param appContext
     *         Contexte de l'application
     * @param versionCode
     *         version actuelle de l'application
     */
    public ImageCache(final Context appContext, final int versionCode) {
        // La taille du cache en mémoire fait #MEMORY_CACHE_SIZE Mo sauf si la mémoire disponible n'est pas suffisament élevée pour oser réserver ceci.
        final int maxMemory = (int) Runtime.getRuntime().maxMemory();
        final int cacheSize = Math.min(maxMemory / 8, MEMORY_CACHE_SIZE);
        imageMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // La taille du cache est mesuré en byte plutôt qu'en nombre d'éléments
                return bitmap.getByteCount();
            }
        };

        imageDiskCache = new DiskLruImageCache(appContext, DISK_CACHE_SUBDIR, DISK_CACHE_SIZE);

        // Réinitialiser le cache en cas de changement de version
        SharedPreferences pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (pref.getInt(APP_VERSION, 0) != versionCode) {
            final SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.putInt(APP_VERSION, versionCode);
            editor.apply();
            imageDiskCache.clearCache();
        }
    }

    /**
     * Demande de destruction du cache.
     */
    public synchronized void clear() {
        imageMemoryCache.evictAll();
        imageDiskCache.clearCache();
    }

    /**
     * Ajout d'une image au cache si non présente.<br>
     * NE devrait pas être appelé depuis la thread UI
     *
     * @param messageId
     *         Id du message
     * @param bitmap
     *         image
     * @param alreadyInCacheSD
     *         Indique que l'image est déjà dans le cache SD (Elle n'y sera donc pas remise)
     */
    public synchronized void addImageToCache(final long messageId, final Bitmap bitmap, final boolean alreadyInCacheSD) {
        if (getImageFromCacheRAM(messageId) == null) {
            imageMemoryCache.put(String.valueOf(messageId), bitmap);
            if (!alreadyInCacheSD) {
                imageDiskCache.putBitmap(String.valueOf(messageId), bitmap);
            }
        }
    }

    /**
     * Retrouve une image du cache RAM
     *
     * @param messageId
     *         Id du message
     * @return L'image ou null si non présente
     */
    public Bitmap getImageFromCacheRAM(final long messageId) {
        return imageMemoryCache.get(String.valueOf(messageId));
    }

    /**
     * Retrouve une image du cache SD.<br>
     * NE devrait pas être appelé depuis la thread UI
     *
     * @param messageId
     *         Id du message
     * @return L'image ou null si non présente
     */
    public synchronized Bitmap getImageFromCacheSD(final long messageId) {
        return imageDiskCache.getBitmap(String.valueOf(messageId));
    }

    /**
     * Lance le chargement de l'image en cache SD ou Web
     *
     * @param messageId
     *         Id du message
     */
    public void loadImageFromCacheSDorWeb(final long messageId, final MessageView messageView) {
        new TaskLoadImageCacheOrWeb(messageId, messageView).execute();
    }

    /**
     * Supprime une image du cache
     *
     * @param messageId
     *         Id du message
     */
    public synchronized void removeImageFromCache(final long messageId) {
        imageMemoryCache.remove(String.valueOf(messageId));
        imageDiskCache.removeBitmap(String.valueOf(messageId));
    }
}
