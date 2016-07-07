package it.uspread.android.data.type;

/**
 * Type du message pour l'utilisateur.
 *
 * @author Lone Décosterd,
 */
public enum MessageType {
    /** Message reçus par l'utilisateur */
    RECEIVED,
    /** Message écrit par l'utilisateur */
    WRITED,
    /** Message propagé par l'utilisateur */
    SPREAD,
    /** Indique que c'est un contenu de maj pour un message déjà possédé */
    UPDATE
}
