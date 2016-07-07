package it.uspread.android.data.criteria;

/**
 * Interface pour les critères de recherche
 *
 * @author Lone Décosterd,
 */
public interface Criteria {

    /** Valeur pour indiquer vrai */
    public static final String VAL_TRUE = "true";

    /**
     * Indique si des critères sont renseigné
     *
     * @return true s'il y a des critères
     */
    public boolean isEmpty();

    /**
     * Ajoute les critères à l'URL s'il y a lieu d'en ajouter
     *
     * @param url
     *         Une URL sans parametres ou ayant déjà des paramétres
     * @return L'URL enrichi des parametres
     */
    public String addCriteriaToUrl(final String url);

}
