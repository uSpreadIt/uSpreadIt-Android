package it.uspread.android.gcm;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Classes utilitaires pour la gestion du GCM
 *
 * @author Lone Décosterd,
 */
public class GcmUtils {

    /** Log tag */
    private static final String LOG_TAG = "GcmUtils";

    /** Numéro de projet (https://developer.android.com/google/gcm/gs.html) */
    private static final String SENDER_ID = "";

    /** Clé de requête premettant de demander l'installation de l'APK Google Play Service */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    /**
     * Vérifie que le périphérique est prêt pour l'utilisation de GCM
     *
     * @param activity
     *         activité lançant le check
     */
    public static boolean checkPlayServices(final Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(LOG_TAG, "This device is not supported. GCM will not be in use");
            }
            return false;
        }
        return true;
    }

    /**
     * Tentative d'obtention du token d'enregistrement au service GCM (Registration ID). Puis envoie du token au serveur. Et enfin si le serveur l'as bien reçus on l'enregistre
     * dans l'objet de session. On est ainsi certain que la présence en session reflète que toute la chaine à bien été éxécuté sans erreur.
     *
     * @param context
     *         contexte
     */
    public static void obtainNewGCMRegistrationIDInBackground(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                    final String registrationId = gcm.register(SENDER_ID);

                    try {
                        USpreadItServer.getInstance().sendGCMRegistrationId(registrationId);

                        // Persistance du token afin de le réutiliser aux prochain lancements de l'applications
                        USpreadItApplication.getInstance().getSessionManager().setGCMRegistrationId(registrationId);
                    } catch (USpreadItException e) {
                        e.printStackTrace();
                        // Pas génant ce sera retenté lors de le prochaine ouverture de l'application
                        Log.i(LOG_TAG, "Failed to transmit registration ID to server");
                    }
                } catch (IOException e) {
                    // Pas génant ce sera retenté lors de le prochaine ouverture de l'application
                    Log.i(LOG_TAG, "Error on GCM register");
                }
                return null;
            }
        }.execute();
    }

}
