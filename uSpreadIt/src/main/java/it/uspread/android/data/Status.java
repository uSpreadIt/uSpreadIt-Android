package it.uspread.android.data;

import org.json.JSONException;
import org.json.JSONObject;

import it.uspread.android.remote.exception.USpreadItException;

/**
 * Les informations de status de l'utilisateur
 *
 * @author Lone Décosterd,
 */
public class Status {

    /** Indique si le quota de création de message est atteint */
    private boolean quotaReached;

    /** Indique le nombre de messages écrits */
    private long nbMessageWrited;
    /** Indique le nombre de messages propagés */
    private long nbMessageSpread;
    /** Indique le nombre de messages ignorés */
    private long nbMessageIgnored;
    /** Indique le nombre de messages signalés */
    private long nbMessageReported;

    public Status() {
    }

    /**
     * Construction d'un Status à partir des informations fourni en JSON
     *
     * @param json
     *         status au format JSON
     */
    public Status(final JSONObject json) throws USpreadItException {
        try {
            this.quotaReached = json.getBoolean("wQuota");
            if (!json.isNull("msgWrited")) {
                this.nbMessageWrited = json.getLong("msgWrited");
            }
            if (!json.isNull("msgSpread")) {
                this.nbMessageSpread = json.getLong("msgSpread");
            }
            if (!json.isNull("msgIgnored")) {
                this.nbMessageIgnored = json.getLong("msgIgnored");
            }
            if (!json.isNull("msgReported")) {
                this.nbMessageReported = json.getLong("msgReported");
            }
        } catch (JSONException e) {
            throw new USpreadItException(USpreadItException.Type.OTHERS, e.getMessage(), e);
        }
    }

    /**
     * @return {@link #quotaReached}
     */
    public boolean isQuotaReached() {
        return quotaReached;
    }

    /**
     * @param quotaReached
     *         {@link #quotaReached}
     */
    public void setQuotaReached(final boolean quotaReached) {
        this.quotaReached = quotaReached;
    }

    /**
     * @return {@link #nbMessageWrited}
     */
    public long getNbMessageWrited() {
        return nbMessageWrited;
    }

    /**
     * @param nbMessageWrited
     *         {@link #nbMessageWrited}
     */
    public void setNbMessageWrited(final long nbMessageWrited) {
        this.nbMessageWrited = nbMessageWrited;
    }

    /**
     * @return {@link #nbMessageSpread}
     */
    public long getNbMessageSpread() {
        return nbMessageSpread;
    }

    /**
     * @param nbMessageSpread
     *         {@link #nbMessageSpread}
     */
    public void setNbMessageSpread(final long nbMessageSpread) {
        this.nbMessageSpread = nbMessageSpread;
    }

    /**
     * @return {@link #nbMessageIgnored}
     */
    public long getNbMessageIgnored() {
        return nbMessageIgnored;
    }

    /**
     * @param nbMessageIgnored
     *         {@link #nbMessageIgnored}
     */
    public void setNbMessageIgnored(final long nbMessageIgnored) {
        this.nbMessageIgnored = nbMessageIgnored;
    }

    /**
     * @return {@link #nbMessageReported}
     */
    public long getNbMessageReported() {
        return nbMessageReported;
    }

    /**
     * @param nbMessageReported
     *         {@link #nbMessageReported}
     */
    public void setNbMessageReported(final long nbMessageReported) {
        this.nbMessageReported = nbMessageReported;
    }
}
