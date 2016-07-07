package it.uspread.android.activity.message.list.writed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import it.uspread.android.R;
import it.uspread.android.activity.misc.ListElementAdapter;
import it.uspread.android.data.Message;
import it.uspread.android.message.MessageUtils;
import it.uspread.android.message.MessageView;

/**
 * Gestion de l'affichage des éléments de la liste des message écrit par l'utilisateur.
 *
 * @author Lone Décosterd,
 */
public class WritedMessagesAdapter extends ListElementAdapter<Message> {

    /**
     * Constructeur.
     *
     * @param context
     *         Contexte
     */
    public WritedMessagesAdapter(final Context context) {
        super(context, R.layout.message, new ArrayList<Message>());
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        // Optimisation : création d'une nouvelle vue que si nécessaire
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message, parent, false);
            holder = new ViewHolder();
            // On place les widgets de notre layout dans le holder
            holder.messageView = (MessageView) convertView.findViewById(R.id.message_render);
            holder.textNbDiffuse = (TextView) convertView.findViewById(R.id.text_nbdiffuse);

            // On insère le holder en tant que tag dans le layout
            convertView.setTag(holder);
        } else {
            // Si on recycle la vue, on récupère son holder en tag
            holder = (ViewHolder) convertView.getTag();
        }

        final Message message = getItem(position);
        holder.messageView.setMessage(message);
        holder.textNbDiffuse.setText(MessageUtils.convertToShortNbSpread(message.getNbSpread()));

        return convertView;
    }

    /**
     * Pour l'optimisation de l'affichage de la vue. Les widget du layout dont le contenu change suivant l'élément doivent être ajouté dans cette classe interne.<br>
     * (Raison lors du scroll d'une liste les layout et widget des éléments qui disparaissent sont systématiquement recyclé pour l'affichage des nouveaux éléments apparaissant)
     */
    private static class ViewHolder {
        public MessageView messageView;
        public TextView textNbDiffuse;
    }
}
