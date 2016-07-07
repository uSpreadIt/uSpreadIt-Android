package it.uspread.android.remote.url;

/**
 * Liste des URL de communication avec le server.<br>
 *
 * @author Lone Décosterd,
 */
public class ServerRestURL {
    /** URL du serveur */
    //private static final String SERVER_URL = "http://10.0.2.2:8080/uSpread-Core/rest/"; // Pour taper en local, en théorie, en pratique chez moi ça ne fonctionne pas :-(
    private static final String SERVER_URL = "http://uspreadit.herokuapp.com/rest/";

    /** Enregister un nouvel utilisateur */
    public static final String SIGNUP_USER = SERVER_URL + "signup";

    /** Login de l'utilisateur */
    public static final String LOGIN_USER = SERVER_URL + "login";

    /** Enregistrement du push token (GCM Registration ID) */
    public static final String SEND_GCM_PUSH_TOKEN = SERVER_URL + "users/connected/pushtoken";

    /** Info du compte de l'utilisateur connecté */
    public static final String GET_CONNECTED_USER = SERVER_URL + "users/connected";

    /** Info de statut de l'utilisateur connecté */
    public static final String GET_CONNECTED_USER_STATUS = SERVER_URL + "users/connected/status";

    /** Messages reçus */
    public static final String LIST_RECEIVED_MESSAGES = SERVER_URL + "messages?msg=RECEIVED";

    /** Messages écrit par l'utilisateur */
    public static final String LIST_WRITED_MESSAGES = SERVER_URL + "messages?msg=AUTHOR";

    /** Messages propagé par l'utilisateur */
    public static final String LIST_SPREAD_MESSAGES = SERVER_URL + "messages?msg=SPREAD";

    /** Creer un message */
    public static final String SEND_MESSAGE = SERVER_URL + "messages";

    /** Propager un message */
    public static final String SPREAD_MESSAGE = SERVER_URL + "messages/{0}/spread";

    /** Ignorer un message */
    public static final String IGNORE_MESSAGE = SERVER_URL + "messages/{0}/ignore";

    /** Signaler un message */
    public static final String REPORT_MESSAGE = SERVER_URL + "messages/{0}/report?type={1}";

    /** Image d'un message */
    public static final String GET_MESSAGE_IMAGE = SERVER_URL + "messages/{0}?onlyImg=true";

    /** Supprimer un message */
    public static final String DELETE_MESSAGE = SERVER_URL + "messages/{0}";
}
