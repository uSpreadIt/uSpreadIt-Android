package it.uspread.android.message;

import java.util.Comparator;

import it.uspread.android.data.Message;

/**
 * Comparator des messages
 *
 * @author Lone Décosterd,
 */
public class MessageComparator implements Comparator<Message> {

    /** Mode de comparaison utilisé */
    private final CompareMode mode;

    /**
     * Constructeur
     *
     * @param mode
     *         {@link #mode}
     */
    public MessageComparator(final CompareMode mode) {
        this.mode = mode;
    }

    @Override
    public int compare(final Message msg1, final Message msg2) {
        if (CompareMode.DATE_CREATION.equals(mode)) {
            return msg2.getDateCreation().compareTo(msg1.getDateCreation());
        } else if (CompareMode.DATE_RECEPTION.equals(mode)) {
            return msg2.getDateReception().compareTo(msg1.getDateReception());
        } else if (CompareMode.DATE_SPREAD.equals(mode)) {
            return msg2.getDateSpread().compareTo(msg1.getDateSpread());
        }
        return 0;
    }

    /**
     * Mode de tri disponibles
     */
    public enum CompareMode {
        /** Date de création plus récentes en premier */
        DATE_CREATION,
        /** Date de réceptions plus récentes en premier */
        DATE_RECEPTION,
        /** Date de propagation plus récentes en premier */
        DATE_SPREAD
    }
}
