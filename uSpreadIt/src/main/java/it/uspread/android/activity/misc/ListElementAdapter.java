package it.uspread.android.activity.misc;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Ajout d'une référence de la liste au ArrayAdapter.
 *
 * @author Lone Décosterd,
 */
public abstract class ListElementAdapter<T> extends ArrayAdapter<T> {

    /** Liste des éléments manipulé par l'adapter */
    private final List<T> listElement;

    /**
     * Constructeur.
     *
     * @param context
     *         Contexte
     * @param resource
     *         The resource ID for a layout file containing a TextView to use when
     *         instantiating views.
     * @param listElement
     *         {@link #listElement}
     */
    public ListElementAdapter(final Context context, final int resource, final List<T> listElement) {
        super(context, resource, listElement);
        this.listElement = listElement;
    }

    /**
     * @return {@link #listElement}
     */
    public List<T> getListElement() {
        return listElement;
    }

}
