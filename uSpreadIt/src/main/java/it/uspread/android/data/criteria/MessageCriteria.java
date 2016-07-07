package it.uspread.android.data.criteria;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import it.uspread.android.remote.utils.HttpUtils;

/**
 * Classe encapsulant les critère de recherche pour un {@link it.uspread.android.data.Message}
 *
 * @author Lone Décosterd,
 */
public class MessageCriteria implements Criteria {

    /** Indique plus récent qu'une date */
    public static final String VAL_AFTER_DATE = "gt";
    /** Indique plus anciens qu'une date */
    public static final String VAL_AFTER_OR_EQUALS_DATE = "ge";
    /** Indique plus anciens qu'une date */
    public static final String VAL_BEFORE_DATE = "lt";
    /** Formattage utilisé pour les dates en parametres d'URL */
    private static final SimpleDateFormat URL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSSZ");
    /** Indique le nombre de messages à retourner */
    private long count;
    /** Indique une date sur laquelle se baser pour retourner les messages : utilisation conjointe avec {@link #operator} */
    private Date date;
    /** Indique l'opérateur utilisé pour effectuer la comparaison de dates :  utilisation conjointe avec {@link #date} */
    private String operator;
    /** Indique de ne retourner que les valeurs dynamique des messages */
    private boolean onlyDynamicValue;

    /**
     * Constructeur.
     */
    public MessageCriteria() {
    }

    /**
     * Constructeur.
     *
     * @param count
     *         {@link #count}
     */
    public MessageCriteria(final long count) {
        this.count = count;
    }

    /**
     * Constructeur.
     *
     * @param date
     *         {@link #date}
     * @param operator
     *         {@link #operator}
     */
    public MessageCriteria(final Date date, final String operator) {
        this.date = date;
        this.operator = operator;
    }

    /**
     * Constructeur.
     *
     * @param count
     *         {@link #count}
     * @param date
     *         {@link #date}
     * @param operator
     *         {@link #operator}
     */
    public MessageCriteria(final long count, final Date date, final String operator) {
        this.count = count;
        this.date = date;
        this.operator = operator;
    }


    /**
     * Constructeur.
     *
     * @param date
     *         {@link #date}
     * @param operator
     *         {@link #operator}
     * @param onlyDynamicValue
     *         {@link #onlyDynamicValue}
     */
    public MessageCriteria(final boolean onlyDynamicValue, final Date date, final String operator) {
        this.onlyDynamicValue = onlyDynamicValue;
        this.date = date;
        this.operator = operator;
    }

    /**
     * @return {@link #count}
     */
    public long getCount() {
        return count;
    }

    /**
     * @param count
     *         {@link #count}
     */
    public void setCount(final long count) {
        this.count = count;
    }

    /**
     * @return {@link #date}
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date
     *         {@link #count}
     */
    public void setDate(final Date date) {
        this.date = date;
    }

    /**
     * @return {@link #operator}
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator
     *         {@link #operator}
     */
    public void setOperator(final String operator) {
        this.operator = operator;
    }

    /**
     * @return {@link #onlyDynamicValue}
     */
    public boolean isOnlyDynamicValue() {
        return onlyDynamicValue;
    }

    /**
     * @param onlyDynamicValue
     *         {@link #onlyDynamicValue}
     */
    public void setOnlyDynamicValue(final boolean onlyDynamicValue) {
        this.onlyDynamicValue = onlyDynamicValue;
    }

    @Override
    public boolean isEmpty() {
        return count < 1 && date == null && operator == null && !onlyDynamicValue;
    }

    @Override
    public String addCriteriaToUrl(final String url) {
        if (isEmpty()) {
            return url;
        }
        final StringBuilder str = new StringBuilder(50);

        try {
            // Ajout de la déclaration de paramètre si pas déjà présent
            if (!url.contains("?")) {
                str.append("?");
            } else if (!"?".equals(url.substring(url.length() - 2))) {
                str.append("&");
            }

            if (count > 0) {
                str.append("nb=").append(count);
                str.append("&");
            }
            if (date != null) {
                str.append("date=").append(URLEncoder.encode(URL_DATE_FORMAT.format(date), HttpUtils.UTF8));
                str.append("&");
            }
            if (operator != null) {
                str.append("op=").append(operator);
                str.append("&");
            }
            if (onlyDynamicValue) {
                str.append("onlyDyn=").append(VAL_TRUE);
                str.append("&");
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("MessageCriteria", e.getMessage());
        }

        // On enlève le & en trop
        if (!isEmpty()) {
            str.setLength(str.length() - 1);
        }
        return url + str.toString();
    }
}
