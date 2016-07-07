package it.uspread.android.remote;

import android.graphics.Bitmap;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.data.Status;
import it.uspread.android.data.User;
import it.uspread.android.data.UserRanking;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.data.criteria.StatusCriteria;
import it.uspread.android.data.type.BackgroundColor;
import it.uspread.android.data.type.MessageType;
import it.uspread.android.data.type.ReportType;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Implémentation pour test de l'interface en attendant un serveur complet ou pour pallier à des problèmes d'indisponibilité.<br>
 * L'implémentation de test est donc bien incomplète face à un branchement usr serveur<br>
 *
 * @author Lone Décosterd,
 */
public class CoreTest implements Core {

    /** Quick tmp class for little standalone demo */
    private static class MessageAuthor {

        /** Liste des messages reçus */
        public final List<Message> listReceivedMessage = new ArrayList<>();

        /** Liste des messages propagé */
        public final List<Message> listSpreadMessage = new ArrayList<>();

        /** Liste des messages de l'utilisateur */
        public final List<Message> listUserMessage = new ArrayList<>();

        public User author;

        public MessageAuthor(User author) {
            this.author = author;
        }
    }

    /** Utilisateur connecté */
    private User userConnected;

    /** Liste des utilisateurs et leurs messages */
    private final Map<User, MessageAuthor> listUser = new HashMap<>();

    /** Liste des classements utilisateurs */
    private final List<UserRanking> listUsersRanking = new ArrayList<>();

    /** incrément de l'id */
    private int idGenerator = 1;

    /**
     * Constructeur
     */
    public CoreTest() {
        try {
            USpreadItApplication.getInstance().clearCaches();

            User crom = new User("crom", "crom", "crom@free.fr");
            createUser(crom);
            User chuck = new User("chuck", "chuck", "chuck@free.fr");
            createUser(chuck);
            User gandalf = new User("gandalf", "gandalf", "gandalf@free.fr");
            createUser(gandalf);

            User current = null;
            for (User key : listUser.keySet()) {
                if (key.getUsername().equals(USpreadItApplication.getInstance().getSessionManager().getUsername())) {
                    current = key;
                }
            }
            userConnected = crom;

            Message msg = new Message("L'albatros");
            msg.setBackgroundColor(BackgroundColor.BROWN.getHtmlColor());
            sendMessage(msg);

            msg = new Message("Souvent, pour s'amuser, les hommes d'équipage\n" +
                    "Prennent des albatros, vastes oiseaux des mers,\n" +
                    "Qui suivent, indolents compagnons de voyage,\n" +
                    "Le navire glissant sur les gouffres amers.");
            msg.setBackgroundColor(BackgroundColor.PURPLE.getHtmlColor());
            sendMessage(msg);

            msg = new Message("A peine les ont-ils déposés sur les planches,\n" +
                    "Que ces rois de l'azur, maladroits et honteux,\n" +
                    "Laissent piteusement leurs grandes ailes blanches\n" +
                    "Comme des avirons traîner à côté d'eux.");
            msg.setBackgroundColor(BackgroundColor.ORANGE.getHtmlColor());
            sendMessage(msg);

            msg = new Message("Ce voyageur ailé, comme il est gauche et veule !\n" +
                    "Lui, naguère si beau, qu'il est comique et laid !\n" +
                    "L'un agace son bec avec un brûle-gueule,\n" +
                    "L'autre mime, en boitant, l'infirme qui volait !");
            msg.setBackgroundColor(BackgroundColor.BLUE.getHtmlColor());
            sendMessage(msg);

            msg = new Message("Le Poète est semblable au prince des nuées\n" +
                    "Qui hante la tempête et se rit de l'archer ;\n" +
                    "Exilé sur le sol au milieu des huées,\n" +
                    "Ses ailes de géant l'empêchent de marcher.");
            msg.setBackgroundColor(BackgroundColor.GREEN.getHtmlColor());
            sendMessage(msg);

            userConnected = current;

        } catch (USpreadItException e) {
            e.printStackTrace();
        }

        listUsersRanking.add(new UserRanking("CROM", 100));
        listUsersRanking.add(new UserRanking("Chuck Norris", 42));
    }

    /**
     * @see it.uspread.android.remote.Core#createUser(it.uspread.android.data.User)
     */
    @Override
    public void createUser(User user) throws USpreadItException {
        user.setId(idGenerator++);
        listUser.put(user, new MessageAuthor(user));
    }

    /**
     * @see it.uspread.android.remote.Core#loginUser(it.uspread.android.data.User, String)
     */
    @Override
    public void loginUser(User user, final String gcmRegistrationId) throws USpreadItException {
        boolean exist = false;
        for (User key : listUser.keySet()) {
            if (key.getUsername().equals(user.getUsername())) {
                exist = true;
                userConnected = key;
            }
        }
        if (!exist) {
            throw new USpreadItException(USpreadItException.Type.AUTHENTICATION_ERROR);
        }
    }

    /**
     * @see Core#sendGCMRegistrationId(String)
     */
    @Override
    public void sendGCMRegistrationId(final String gcmRegistrationId) throws USpreadItException {
    }

    /**
     * @see it.uspread.android.remote.Core#getUser()
     */
    @Override
    public User getUser() throws USpreadItException {
        return userConnected;
    }

