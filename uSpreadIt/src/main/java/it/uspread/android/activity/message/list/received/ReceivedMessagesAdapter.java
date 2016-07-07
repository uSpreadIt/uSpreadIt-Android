package it.uspread.android.activity.message.list.received;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import it.uspread.android.R;
import it.uspread.android.activity.misc.ListElementAdapter;
import it.uspread.android.activity.misc.OnViewInListClickListener;
import it.uspread.android.data.Message;
import it.uspread.android.message.MessageView;

/**
 * Gestion de l'affichage des éléments de la liste des message reçus.
 *
 * @author Lone Décosterd,
 */
public class ReceivedMessagesAdapter extends ListElementAdapter<Message> {

    /** Liste des id des messages dont une action de traitement (ignorer, propager, etc..) est en cours */
    private final Set<Long> listIdMessageInProcess = new HashSet<>();

    /** Controleur des boutons de la liste */
    private final OnViewInListClickListener callback;

    /**
     * Constructeur.
     *
     * @param context
     *         Contexte
     * @param callback
     *         Le controleur des boutons de la liste
     */
    public ReceivedMessagesAdapter(final Context context, final OnViewInListClickListener callback) {
        super(context, R.layout.message_received, new ArrayList<Message>());
        this.callback = callback;
    }

    /**
     * Indique le lancement d'une action (ignorer, propager, etc..) pour le message donné
     *
     * @param idMessage
     *         id du message dont le traitement commence
     */
    public void startProcessIndicatorOnMessage(final long idMessage) {
        listIdMessageInProcess.add(idMessage);
    }

    /**
     * Indique la fin d'éxécution de l'action (ignorer, propager, etc..) pour le message donné
     *
     * @param idMessage
     *         id du message dont le traitement se termine
     */
    public void stopProcessIndicatorOnMessage(final long idMessage) {
        listIdMessageInProcess.remove(idMessage);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        // Optimisation : création d'une nouvelle vue que si nécessaire
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message_received, parent, false);
            holder = new ViewHolder();

            // On place les widgets de notre layout dans le holder
            holder.messageView = (MessageView) convertView.findViewById(R.id.message_render);
            // Les dimenssions demandées doivent être renseigné afin que le message s'adapte pour toujours contenir dans l'écran
            holder.messageView.getLayoutParams().width = parent.getWidth();
            holder.messageView.getLayoutParams().height = parent.getHeight();
            holder.messageView.setViewedInList(true);
            holder.link = (ImageButton) convertView.findViewById(R.id.action_hyperlink);
            holder.actionSpread = (ImageButton) convertView.findViewById(R.id.action_spread);
            holder.actionIgnore = (ImageButton) convertView.findViewById(R.id.action_ignore);
            holder.actionMore = (ImageButton) convertView.findViewById(R.id.action_moreAction);
            holder.waitingActionComplete = (ProgressBar) convertView.findViewById(R.id.message_waitingActionComplete);

            // On insère le holder en tant que tag dans le layout
            convertView.setTag(holder);
        } else {
            // Si on recycle la vue, on récupère son holder en tag
            holder = (ViewHolder) convertView.getTag();
        }

        // Redirection des click des boutons vers le controlleur
        holder.link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onViewInListClick(holder.link, position);
            }
        });
        holder.actionSpread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onViewInListClick(holder.actionSpread, position);
            }
        });
        holder.actionIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onViewInListClick(holder.actionIgnore, position);
            }
        });
        holder.actionMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onViewInListClick(holder.actionMore, position);
            }
        });

        final Message message = getItem(position);
        holder.messageView.setMessage(message);
        holder.link.setVisibility(message.hasLink() ? View.VISIBLE : View.GONE);

        // Définitions de la visibilité des boutons d'action ou bien d'un indicateur d'attente si une action a été lancé
        holder.actionSpread.setVisibility(!listIdMessageInProcess.contains(message.getId()) ? View.VISIBLE : View.GONE);
        holder.actionIgnore.setVisibility(!listIdMessageInProcess.contains(message.getId()) ? View.VISIBLE : View.GONE);
        holder.actionMore.setVisibility(!listIdMessageInProcess.contains(message.getId()) ? View.VISIBLE : View.GONE);
        holder.waitingActionComplete.setVisibility(listIdMessageInProcess.contains(message.getId()) ? View.VISIBLE : View.GONE);

        return convertView;
    }

    /**
     * Pour l'optimisation de l'affichage de la vue. Les widget du layout dont le contenu change suivant l'élément doivent être ajouté dans cette classe interne.<br>
     * (Raison lors du scroll d'une liste les layout et widget des éléments qui disparaissent sont systématiquement recyclé pour l'affichage des nouveaux éléments apparaissant)
     */
    private static class ViewHolder {
        public MessageView messageView;
        public ImageButton link;
        public ImageButton actionSpread;
        public ImageButton actionIgnore;
        public ImageButton actionMore;
        public ProgressBar waitingActionComplete;
    }
}
