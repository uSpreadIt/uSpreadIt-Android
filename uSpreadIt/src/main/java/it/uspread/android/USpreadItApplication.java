package it.uspread.android;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import it.uspread.android.activity.USpreadItActivity;
import it.uspread.android.cache.ImageCache;
import it.uspread.android.cache.MessageCache;
import it.uspread.android.session.SessionManager;

/**
 * Classe principale de l'application.<br>
 * Permet d'avoir ce qui se rapproche le plus du pattern singleton en tenant compte du fonctionnement du framework android. Noter quand même qu'à tout moment une app peut être
 * entiérement killé par le systeme si besoin. Mais tant que l'app n'est pas killé on peut être sur de la persistence en mémoire des attributs de classe qui serait ajouté ici.
 * <ul>
 * <li>Gestionnaire de session</li>
 * <li>Cache des messages</li>
 * <li>Cache des images</li>
 * <li>Référence vers l'activité affiché en premier plan</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class USpreadItApplication extends Application {

    /** Instance unique de la classe : ceci est garantie par contrat du framework android */
    private static USpreadItApplication instance;

    /** Le gestionnaire de session */
    private SessionManager sessionManager;

    /** Cache des messages */
    private MessageCache messageCache;

    /** Cache des images */
    private ImageCache imageCache;

    /** Activité actuellement affiché en premier plan (Vaut null si aucune activité de l'application n'est actuellement en premier plan) */
    private USpreadItActivity runningActivity;

    /**
     * @return {@link #instance}
     */
    public static USpreadItApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initializeInstance();
    }

    /**
     * Inititialisation
     */
    protected void initializeInstance() {
        sessionManager = new SessionManager(this, getAppVersionCode());
        messageCache = new MessageCache(this, getAppVersionCode());
        imageCache = new ImageCache(this, getAppVersionCode());
    }

    /**
     * @return {@link #sessionManager}
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * @return {@link #messageCache}
     */
    public MessageCache getMessageCache() {
        return messageCache;
    }

    /**
     * @return {@link #imageCache}
     */
    public ImageCache getImageCache() {
        return imageCache;
    }

    /**
     * Vidage des caches
     */
    public void clearCaches() {
        messageCache.clear();
        imageCache.clear();
    }

    /**
     * @return {@link #runningActivity}
     */
    public USpreadItActivity getRunningActivity() {
        return runningActivity;
    }

    /**
     * @param currentActivity
     *         {@link #runningActivity}
     */
    public void setRunningActivity(final USpreadItActivity currentActivity) {
        this.runningActivity = currentActivity;
    }

    /**
     * Retourne la version de l'application depuis les infos du package
     *
     * @return version de l'applic
     */
    public int getAppVersionCode() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
