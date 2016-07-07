package it.uspread.android.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.List;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.data.Status;
import it.uspread.android.data.User;
import it.uspread.android.data.UserRanking;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.data.criteria.StatusCriteria;
import it.uspread.android.data.type.ReportType;
import it.uspread.android.message.MessageUtils;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Facade de communication entre le client et le serveur.<br>
 * L'appel aux élements de cette méthode doivent être encapsulé dans un {@link android.os.AsyncTask} ou autre chose permettant ne ne pas occuper la Thread UI.
 *
 * @author Lone Décosterd,
 */
public class USpreadItServer {

    /** Instance du singleton */
    private static final USpreadItServer INSTANCE = new USpreadItServer();

    /** Implémentation utilisé pour la communication */
    private final Core core;

    /**
     * Constructeur inaccessible
     */
    private USpreadItServer() {
        // **************************************************************************************
        // ************************** ICI : pour passer de REST à TEST **************************
        // **************************************************************************************
        //core = new CoreRest();
        core = new CoreTest();
    }

    /**
     * Instance de la facade de communication avec le serveur.
     *
     * @return Le singleton de la facade
     */
    public static USpreadItServer getInstance() {
        return INSTANCE;
    }

    /**
     * Vérifie que le périphérique mobile est bien connecté actuellement
     */
    private void checkOnline() throws USpreadItException {
        final NetworkInfo netInfo = ((ConnectivityManager) USpreadItApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnectedOrConnecting()) {
            throw new USpreadItException(USpreadItException.Type.INTERNET);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#createUser(it.uspread.android.data.User)
     */
    public void createUser(final User user) throws USpreadItException {
        checkOnline();
        core.createUser(user);
    }

    /**
     * @see it.uspread.android.remote.Core#loginUser(it.uspread.android.data.User, String)
     */
    public void loginUser(final User user, final String gcmRegistrationId) throws USpreadItException {
        checkOnline();
        core.loginUser(user, gcmRegistrationId);
    }

    /**
     * @see Core#sendGCMRegistrationId(String)
     */
    public void sendGCMRegistrationId(final String gcmRegistrationId) throws USpreadItException {
        checkOnline();
        core.sendGCMRegistrationId(gcmRegistrationId);
    }

    /**
     * @see it.uspread.android.remote.Core#getUser()
     */
    public User getUser() throws USpreadItException {
        checkOnline();
        return core.getUser();
    }

    /**
     * @see Core#getUserStatus(it.uspread.android.data.criteria.StatusCriteria
     */
    public Status getUserStatus(final StatusCriteria criteria) throws USpreadItException {
        checkOnline();
        return core.getUserStatus(criteria);
    }

    /**
     * @see it.uspread.android.remote.Core#sendMessage(it.uspread.android.data.Message)
     */
    public Message sendMessage(final Message message) throws USpreadItException {
        checkOnline();
        MessageUtils.detectLinks(Message.inList(message));
        return core.sendMessage(message);
    }

    /**
     * @see it.uspread.android.remote.Core#spreadMessage(it.uspread.android.data.Message)
     */
    public Message spreadMessage(final Message message) throws USpreadItException {
        checkOnline();
        return core.spreadMessage(message);
    }

    /**
     * @see it.uspread.android.remote.Core#ignoreMessage(it.uspread.android.data.Message)
     */
    public void ignoreMessage(final Message message) throws USpreadItException {
        checkOnline();
        core.ignoreMessage(message);
    }

    /**
     * @see it.uspread.android.remote.Core#reportMessage(it.uspread.android.data.Message, it.uspread.android.data.type.ReportType)
     */
    public void reportMessage(final Message message, final ReportType reportType) throws USpreadItException {
        checkOnline();
        core.reportMessage(message, reportType);
    }

    /**
     * @see it.uspread.android.remote.Core#getMessageImage(long)
     */
    public Message getMessageImage(final long messageId) throws USpreadItException {
        checkOnline();
        return core.getMessageImage(messageId);
    }

    /**
     * @see it.uspread.android.remote.Core#deleteMessage(it.uspread.android.data.Message)
     */
    public void deleteMessage(final Message message) throws USpreadItException {
        checkOnline();
        core.deleteMessage(message);
    }

    /**
     * @see it.uspread.android.remote.Core#getReceivedMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    public List<Message> getReceivedMessage(final MessageCriteria criteria) throws USpreadItException {
        checkOnline();
        final List<Message> listMessage = core.getReceivedMessage(criteria);
        if (!criteria.isOnlyDynamicValue()) {
            MessageUtils.detectLinks(listMessage);
        }
        return listMessage;
    }

    /**
     * @see it.uspread.android.remote.Core#getWritedMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    public List<Message> getWritedMessage(final MessageCriteria criteria) throws USpreadItException {
        checkOnline();
        final List<Message> listMessage = core.getWritedMessage(criteria);
        if (!criteria.isOnlyDynamicValue()) {
            MessageUtils.detectLinks(listMessage);
        }
        return listMessage;
    }

    /**
     * @see it.uspread.android.remote.Core#getSpreadMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    public List<Message> getSpreadMessage(final MessageCriteria criteria) throws USpreadItException {
        checkOnline();
        final List<Message> listMessage = core.getSpreadMessage(criteria);
        if (!criteria.isOnlyDynamicValue()) {
            MessageUtils.detectLinks(listMessage);
        }
        return listMessage;
    }

    /**
     * @see it.uspread.android.remote.Core#getUsersRanking()
     */
    public List<UserRanking> getUsersRanking() throws USpreadItException {
        checkOnline();
        return core.getUsersRanking();
    }
}
