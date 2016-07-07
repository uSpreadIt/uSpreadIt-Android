package it.uspread.android.activity.misc;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;

/**
 * Regroupe des méthodes utilitaires qui appliquent du look à l'application
 *
 * @author Lone Décosterd,
 */
public class ApplicationStyle {

    /**
     * Applique les couleurs à utiliser par ce composant de refresh
     *
     * @param swipeLayout
     *         layout
     */
    public static void applySwipeLayoutStyle(final SwipeRefreshLayout swipeLayout) {
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    /**
     * Permet d'obtenir la taille de l'actionBar.
     *
     * @param activity
     *         activité
     * @return hauteur de l'actionBar
     */
    public static int getActionBarSize(final Activity activity) {
        final TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typedValue, true);
        return activity.getResources().getDimensionPixelSize(typedValue.resourceId);
    }
}
