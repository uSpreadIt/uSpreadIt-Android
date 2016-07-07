package it.uspread.android.activity.misc;

import android.widget.AbsListView;

/**
 * Scroll Listener permettant d'intégrer une possiblité de demander de charger les éléments suivants lorsque le bas de liste est atteint. (Ne fait rien si la liste est vide)
 *
 * @author Lone Décosterd,
 */
public abstract class OnLoadMoreScrollListener implements AbsListView.OnScrollListener {

    /** Clé de l'objet sauvegardé dans le contexte */
    public static final String CONTEXT_DATA_REMAINING = "data_remaining";

    /** Indique que le chargement de données supplémentaires est en cours */
    private boolean isLoading = false;

    /** Indique s'il reste des données à charger */
    private boolean isDataRemaining = true;

    /**
     * Constructeur
     */
    public OnLoadMoreScrollListener() {
    }

    /**
     * @return {@link #isLoading}
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * @param isLoading
     *         {@link #isLoading}
     */
    public void setLoading(final boolean isLoading) {
        this.isLoading = isLoading;
    }

    /**
     * @return {@link #isDataRemaining}
     */
    public boolean isDataRemaining() {
        return isDataRemaining;
    }

    /**
     * @param isDataRemaining
     *         {@link #isDataRemaining}
     */
    public void setDataRemaining(final boolean isDataRemaining) {
        this.isDataRemaining = isDataRemaining;
    }

    /**
     * Demande de chargement d'éléments supplémentaire.<br>
     * Doit indiquer quand les données ont éffectivement été chargé et lorsque plus de donné n'est disponible.
     * Est jamais appelé si la liste est vide
     */
    public abstract void loadMore();

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        // Si il y a des éléments dans la liste et si le dernier éléments est atteint on lance le chargement de données supplémentaire
        if (!isLoading && isDataRemaining && totalItemCount > 0 && (firstVisibleItem + visibleItemCount >= totalItemCount)) {
            isLoading = true;
            loadMore();
        }
    }
}