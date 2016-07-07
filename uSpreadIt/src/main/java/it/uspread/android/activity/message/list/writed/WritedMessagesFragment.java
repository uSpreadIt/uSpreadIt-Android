package it.uspread.android.activity.message.list.writed;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.USpreadItFragment;
import it.uspread.android.activity.message.view.MessageViewerActivity;
import it.uspread.android.activity.misc.ApplicationStyle;
import it.uspread.android.activity.misc.OnLoadMoreScrollListener;
import it.uspread.android.data.Message;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskDeleteMessage;
import it.uspread.android.task.TaskListWritedMessages;
import it.uspread.android.task.TaskLoadImageCacheOrWeb;
import it.uspread.android.task.TaskSendMessage;

/**
 * Fragment
 * <ul>
 * <li>Consultation des messages écrit par l'utilisateur</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class WritedMessagesFragment extends USpreadItFragment {

    /** Indique le nombre de messages à charger */
    private final int NB_MESSAGE_PAGINATION = 20;

    /** Grille d'affichage */
    private GridView gridView;

    /** Scroll listener */
    private OnLoadMoreScrollListener onLoadMoreScrollListener;

    /** Modèle des éléments à afficher */
    private WritedMessagesAdapter listAdapter;

    /** Composant de refresh de la grille lorsque non vide */
    private SwipeRefreshLayout swipeLayoutGrid;

    /** Composant de refresh de la grille lorsque vide */
    private SwipeRefreshLayout swipeLayoutEmpty;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages_writed, container, false);

        // ############################## Confivuration du layout liste ##############################
        gridView = (GridView) view.findViewById(R.id.grid_messages_writed);
        listAdapter = new WritedMessagesAdapter(getActivity());
        gridView.setAdapter(listAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                final Intent intent = new Intent(getActivity(), MessageViewerActivity.class);
                intent.putExtra(Message.class.getName(), ((Message) parent.getItemAtPosition(position)).getId());
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, MessageViewerActivity.TRANSITION_NAME);
                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });

        // ############################## Configuration des composant permettant de faire du pull down to refresh. ##############################
        // Il y en a deux en raison de l'existance d'un composant spécial affiché lorsqu'aucun élément dans la liste
        swipeLayoutGrid = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_messages);
        swipeLayoutGrid.setVisibility(View.GONE);
        // Sur un refresh manuel de la liste non vide : On veut charger les nouveaux messages plus récents et mettre à jour les anciens
        swipeLayoutGrid.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Demander de mettre à jour toutes les valeurs dynamique de nos messages
                loadingWritedMessages(new MessageCriteria(true, USpreadItApplication.getInstance().getMessageCache().getOldestDateCreationOfMessagesWrited(), MessageCriteria.VAL_AFTER_OR_EQUALS_DATE));

                // Demander un chargement des messages plus récents que le message le plus récent en notre possesion
                loadingWritedMessages(new MessageCriteria(USpreadItApplication.getInstance().getMessageCache().getLatestDateCreationOfMessagesWrited(), MessageCriteria.VAL_AFTER_DATE));
            }
        });
        ApplicationStyle.applySwipeLayoutStyle(swipeLayoutGrid);

        swipeLayoutEmpty = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_no_messages);
        swipeLayoutEmpty.setVisibility(View.VISIBLE);
        // Sur un refresh manuel de la liste vide : On veut charger les NB_MESSAGE_PAGINATION premiers messages de cette liste
        swipeLayoutEmpty.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingWritedMessages(new MessageCriteria(NB_MESSAGE_PAGINATION));
            }
        });
        ApplicationStyle.applySwipeLayoutStyle(swipeLayoutEmpty);

        // Configuration de l'affichage du bon composant suivant que la liste soit vide ou pas.
        listAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if (listAdapter.isEmpty()) {
                    swipeLayoutGrid.setVisibility(View.GONE);
                    swipeLayoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    swipeLayoutEmpty.setVisibility(View.GONE);
                    swipeLayoutGrid.setVisibility(View.VISIBLE);
                }
            }
        });

        // ############################## Configuration de la pagination ##############################
        onLoadMoreScrollListener = new OnLoadMoreScrollListener() {
            @Override
            public void loadMore() {
                swipeLayoutGrid.setRefreshing(true);
                new TaskListWritedMessages(TaskListWritedMessages.RESULT_CODE_LOAD_MORE, false).execute(new MessageCriteria(NB_MESSAGE_PAGINATION, USpreadItApplication.getInstance().getMessageCache().getOldestDateCreationOfMessagesWrited(), MessageCriteria.VAL_BEFORE_DATE));
            }
        };
        if (savedInstanceState != null && savedInstanceState.containsKey(OnLoadMoreScrollListener.CONTEXT_DATA_REMAINING)) {
            onLoadMoreScrollListener.setDataRemaining(savedInstanceState.getBoolean(OnLoadMoreScrollListener.CONTEXT_DATA_REMAINING));
        }
        gridView.setOnScrollListener(onLoadMoreScrollListener);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Recherche des messages en cache RAM
        final List<Message> listMessage = USpreadItApplication.getInstance().getMessageCache().getListMessageWrited();

        if (!listMessage.isEmpty()) {
            listAdapter.addAll(listMessage);
        }
        // Si aucun message n'est trouvé en cache : On veut charger les NB_MESSAGE_PAGINATION premiers messages de cette liste
        else {
            // Lancement de l'indicateur de chargement
            swipeLayoutEmpty.setProgressViewOffset(false, 0, ApplicationStyle.getActionBarSize(getActivity()));
            swipeLayoutEmpty.setRefreshing(true);
            // Chargement des NB_MESSAGE_PAGINATION premiers messages
            loadingWritedMessages(new MessageCriteria(NB_MESSAGE_PAGINATION));
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(OnLoadMoreScrollListener.CONTEXT_DATA_REMAINING, onLoadMoreScrollListener.isDataRemaining());
    }

    /**
     * Chargement des message de l'utilisateur.
     */
    private void loadingWritedMessages(final MessageCriteria criteria) {
        new TaskListWritedMessages(TaskListWritedMessages.RESULT_CODE_GENERAL, criteria != null && criteria.isOnlyDynamicValue()).execute(criteria != null ? criteria : new MessageCriteria());
    }

    /**
     * Rafraichi la liste pour être synchrone avec le cache
     */
    public void refreshList() {
        listAdapter.getListElement().clear();
        listAdapter.getListElement().addAll(USpreadItApplication.getInstance().getMessageCache().getListMessageWrited());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
        if (taskResult.success) {
            if (taskResult.resultCode == TaskListWritedMessages.RESULT_CODE_LOAD_MORE) {
                final List<Message> listMessage = (List<Message>) taskResult.resultData;
                if (listMessage.size() < NB_MESSAGE_PAGINATION) {
                    onLoadMoreScrollListener.setDataRemaining(false);
                }
            }

            if (taskResult.resultCode == TaskListWritedMessages.RESULT_CODE_GENERAL || taskResult.resultCode == TaskListWritedMessages.RESULT_CODE_LOAD_MORE) {
                refreshList();
            } else if (taskResult.resultCode == TaskDeleteMessage.RESULT_CODE) {
                final Message message = (Message) taskResult.resultData;
                listAdapter.remove(message);
            } else if (taskResult.resultCode == TaskSendMessage.RESULT_CODE) {
                final Message message = (Message) taskResult.resultData;
                listAdapter.insert(message, 0);
            } else if (taskResult.resultCode == TaskLoadImageCacheOrWeb.RESULT_CODE) {
                listAdapter.notifyDataSetChanged();
            }
        }

        if (taskResult.resultCode == TaskListWritedMessages.RESULT_CODE_GENERAL) {
            swipeLayoutEmpty.setRefreshing(false);
            swipeLayoutGrid.setRefreshing(false);
        } else if (taskResult.resultCode == TaskListWritedMessages.RESULT_CODE_LOAD_MORE) {
            onLoadMoreScrollListener.setLoading(false);
            swipeLayoutGrid.setRefreshing(false);
        }
    }
}
