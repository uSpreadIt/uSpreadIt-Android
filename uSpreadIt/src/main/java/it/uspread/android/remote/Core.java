package it.uspread.android.remote;

import java.util.List;

import it.uspread.android.data.Message;
import it.uspread.android.data.Status;
import it.uspread.android.data.User;
import it.uspread.android.data.UserRanking;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.data.criteria.StatusCriteria;
import it.uspread.android.data.type.ReportType;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Interface de communication avec le serveur.<br>
 *
 * @author Lone Décosterd,
 */
public interface Core {

    /**
     * Création du compte utilisateur.<br>
     *
     * @param user
     *         L'utilisateur à créer
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    void createUser(final User user) throws USpreadItException;

    /**
     * Login de l'utilisateur
     *
     * @param user
     *         L'utilisateur à logger
     * @param gcmRegistrationId
     *         le token d'enregistrement au service GCM si obtenu
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    void loginUser(final User user, final String gcmRegistrationId) throws USpreadItException;

    /**
     * Permet de transmettre auprès du serveur pour le périphérique de l'utilisateur connecté sa clé d'enregistrement au service GCM
     *
     * @param gcmRegistrationId
     *         Registration ID
     * @throws USpreadItException
     */
    void sendGCMRegistrationId(final String gcmRegistrationId) throws USpreadItException;

    /**
     * Permet d'obtenir les informations du compte utilisateur
     *
     * @return L'utilisateur connecté
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    User getUser() throws USpreadItException;

    /**
     * Permet d'obtenir les informations de statut de l'utilisateur
     *
     * @param criteria
     *         Les critères
     * @return Les informations de statut de l'utilisateur
     * @throws USpreadItException
     *         En cas d'échec de l'action
     */
    Status getUserStatus(final StatusCriteria criteria) throws USpreadItException;

    /**
     * Création et première propagation d'un tout nouveau message venant d'être écrit par l'utilisateur.<br>
     *
     * @param newMessage
     *         Le nouveau message à créer
     * @return Le message fourni avec les informations de création à jour
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    Message sendMessage(final Message newMessage) throws USpreadItException;

    /**
     * Propager un message.<br>
     *
     * @param message
     *         Le message à propager
     * @return Le message fourni avec les informations dynamique à jour
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    Message spreadMessage(final Message message) throws USpreadItException;

    /**
     * Ignore un message.<br>
     *
     * @param message
     *         Le message à ignorer
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    void ignoreMessage(final Message message) throws USpreadItException;

    /**
     * Signaler un message.<br>
     *
     * @param message
     *         Le message à signaler
     * @param reportType
     *         Le type de signalement à effectuer
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    void reportMessage(final Message message, final ReportType reportType) throws USpreadItException;

    /**
     * Permet d'obtenir l'image d'un message
     *
     * @param messageId
     *         L'id du message dont on souhaite obtenir l'image
     * @return Le message dont seul l'image est chargé
     * @throws USpreadItException
     *         En cas d'échec de l'action
     */
    Message getMessageImage(final long messageId) throws USpreadItException;

    /**
     * Suppression du message.<br>
     *
     * @param message
     *         Le message à supprimer
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    void deleteMessage(final Message message) throws USpreadItException;

    /**
     * Permet d'obtenir les messages reçus par l'utilisateur.<br>
     *
     * @param criteria
     *         Les critères de recherche
     * @return La liste des messages ou une liste vide si aucun message
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    List<Message> getReceivedMessage(final MessageCriteria criteria) throws USpreadItException;

    /**
     * Permet d'obtenir les messages propagé par l'utilisateur.<br>
     *
     * @param criteria
     *         Les critères de recherche
     * @return La liste des messages ou une liste vide si aucun message
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    List<Message> getSpreadMessage(final MessageCriteria criteria) throws USpreadItException;

    /**
     * Permet d'obtenir les messages écrits par l'utilisateur.<br>
     *
     * @param criteria
     *         Les critères de recherche
     * @return La liste des messages ou une liste vide si aucun message
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    List<Message> getWritedMessage(final MessageCriteria criteria) throws USpreadItException;

    /**
     * Permet d'obtenir le classement des utilisateurs.<br>
     *
     * @return La liste des classement utilisateurs ou une liste vide
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'échec de l'action
     */
    List<UserRanking> getUsersRanking() throws USpreadItException;
}
