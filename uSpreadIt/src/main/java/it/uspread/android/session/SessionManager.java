package it.uspread.android.session;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.NavigationDrawerActivity;
import it.uspread.android.data.User;
import it.uspread.android.task.Task;

/**
 * Gestion de la session.<br>
 * La notion de {@link android.content.SharedPreferences} est utilisé pour stocker les informations d'authentification.
 *
 * @author Lone Décosterd,
 */
public class SessionManager {

    /** Nom du fichier ou sera stocké la session */
    private static final String PREF_NAME = "it.uspread.android.session";
    /** Version de l'application associé aux infos stockés */
    private static final String APP_VERSION = "version";
    /** Indique si l'utilisateur est logué */
    private static final String IS_LOGIN = "isLoggedIn";
    /** Nom d'utilisateur */
    private static final String KEY_USERNAME = "username";
    /** Mot de passe TODO temporaire premier sprint d'identification : remplacer par un systeme de token plutôt que de donner toujours le pass */
    private static final String KEY_PASSWORD = "password";
    /** Token d'enregistrement au service GCM (Registration ID) */
    private static final String GCM_REGISTRATION_ID = "gcm_registrationID";

    /** Contexte de l'application */
    private final Context appContext;
    /** Shared Preferences pour le stockage persistant en cas de fermeture de l'application */
    private final SharedPreferences pref;

    /**
     * Constructeur.<br>
     * Accède aux informations de session.
     *
     * @param appContext
     *         {@link #appContext}
     * @param versionCode
     *         version actuelle de l'application
     */
    public SessionManager(final Context appContext, final int versionCode) {
        this.appContext = appContext;
        pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Réinitialiser les infos sauvegardé si la version a changé
        if (pref.getInt(APP_VERSION, 0) != versionCode) {
            final Editor editor = pref.edit();
            editor.clear();
            editor.putInt(APP_VERSION, versionCode);
            editor.apply();
        }
    }

    /**
     * @return Le nom de l'utilisateur connecté ou Unknown si pas connecté (cela dit il n'y a pas de raison d'utiliser cette méthode si l'user n'est pas connecté)
     */
    public String getUsername() {
        return pref.getString(KEY_USERNAME, "Unknown");
    }

    /**
     * // TODO a supprimer lorsque token de connection
     *
     * @return Le pass de l'utilisateur connecté ou Unknown si pas connecté (cela dit il n'y a pas de raison d'utiliser cette méthode si l'user n'est pas connecté)
     */
    public String getPassword() {
        return pref.getString(KEY_PASSWORD, "Unknown");
    }

    /**
     * @return Le token d'enregistrement au service GCM (Registration ID)
     */
    public String getGCMRegistrationId() {
        return pref.getString(GCM_REGISTRATION_ID, null);
    }

    /**
     * Sauvegarde le token d'enregistrement au service GCM (Registration ID)
     *
     * @param gcmRegistrationId
     *         Registration ID
     */
    public void setGCMRegistrationId(final String gcmRegistrationId) {
        final Editor editor = pref.edit();
        editor.putString(GCM_REGISTRATION_ID, gcmRegistrationId);
        editor.apply();
    }

    /**
     * Création de la session
     *
     * @param user
     *         L'utilisateur
     */
    public void createSession(final User user) {
        final Editor editor = pref.edit();

        // Stockage des éléments utile à la conservation de la session
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_USERNAME, user.getUsername());
        editor.putString(KEY_PASSWORD, user.getPassword());
        editor.apply();

        // Le cache de message doit être absolument vidé
        USpreadItApplication.getInstance().clearCaches();
    }

    /**
     * Destruction des informations de session
     */
    public void clearSession() {
        final Editor editor = pref.edit();

        editor.remove(IS_LOGIN);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.apply();

        // Le cache de message doit être vidé
        USpreadItApplication.getInstance().clearCaches();
    }

    /**
     * Délogue l'utilisateur, ferme les activités courante puis lance l'activité de login (en relancant l'application)
     */
    public void logout() {
        clearSession();

        // Annulation de toutes les taches en cours d'éxécution
        Task.cancelAllPendingTask();

        // TODO Vérifier que fonctionne aussi lors d'une déconnexion lancé depuis une task
        Context activityContext = USpreadItApplication.getInstance().getRunningActivity();
        if (activityContext == null) {
            activityContext = appContext;
        }

        final Intent intent = new Intent(activityContext, NavigationDrawerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Lancement de l'activité de login en relancant l'activité principale
        // (Attention sur des changement de cette dynamqiue s'assurer que l'écran de login n'ai pas de back possible et ne soit pas accessible par un back)
        activityContext.startActivity(intent);
    }

    /**
     * Indique si l'utilisateur est logué
     *
     * @return Vrai si logué
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

}