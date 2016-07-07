package it.uspread.android.remote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.data.Status;
import it.uspread.android.data.User;
import it.uspread.android.data.UserRanking;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.data.criteria.StatusCriteria;
import it.uspread.android.data.criteria.UserCriteria;
import it.uspread.android.data.type.MessageType;
import it.uspread.android.data.type.ReportType;
import it.uspread.android.remote.exception.USpreadItException;
import it.uspread.android.remote.url.ServerRestURL;
import it.uspread.android.remote.utils.HttpUtils;
import it.uspread.android.session.SessionManager;

/**
 * Communication avec le serveur.<br>
 *
 * @author Lone Décosterd,
 */
public class CoreRest implements Core {

    /**
     * @see it.uspread.android.remote.Core#createUser(it.uspread.android.data.User)
     */
    @Override
    public void createUser(User user) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createPostConnection(ServerRestURL.SIGNUP_USER, null);

            final OutputStream os = connection.getOutputStream();
            os.write(user.toJSONCreation().toString().getBytes(HttpUtils.UTF8));
            os.close();

            // Analyse requête
            getRequestResult(connection);
        } catch (IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#loginUser(it.uspread.android.data.User, String)
     */
    @Override
    public void loginUser(final User user, final String gcmRegistrationId) throws USpreadItException {
        SessionManager tmp = new SessionManager(USpreadItApplication.getInstance().getApplicationContext(), USpreadItApplication.getInstance().getAppVersionCode()) {
            @Override
            public String getUsername() {
                return user.getUsername();
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }
        }; // TODO ce tmp est à supprimer quand le login fera un vrai login

        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createPostConnection(new UserCriteria(gcmRegistrationId).addCriteriaToUrl(ServerRestURL.LOGIN_USER), tmp);

            final OutputStream os = connection.getOutputStream();
            os.write(user.toJSONLogin().toString().getBytes(HttpUtils.UTF8));
            os.close();

            // Analyse requête
            getRequestResult(connection);
        } catch (USpreadItException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.AUTHENTICATION_ERROR);
        }
    }

    /**
     * @see Core#sendGCMRegistrationId(String)
     */
    @Override
    public void sendGCMRegistrationId(final String gcmRegistrationId) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createPostConnection(ServerRestURL.SEND_GCM_PUSH_TOKEN, USpreadItApplication.getInstance().getSessionManager());

            final OutputStream os = connection.getOutputStream();
            final JSONObject json = new JSONObject();
            try {
                json.put("pToken", gcmRegistrationId);
                json.put("device", "ANDROID");
            } catch (JSONException e) {
                throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
            }
            os.write(json.toString().getBytes(HttpUtils.UTF8));
            os.close();

            // Analyse requête
            getRequestResult(connection);
        } catch (IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getUser()
     */
    @Override
    public User getUser() throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createGetConnection(ServerRestURL.GET_CONNECTED_USER, USpreadItApplication.getInstance().getSessionManager());

            // Analyse et Lecture du retour au format JSON
            final JSONObject json = new JSONObject(getRequestResult(connection));
            return new User(json);
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getUserStatus(it.uspread.android.data.criteria.StatusCriteria)
     */
    @Override
    public Status getUserStatus(final StatusCriteria criteria) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createGetConnection(criteria.addCriteriaToUrl(ServerRestURL.GET_CONNECTED_USER_STATUS), USpreadItApplication.getInstance().getSessionManager());

            // Analyse et Lecture du retour au format JSON
            final JSONObject json = new JSONObject(getRequestResult(connection));
            return new Status(json);
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#sendMessage(it.uspread.android.data.Message)
     */
    @Override
    public Message sendMessage(final Message newMessage) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createPostConnection(ServerRestURL.SEND_MESSAGE, USpreadItApplication.getInstance().getSessionManager());

            final OutputStream os = connection.getOutputStream();
            os.write(newMessage.toCreationJSON().toString().getBytes(HttpUtils.UTF8));
            os.close();

            // Analyse et Lecture du retour au format JSON
            final JSONObject json = new JSONObject(getRequestResult(connection));
            newMessage.updateCreationValue(new Message(json, MessageType.WRITED));
            return newMessage;
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }

    }

    /**
     * @see it.uspread.android.remote.Core#spreadMessage(it.uspread.android.data.Message)
     */
    @Override
    public Message spreadMessage(final Message message) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createPostConnection(ServerRestURL.SPREAD_MESSAGE.replaceAll("\\{0\\}", String.valueOf(message.getId())), USpreadItApplication.getInstance().getSessionManager());

            // Analyse et Lecture du retour au format JSON
            final JSONObject json = new JSONObject(getRequestResult(connection));
            Message msgUpdate = new Message(json, MessageType.SPREAD);
            message.updateDynamicValue(msgUpdate);
            message.setDateSpread(msgUpdate.getDateSpread()); // Il faut récupérer la date de spread placé par le serveur
            message.setMessageType(MessageType.SPREAD); // Le message va changer de liste il faut mettre à jour son type
            return message;
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#ignoreMessage(it.uspread.android.data.Message)
     */
    @Override
    public void ignoreMessage(final Message message) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createPostConnection(ServerRestURL.IGNORE_MESSAGE.replaceAll("\\{0\\}", String.valueOf(message.getId())), USpreadItApplication.getInstance().getSessionManager());

            // Analyse requête
            getRequestResult(connection);
        } catch (IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#reportMessage(it.uspread.android.data.Message, it.uspread.android.data.type.ReportType)
     */
    @Override
    public void reportMessage(final Message message, final ReportType reportType) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createPostConnection(ServerRestURL.REPORT_MESSAGE.replaceAll("\\{0\\}", String.valueOf(message.getId())).replaceAll("\\{1\\}", reportType.name()), USpreadItApplication.getInstance().getSessionManager());

            // Analyse requête
            getRequestResult(connection);
        } catch (IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getMessageImage(long)
     */
    @Override
    public Message getMessageImage(final long messageId) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createGetConnection(ServerRestURL.GET_MESSAGE_IMAGE.replaceAll("\\{0\\}",  String.valueOf(messageId)), USpreadItApplication.getInstance().getSessionManager());

            // Analyse et Lecture du retour au format JSON
            final JSONObject json = new JSONObject(getRequestResult(connection));
            return new Message(json, MessageType.UPDATE);
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#deleteMessage(it.uspread.android.data.Message)
     */
    @Override
    public void deleteMessage(Message message) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createDeleteConnection(ServerRestURL.DELETE_MESSAGE.replaceAll("\\{0\\}", String.valueOf(message.getId())), USpreadItApplication.getInstance().getSessionManager());

            // Analyse requête
            getRequestResult(connection);
        } catch (IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getReceivedMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    @Override
    public List<Message> getReceivedMessage(final MessageCriteria criteria) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createGetConnection(criteria.addCriteriaToUrl(ServerRestURL.LIST_RECEIVED_MESSAGES), USpreadItApplication.getInstance().getSessionManager());

            // Analyse et Lecture du retour au format JSON
            final JSONArray array = new JSONArray(getRequestResult(connection));
            MessageType messageType = MessageType.RECEIVED;
            if (criteria.isOnlyDynamicValue()) {
                messageType = MessageType.UPDATE;
            }
            return jsonArrayToMessage(array, messageType);
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getWritedMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    @Override
    public List<Message> getWritedMessage(final MessageCriteria criteria) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createGetConnection(criteria.addCriteriaToUrl(ServerRestURL.LIST_WRITED_MESSAGES), USpreadItApplication.getInstance().getSessionManager());

            // Analyse et Lecture du retour au format JSON
            final JSONArray array = new JSONArray(getRequestResult(connection));
            MessageType messageType = MessageType.WRITED;
            if (criteria.isOnlyDynamicValue()) {
                messageType = MessageType.UPDATE;
            }
            return jsonArrayToMessage(array, messageType);
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getSpreadMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    @Override
    public List<Message> getSpreadMessage(final MessageCriteria criteria) throws USpreadItException {
        try {
            // Execution requête
            final HttpURLConnection connection = HttpUtils.createGetConnection(criteria.addCriteriaToUrl(ServerRestURL.LIST_SPREAD_MESSAGES), USpreadItApplication.getInstance().getSessionManager());

            // Analyse et Lecture du retour au format JSON
            final JSONArray array = new JSONArray(getRequestResult(connection));
            MessageType messageType = MessageType.SPREAD;
            if (criteria.isOnlyDynamicValue()) {
                messageType = MessageType.UPDATE;
            }
            return jsonArrayToMessage(array, messageType);
        } catch (JSONException | IOException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getUsersRanking()
     */
    @Override
    public List<UserRanking> getUsersRanking() throws USpreadItException {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    /**
     * Conversion du tableau json en une liste de messages
     *
     * @param array
     *         tableau json de messages
     * @return Liste de message pouvant être vide
     */
    private List<Message> jsonArrayToMessage(final JSONArray array, final MessageType messageType) throws USpreadItException {
        final List<Message> listMessage = new ArrayList<>();
        Message msg;
        try {
            for (int i = 0; i < array.length(); i++) {
                msg = new Message(array.getJSONObject(i), messageType);
                listMessage.add(msg);
            }
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }

        return listMessage;
    }

    /**
     * Analyse et retourne le résultat de la requete HTTP
     *
     * @param connection
     *         La connection
     * @return la réponse converti en string
     * @throws it.uspread.android.remote.exception.USpreadItException
     */
    private String getRequestResult(final HttpURLConnection connection) throws USpreadItException {
        String response = "";
        try {
            response = HttpUtils.readResponseContent(connection.getInputStream());
            checkResponseStatus(connection, response);
        } catch (final IOException e) {
            // En cas d'erreur de lecture du body de la réponse on peut quand même analyser le code de retour
            checkResponseStatus(connection, connection.getURL().toString());
        } finally {
            connection.disconnect();
        }

        return response;
    }

    /**
     * Vérifie les codes de réponse pour générer l'erreur approprié
     *
     * @param connection
     *         La connection
     * @throws it.uspread.android.remote.exception.USpreadItException
     */
    private void checkResponseStatus(final HttpURLConnection connection, final String response) throws USpreadItException {
        int statusCode;
        try {
            statusCode = connection.getResponseCode();
        } catch (IOException e) {
            // Il semble que l'exception se passe en cas de timeout
            statusCode = 408;
        }

        if (statusCode == 401) {
            throw new USpreadItException(USpreadItException.Type.AUTHENTICATION_REQUIRED);
        } else if (statusCode == 403) {
            throw new USpreadItException(USpreadItException.Type.FORBIDDEN);
        } else if (statusCode == 118) {
            throw new USpreadItException(USpreadItException.Type.COMMUNICATION_UNREACHABLE);
        } else if (statusCode == 503) {
            throw new USpreadItException(USpreadItException.Type.COMMUNICATION_UNAVAILABLE);
        } else if (statusCode == 408) {
            throw new USpreadItException(USpreadItException.Type.COMMUNICATION_TIMEOUT);
        } else if (statusCode == 550) {
            throw new USpreadItException(USpreadItException.Type.QUOTA);
        } else if (statusCode >= 400 && statusCode <= 599) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, "Status-Code : " + statusCode + " - " + response);
        }
    }
}
