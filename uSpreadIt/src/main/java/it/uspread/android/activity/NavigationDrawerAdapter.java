package it.uspread.android.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.uspread.android.R;

/**
 * Gestion de l'affichage des éléments du panneau de navigation.
 *
 * @author Lone Décosterd,
 */
public class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerModel> {

    /** TYPE MENU */
    private static final int ITEM_VIEW_TYPE_MENU = 0;
    /** TYPE HEADER */
    private static final int ITEM_VIEW_TYPE_HEADER = 1;
    /** Nb de type de vue affiché */
    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    /**
     * Constructeur.
     *
     * @param context
     *         Contexte
     * @param listMenu
     *         Les éléments du panneau de navigation
     */
    public NavigationDrawerAdapter(final Context context, final List<NavigationDrawerModel> listMenu) {
        super(context, R.layout.navigation_drawer_item, listMenu);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;

        // Optimisation : création d'une nouvelle vue que si nécessaire
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.navigation_drawer_item, parent, false);
            holder = new ViewHolder();

            holder.headerText = (TextView) convertView.findViewById(R.id.drawer_header_text);
            holder.itemText = (TextView) convertView.findViewById(R.id.drawer_item_text);
            holder.headerLayout = (LinearLayout) convertView.findViewById(R.id.drawer_header);
            holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.drawer_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final NavigationDrawerModel item = getItem(position);

        // Entête
        if (item.isGroupHeader()) {
            holder.headerLayout.setVisibility(LinearLayout.VISIBLE);
            holder.itemLayout.setVisibility(LinearLayout.GONE);
            holder.headerText.setText(item.getTitle());
        }
        // Menu
        else {
            holder.headerLayout.setVisibility(LinearLayout.GONE);
            holder.itemLayout.setVisibility(LinearLayout.VISIBLE);
            holder.itemText.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(item.getIcon()), null, null, null);
            holder.itemText.setText(item.getTitle());
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(final int position) {
        final NavigationDrawerModel item = getItem(position);
        return item.isGroupHeader() ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_MENU;
    }

    @Override
    public boolean isEnabled(int position) {
        return ITEM_VIEW_TYPE_MENU == getItemViewType(position);
    }

    /**
     * Pour l'optimisation de l'affichage de la vue. Les widget du layout dont le contenu change suivant l'élément doivent être ajouté dans cette classe interne.<br>
     * (Raison lors du scroll d'une liste les layout et widget des éléments qui disparaissent sont systématiquement recyclé pour l'affichage des nouveaux éléments apparaissant)
     */
    private static class ViewHolder {
        TextView headerText;
        TextView itemText;
        LinearLayout headerLayout;
        LinearLayout itemLayout;
    }
}
