package it.uspread.android.activity.misc;

import android.view.View;

/**
 * Callback permettant typiquement de réagir aux click des boutons inclus dans une liste ou grille. (Il s'agit d'un problème qui ne peux être résolu avec la simple utilisation de
 * {@link android.widget.AdapterView.OnItemClickListener}) car la présence du bouton intercepte l'événement de l'adapter d'une part, mais surtout il faut bien savoir quel boutons
 * a
 * été cliqué quand il y en as plusieurs..
 *
 * @author Lone Décosterd,
 */
public interface OnViewInListClickListener {

    /**
     * Appelé lors du click sur un bouton faisant partis d'un élément rendu dans une {@link android.widget.ListView} ou {@link android.widget.GridView}...
     *
     * @param view
     *         la vue (boutons...) dans la liste source du click
     * @param position
     *         la position de l'élément dans la liste
     */
    void onViewInListClick(final View view, final int position);
}