    /**
     * @see Core#getUserStatus(it.uspread.android.data.criteria.StatusCriteria)
     */
    @Override
    public Status getUserStatus(final StatusCriteria criteria) throws USpreadItException {
        Status status = new Status();
        status.setQuotaReached(false);
        return status;
    }

    /**
     * @see it.uspread.android.remote.Core#sendMessage(it.uspread.android.data.Message)
     */
    @Override
    public Message sendMessage(final Message newMessage) throws USpreadItException {
        newMessage.setId(idGenerator++);
        newMessage.setMessageType(MessageType.WRITED);
        newMessage.setDateCreation(new Date());
        listUser.get(userConnected).listUserMessage.add(newMessage);
        spreadIt(newMessage);
        return newMessage;
    }

    /**
     * @see it.uspread.android.remote.Core#spreadMessage(it.uspread.android.data.Message)
     */
    @Override
    public Message spreadMessage(final Message message) throws USpreadItException {
        listUser.get(userConnected).listReceivedMessage.remove(message);
        listUser.get(userConnected).listSpreadMessage.add(message);
        message.setMessageType(MessageType.SPREAD);
        spreadIt(message);
        return message;
    }

    private void spreadIt(Message msg) {
        User author = null;
        msg.setNbSpread(msg.getNbSpread() + 1);
        for (User key : listUser.keySet()) {
            int idx = listUser.get(key).listSpreadMessage.indexOf(msg);
            if (idx > -1) {
                listUser.get(key).listSpreadMessage.get(idx).setNbSpread(msg.getNbSpread());
            }
            idx = listUser.get(key).listReceivedMessage.indexOf(msg);
            if (idx > -1) {
                listUser.get(key).listReceivedMessage.get(idx).setNbSpread(msg.getNbSpread());
            }
            idx = listUser.get(key).listUserMessage.indexOf(msg);
            if (idx > -1) {
                listUser.get(key).listUserMessage.get(idx).setNbSpread(msg.getNbSpread());
                author = key;
            }
        }

        for (User key : listUser.keySet()) {
            Bitmap img = msg.getBackgroundImage();
            msg.setBackgroundImage(null);
            Message msgCloned = SerializationUtils.clone(msg);
            msg.setBackgroundImage(img);
            msg.setDateSpread(new Date());
            if (!key.equals(author) && !listUser.get(key).listReceivedMessage.contains(msg) && !listUser.get(key).listSpreadMessage.contains(msg)) {
                msgCloned.setBackgroundImage(img);
                msgCloned.setDateReception(new Date());
                msgCloned.setMessageType(MessageType.RECEIVED);
                listUser.get(key).listReceivedMessage.add(msgCloned);
            }
        }
    }

    /**
     * @see it.uspread.android.remote.Core#ignoreMessage(it.uspread.android.data.Message)
     */
    @Override
    public void ignoreMessage(final Message message) throws USpreadItException {
        listUser.get(userConnected).listReceivedMessage.remove(message);
    }

    /**
     * @see it.uspread.android.remote.Core#reportMessage(it.uspread.android.data.Message, it.uspread.android.data.type.ReportType)
     */
    @Override
    public void reportMessage(final Message message, final ReportType reportType) throws USpreadItException {
        listUser.get(userConnected).listReceivedMessage.remove(message);
    }

    /**
     * @see it.uspread.android.remote.Core#getMessageImage(long)
     */
    @Override
    public Message getMessageImage(final long messageId) throws USpreadItException {
        for (User key : listUser.keySet()) {
            Message msgId = new Message();
            msgId.setId(messageId);
            int idx = listUser.get(key).listUserMessage.indexOf(msgId);
            if (idx > -1) {
                listUser.get(key).listUserMessage.get(idx);
            }
        }
        return null;
    }

    /**
     * @see it.uspread.android.remote.Core#deleteMessage(it.uspread.android.data.Message)
     */
    @Override
    public void deleteMessage(Message message) throws USpreadItException {
        for (User key : listUser.keySet()) {
            listUser.get(key).listUserMessage.remove(message);
            listUser.get(key).listReceivedMessage.remove(message);
            listUser.get(key).listSpreadMessage.remove(message);
        }
    }

    /**
     * @see it.uspread.android.remote.Core#getReceivedMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    @Override
    public List<Message> getReceivedMessage(final MessageCriteria criteria) throws USpreadItException {
        return listUser.get(userConnected).listReceivedMessage;
    }

    /**
     * @see it.uspread.android.remote.Core#getWritedMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    @Override
    public List<Message> getWritedMessage(final MessageCriteria criteria) throws USpreadItException {
        return listUser.get(userConnected).listUserMessage;
    }

    /**
     * @see it.uspread.android.remote.Core#getSpreadMessage(it.uspread.android.data.criteria.MessageCriteria)
     */
    @Override
    public List<Message> getSpreadMessage(final MessageCriteria criteria) throws USpreadItException {
        return listUser.get(userConnected).listSpreadMessage;
    }

    /**
     * @see it.uspread.android.remote.Core#getUsersRanking()
     */
    @Override
    public List<UserRanking> getUsersRanking() throws USpreadItException {
        return listUsersRanking;
    }
}
