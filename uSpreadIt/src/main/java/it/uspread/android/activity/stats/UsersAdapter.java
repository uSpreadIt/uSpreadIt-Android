package it.uspread.android.activity.stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import it.uspread.android.R;
import it.uspread.android.data.UserRanking;

/**
 * Gestion de l'affichage des éléments de la liste des utilisateurs.
 *
 * @author Lone Décosterd,
 */
public class UsersAdapter extends ArrayAdapter<UserRanking> {
    /**
     * Constructeur.
     *
     * @param context
     *         Contexte
     * @param listUser
     *         Les utilisateurs à afficher
     */
    public UsersAdapter(Context context, List<UserRanking> listUser) {
        super(context, R.layout.score_user_rank, listUser);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // Optimisation : création d'une nouvelle vue que si nécessaire
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.score_user_rank, parent, false);
            holder = new ViewHolder();
            // On place les widgets de notre layout dans le holder
            holder.textRanking = (TextView) convertView.findViewById(R.id.text_username);
            holder.textUserName = (TextView) convertView.findViewById(R.id.text_ranking);

            // On insère le holder en tant que tag dans le layout
            convertView.setTag(holder);
        } else {
            // Si on recycle la vue, on récupère son holder en tag
            holder = (ViewHolder) convertView.getTag();
        }

        final UserRanking user = getItem(position);
        holder.textRanking.setText(user.getUsername());
        holder.textUserName.setText(((Integer) user.getRanking()).toString());

        return convertView;
    }

    /**
     * Pour l'optimisation de l'affichage de la vue. Les widget du layout doivent être ajouté dans cette classe interne.<br>
     * (Raison lors du scroll d'une liste les layout et widget des éléments qui disparaissent sont systématiquement recyclé pour l'affichage des nouveaux éléments apparaissant)
     */
    private static class ViewHolder {
        public TextView textRanking;
        public TextView textUserName;
    }
}
