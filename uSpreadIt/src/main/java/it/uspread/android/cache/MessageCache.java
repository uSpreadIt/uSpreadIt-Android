package it.uspread.android.cache;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.data.type.BackgroundType;
import it.uspread.android.data.type.MessageType;
import it.uspread.android.message.MessageComparator;
import it.uspread.android.message.MessageUtils;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Gestion du cache des messages des utilisateurs.<br>
 * <p>Fonctionnement du cache en mémoire RAM :</p>
 * <ul>
 * <li>Les messages sont séparé suivant leurs types : (Reçus, Propagés, Ecrits)</li>
 * <li>Un message peut être supprimé du cache</li>
 * <li>Une synchronisation peut être demandé dans ce cas on met à jour les messages avec les éventuelles nouvelles informations (nb de propagation) et on ajoute les messages qui
 * ne sont pas encore présent en cache</li>
 * <li>Les messages sont ordonné par date de création décroissante : du plus récent au plus ancien</li>
 * </ul>
 * <p/>
 * <p>Fonctionnement du cache en mémoire SD :</p>
 * <ul>
 * <li>Le cache SD est synchrone aux cache RAM sauf qu'il ne retiens pour chaque liste que les {@link #NB_MESSAGE_LIST_IN_CACHE_SD} premiers messages</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class MessageCache {

    /** Nom du fichier ou sera stocké le cache des messages */
    private static final String PREF_NAME = "it.uspread.android.messages";
    /** Version de l'application associé aux infos stockés */
    private static final String APP_VERSION = "version";
    /** Nombre de message par liste à garder en cache SD */
    private static final int NB_MESSAGE_LIST_IN_CACHE_SD = 10;
    /** Nombre de message par grille à garder en cache SD */
    private static final int NB_MESSAGE_GRID_IN_CACHE_SD = 25;
    /** Shared Preferences pour le stockage persistant en cas de fermeture de l'application */
    private final SharedPreferences pref;

    /** Cache RAM des messages reçus */
    private final List<Message> listMessageReceived = new ArrayList<>(30);
    /** Cache RAM des messages écrits */
    private final List<Message> listMessageWrited = new ArrayList<>(50);
    /** Cache RAM des messages propagés */
    private final List<Message> listMessageSpread = new ArrayList<>(50);

    /**
     * Constructeur.
     *
     * @param appContext
     *         contexte de l'application
     * @param versionCode
     *         version actuelle de l'application
     */
    public MessageCache(final Context appContext, final int versionCode) {
        pref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Réinitialiser le cache en cas de changement de version
        if (pref.getInt(APP_VERSION, 0) != versionCode) {
            final SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.putInt(APP_VERSION, versionCode);
            editor.apply();
        }

        // Initialisation du cache RAM avec les données du cache SD
        readInCacheSD(listMessageReceived, MessageType.RECEIVED);
        Collections.sort(listMessageReceived, new MessageComparator(MessageComparator.CompareMode.DATE_RECEPTION));
        readInCacheSD(listMessageWrited, MessageType.WRITED);
        Collections.sort(listMessageWrited, new MessageComparator(MessageComparator.CompareMode.DATE_CREATION));
        readInCacheSD(listMessageSpread, MessageType.SPREAD);
        Collections.sort(listMessageSpread, new MessageComparator(MessageComparator.CompareMode.DATE_SPREAD));
    }

    /**
     * Demande de destruction du cache.
     */
    public synchronized void clear() {
        // Vidage cache RAM
        listMessageReceived.clear();
        listMessageWrited.clear();
        listMessageSpread.clear();

        // Vidage du cache SD
        final SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt(APP_VERSION, USpreadItApplication.getInstance().getAppVersionCode());
        editor.apply();
    }

    /**
     * Synchronisation des messages reçus : ajout des nouveau messages et mise à jour des attributs dynamique des messages déjà présents.<br>
     * Un tri est effectué pour conserver l'ordre attendu.
     *
     * @param messages
     *         Liste des messages reçus
     */
    public void syncMessagesReceived(final List<Message> messages) {
        if (!messages.isEmpty()) {
            synchronized (this) {
                syncMessages(messages, listMessageReceived, MessageType.RECEIVED);
                Collections.sort(listMessageReceived, new MessageComparator(MessageComparator.CompareMode.DATE_RECEPTION));
                // Ecriture en cache SD
                writeInCacheSD(listMessageReceived, MessageType.RECEIVED);
            }
        }
    }

    /**
     * Synchronisation des messages écrits : ajout des nouveau messages et mise à jour des attributs dynamique des messages déjà présents.<br>
     * Un tri est effectué pour conserver l'ordre attendu.
     *
     * @param messages
     *         Liste des messages écrits
     */
    public void syncMessagesWrited(final List<Message> messages) {
        if (!messages.isEmpty()) {
            synchronized (this) {
                syncMessages(messages, listMessageWrited, MessageType.WRITED);
                Collections.sort(listMessageWrited, new MessageComparator(MessageComparator.CompareMode.DATE_CREATION));
                // Ecriture en cache SD
                writeInCacheSD(listMessageWrited, MessageType.WRITED);
            }
        }
    }

    /**
     * Synchronisation des messages propagés : ajout des nouveau messages et mise à jour des attributs dynamique des messages déjà présents.<br>
     * Un tri est effectué pour conserver l'ordre attendu.
     *
     * @param messages
     *         Liste des messages propagés
     */
    public void syncMessagesSpread(final List<Message> messages) {
        if (!messages.isEmpty()) {
            synchronized (this) {
                syncMessages(messages, listMessageSpread, MessageType.SPREAD);
                Collections.sort(listMessageSpread, new MessageComparator(MessageComparator.CompareMode.DATE_SPREAD));
                // Ecriture en cache SD
                writeInCacheSD(listMessageSpread, MessageType.SPREAD);
            }
        }
    }

    /**
     * Synchronisation des messages : ajout des nouveau messages et mise à jour des attributs dynamique des messages déjà présents
     *
     * @param messages
     *         Les messages à synchroniser
     * @param listMessageCached
     *         Les messages existant en cache
     */
    private void syncMessages(final List<Message> messages, final List<Message> listMessageCached, final MessageType type) {
        if (!messages.isEmpty()) {
            int idxFound;
            for (Message message : messages) {
                idxFound = listMessageCached.indexOf(message);

                // Cas de MAJ d'un message
                if (idxFound != -1) {
                    listMessageCached.get(idxFound).updateDynamicValue(message);
                }
                // Cas d'ajout d'un message
                else if (!MessageType.UPDATE.equals(message.getMessageType())) {
                    listMessageCached.add(message);
                    if (message.getBackgroundType() == BackgroundType.IMAGE && message.getBackgroundImage() != null) {
                        // Une nouvelle image doit être ajouté au cache
                        USpreadItApplication.getInstance().getImageCache().addImageToCache(message.getId(), message.getBackgroundImage(), false);
                        message.setBackgroundImage(null);
                    }
                }
            }
        }
    }

    /**
     * Recherche d'un message dans les cache
     *
     * @param messageId
     *         id du message
     * @return le message ou null si non trouvé
     */
    public synchronized Message getMessage(final long messageId) {
        Message message = null;
        for (Message msg : listMessageWrited) {
            if (msg.getId() == messageId) {
                message = msg;
                break;
            }
        }
        for (Message msg : listMessageSpread) {
            if (msg.getId() == messageId) {
                message = msg;
                break;
            }
        }
        for (Message msg : listMessageReceived) {
            if (msg.getId() == messageId) {
                message = msg;
                break;
            }
        }
        return message;
    }

    /**
     * Suppresion d'un message du cache (et du cache des images si spécifié : si le message n'est pas juste transféré de liste)
     *
     * @param message
     *         le message à supprimer
     * @param alsoImageInCacheSD
     *         Indique si l'image doit aussi être retiré du cache SD
     */
    public synchronized void deleteMessage(final Message message, final boolean alsoImageInCacheSD) {
        boolean removed = false;
        removed = listMessageWrited.remove(message);
        if (alsoImageInCacheSD && message.getBackgroundType() == BackgroundType.IMAGE) {
            // On supprime aussi du cache des images
            USpreadItApplication.getInstance().getImageCache().removeImageFromCache(message.getId());
        }
        if (removed) {
            writeInCacheSD(listMessageWrited, MessageType.WRITED);
        }
        removed = listMessageSpread.remove(message);
        if (removed) {
            writeInCacheSD(listMessageSpread, MessageType.SPREAD);
        }
        removed = listMessageReceived.remove(message);
        if (removed) {
            writeInCacheSD(listMessageReceived, MessageType.RECEIVED);
        }
    }

    /**
     * Lecture du cache SD dans la liste en paramètre
     *
     * @param listMessageCached
     *         cache RAM à alimenter
     * @param type
     *         type de message
     */
    private void readInCacheSD(final List<Message> listMessageCached, final MessageType type) {
        Set<String> listMessage = pref.getStringSet(type.name(), new HashSet<String>());
        for (String message : listMessage) {
            try {
                listMessageCached.add(new Message(new JSONObject(message), null));
            } catch (USpreadItException | JSONException e) {
                e.printStackTrace();
            }
        }
        MessageUtils.detectLinks(listMessageCached);
    }

    /**
     * Ecriture en cache SD du cache RAM demandé
     *
     * @param listMessageCached
     *         cache RAM
     * @param type
     *         type du cache
     */
    private void writeInCacheSD(final List<Message> listMessageCached, final MessageType type) {
        final Set<String> listJsonMsg = new HashSet<String>((int) (listMessageCached.size() * 1.5));
        final int nbInCache = MessageType.RECEIVED.equals(type) ? NB_MESSAGE_LIST_IN_CACHE_SD : NB_MESSAGE_GRID_IN_CACHE_SD;
        for (int i = 0; i < listMessageCached.size() && i < nbInCache; i++) {
            try {
                listJsonMsg.add(listMessageCached.get(i).toJSONMessageCache().toString());
            } catch (USpreadItException e) {
                e.printStackTrace();
            }
        }
        final SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(type.name(), listJsonMsg);
        editor.apply();
    }

    /**
     * @return #listMessageReceived
     */
    public synchronized List<Message> getListMessageReceived() {
        return listMessageReceived;
    }

    /**
     * @return #listMessageWrited
     */
    public synchronized List<Message> getListMessageWrited() {
        return listMessageWrited;
    }

    /**
     * @return #listMessageSpread
     */
    public synchronized List<Message> getListMessageSpread() {
        return listMessageSpread;
    }

    /**
     * @return La date de réception la plus récente des messages reçus par l'utilisateur
     */
    public synchronized Date getLatestDateReceptionOfMessagesReceived() {
        if (listMessageReceived.size() == 0) {
            return null;
        }
        return listMessageReceived.get(0).getDateReception();
    }

    /**
     * @return La date de réception la plus ancienne des messages reçus par l'utilisateur
     */
    public synchronized Date getOldestDateReceptionOfMessagesReceived() {
        if (listMessageReceived.size() == 0) {
            return null;
        }
        return listMessageReceived.get(listMessageReceived.size() - 1).getDateReception();
    }

    /**
     * @return La date de création la plus récente des messages écrits par l'utilisateur
     */
    public synchronized Date getLatestDateCreationOfMessagesWrited() {
        if (listMessageWrited.size() == 0) {
            return null;
        }
        return listMessageWrited.get(0).getDateCreation();
    }

    /**
     * @return La date de création la plus ancienne des messages écrits par l'utilisateur
     */
    public synchronized Date getOldestDateCreationOfMessagesWrited() {
        if (listMessageWrited.size() == 0) {
            return null;
        }
        return listMessageWrited.get(listMessageWrited.size() - 1).getDateCreation();
    }

    /**
     * @return La date de propagation la plus récente des messages propagé par l'utilisateur
     */
    public synchronized Date getLatestDateSpreadOfMessagesSpread() {
        if (listMessageSpread.size() == 0) {
            return null;
        }
        return listMessageSpread.get(0).getDateSpread();
    }

    /**
     * @return La date de propagation la plus ancienne des messages propagé par l'utilisateur
     */
    public synchronized Date getOldestDateSpreadOfMessagesSpread() {
        if (listMessageSpread.size() == 0) {
            return null;
        }
        return listMessageSpread.get(listMessageSpread.size() - 1).getDateSpread();
    }
}
