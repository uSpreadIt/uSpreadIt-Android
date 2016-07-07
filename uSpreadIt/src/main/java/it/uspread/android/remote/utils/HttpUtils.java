package it.uspread.android.remote.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import it.uspread.android.session.SessionManager;

/**
 * Classe utilitaire HTTP<br>
 *
 * @author Lone Décosterd,
 */
public class HttpUtils {

    /** Le format UTF-8 utilisé */
    public static final String UTF8 = "UTF-8";

    /** Le format du contenu des communications http */
    private static final String CONTENT_TYPE = "application/json; charset=" + UTF8;

    /** TimeOut en seconde pour effectuer la connexion */
    private static final int CONNECT_TIMEOUT = 15;

    /** TimeOut en seconde pour attendre les données */
    private static final int SOCKET_TIMEOUT = 30;

    /**
     * Création d'une nouvelle connection HTTP vers l'URL demandé
     *
     * @param url
     *         Url utilisé pour la connexion
     * @return l'objet HttpURLConnection
     * @throws IOException
     */
    private static HttpURLConnection createConnection(final String url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setUseCaches(false);
        connection.setConnectTimeout(HttpUtils.CONNECT_TIMEOUT * 1000);
        connection.setReadTimeout(HttpUtils.SOCKET_TIMEOUT * 1000);
        return connection;
    }

    /**
     * Création d'une nouvelle connection GET vers l'URL demandé
     *
     * @param url
     *         Url utilisé pour la connexion
     * @param session
     *         la session  ou null si pas d'auhtentification nécessaire
     * @return l'objet HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection createGetConnection(final String url, final SessionManager session) throws IOException {
        final HttpURLConnection connection = createConnection(url);

        connection.setRequestProperty("Accept", CONTENT_TYPE);
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod("GET");
        if (session != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(session.getUsername(), session.getPassword().toCharArray());
                }
            });
        }
        return connection;
    }

    /**
     * Création d'une nouvelle connection POST vers l'URL demandé
     *
     * @param url
     *         Url utilisé pour la connexion
     * @param session
     *         la session  ou null si pas d'auhtentification nécessaire
     * @return l'objet HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection createPostConnection(final String url, final SessionManager session) throws IOException {
        final HttpURLConnection connection = createConnection(url);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", CONTENT_TYPE);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        if (session != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(session.getUsername(), session.getPassword().toCharArray());
                }
            });
        }
        return connection;
    }

    /**
     * Création d'une nouvelle connection PUT vers l'URL demandé
     *
     * @param url
     *         Url utilisé pour la connexion
     * @param session
     *         la session  ou null si pas d'auhtentification nécessaire
     * @return l'objet HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection createPutConnection(final String url, final SessionManager session) throws IOException {
        final HttpURLConnection connection = createConnection(url);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Accept", CONTENT_TYPE);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        if (session != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(session.getUsername(), session.getPassword().toCharArray());
                }
            });
        }
        return connection;
    }

    /**
     * Création d'une nouvelle connection DELETE vers l'URL demandé
     *
     * @param url
     *         Url utilisé pour la connexion
     * @param session
     *         la session  ou null si pas d'auhtentification nécessaire
     * @return l'objet HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection createDeleteConnection(final String url, final SessionManager session) throws IOException {
        final HttpURLConnection connection = createConnection(url);
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Accept", CONTENT_TYPE);
        connection.setRequestProperty("Content-Type", CONTENT_TYPE);
        connection.setDoInput(true);
        connection.setDoOutput(false);
        if (session != null) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(session.getUsername(), session.getPassword().toCharArray());
                }
            });
        }
        return connection;
    }

    /**
     * Lecture du contenu de la réponse de la requete
     *
     * @param inputStream
     *         Le inputStream
     * @return le contenu sous forme de string
     * @throws IOException
     */
    public static String readResponseContent(InputStream inputStream) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        br.close();
        return stringBuilder.toString();
    }
}
