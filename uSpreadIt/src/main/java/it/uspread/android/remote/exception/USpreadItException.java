package it.uspread.android.remote.exception;

import android.content.res.Resources;

import it.uspread.android.R;

/**
 * Exception utilisé pour encapsuler tout types de problèmes survenant lors d'une communication avec le serveur.<br>
 *
 * @author Lone Décosterd,
 */
public class USpreadItException extends Exception {

    /**
     * Les types d'exception pouvant survenir
     */
    public static enum Type {
        AUTHENTICATION_ERROR(R.string.exception_authenticationError),
        AUTHENTICATION_REQUIRED(R.string.exception_authenticationRequired),
        FORBIDDEN(R.string.exception_forbidden),
        COMMUNICATION_UNREACHABLE(R.string.exception_communicationUnreachable),
        COMMUNICATION_UNAVAILABLE(R.string.exception_communicationUnavailable),
        COMMUNICATION_TIMEOUT(R.string.exception_communicationTimeout),
        INTERNET(R.string.exception_internet),
        QUOTA(R.string.exception_quota),
        OTHERS(R.string.exception_others);

        /** Id du message associé au type d'exception */
        private final int id;

        /**
         * Constructeur
         *
         * @param id
         *         Id du message associé au type d'exception
         */
        private Type(int id) {
            this.id = id;
        }

        /**
         * Message associé
         *
         * @param resources
         *         pour l'accès à une ressource de l'application
         * @return Le message
         */
        public String getMessage(final Resources resources) {
            return resources.getString(id);
        }
    }

    /** Le type de l'exception */
    private final Type type;

    /**
     * Constructeur pour une exception dont le message sera le nom de l'enum (a utiliser lorsque aucun message particulier a indiquer)
     *
     * @param type
     *         {@link #type} non null
     */
    public USpreadItException(final Type type) {
        this(type, null);
    }

    /**
     * Constructeur pour une exception dont on spécifie le message
     *
     * @param type
     *         {@link #type} non null
     * @param detailledMessage
     *         message ou null
     */
    public USpreadItException(final Type type, String detailledMessage) {
        this(type, detailledMessage, null);
    }

    /**
     * Constructeur pour une exception dont on spécifie le message
     *
     * @param type
     *         {@link #type} non null
     * @param detailledMessage
     *         message ou null
     * @param throwable
     *         exception originale éventuelle
     */
    public USpreadItException(final Type type, String detailledMessage, final Throwable throwable) {
        super(detailledMessage != null ? detailledMessage : type.name(), throwable);
        this.type = type;
    }

    /**
     * @return {@link #type}
     */
    public Type getType() {
        return type;
    }

    /**
     * Le message pour l'utilisateur associé à l'exception
     *
     * @param resources
     *         accès à une ressource de l'application
     * @return Un court message décrivant le problème
     */
    public String getUserMessage(final Resources resources) {
        return type.getMessage(resources);
    }
}
