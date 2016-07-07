package it.uspread.android.data.criteria;

/**
 * Classe encapsulant les critère de recherche pour un {@link it.uspread.android.data.Status}
 *
 * @author Lone Décosterd,
 */
public class StatusCriteria implements Criteria {

    /** Indique de ne retourner que l'information de quota */
    private boolean quotaOnly;

    /**
     * Constructeur
     *
     * @param quotaOnly
     *         {@link #quotaOnly}
     */
    public StatusCriteria(final boolean quotaOnly) {
        this.quotaOnly = quotaOnly;
    }

    /**
     * @return {@link #quotaOnly}
     */
    public boolean isQuotaOnly() {
        return quotaOnly;
    }

    /**
     * @param quotaOnly
     *         {@link #quotaOnly}
     */
    public void setQuotaOnly(final boolean quotaOnly) {
        this.quotaOnly = quotaOnly;
    }

    @Override
    public boolean isEmpty() {
        return !quotaOnly;
    }

    @Override
    public String addCriteriaToUrl(final String url) {
        if (isEmpty()) {
            return url;
        }
        final StringBuilder str = new StringBuilder(20);

        // Ajout de la déclaration de paramètre si pas déjà présent
        if (!url.contains("?")) {
            str.append("?");
        } else if (!"?".equals(url.substring(url.length() - 2))) {
            str.append("&");
        }

        if (quotaOnly) {
            str.append("onlyQuota=").append(VAL_TRUE);
            str.append("&");
        }

        // On enlève le & en trop
        if (!isEmpty()) {
            str.setLength(str.length() - 1);
        }
        return url + str.toString();
    }
}
