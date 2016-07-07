package it.uspread.android.activity.message.list.received;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.List;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.USpreadItListFragment;
import it.uspread.android.activity.misc.ApplicationStyle;
import it.uspread.android.activity.misc.OnLoadMoreScrollListener;
import it.uspread.android.activity.misc.OnViewInListClickListener;
import it.uspread.android.data.Message;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.data.type.ReportType;
import it.uspread.android.message.MessageUtils;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskIgnoreMessage;
import it.uspread.android.task.TaskListReceivedMessages;
import it.uspread.android.task.TaskLoadImageCacheOrWeb;
import it.uspread.android.task.TaskReportMessage;
import it.uspread.android.task.TaskSpreadMessage;

/**
 * Fragment d'activité
 * <ul>
 * <li>Présenter le pool de message reçus (Messages en attente de décision de décision)</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class ReceivedMessagesFragment extends USpreadItListFragment implements OnViewInListClickListener {

    /** Indique le nombre de messages à charger */
    private final int NB_MESSAGE_PAGINATION = 10;

    /** Liste d'affichage */
    private ListView listView;

    /** Scroll listener */
    private OnLoadMoreScrollListener onLoadMoreScrollListener;

    /** Modèle des éléments à afficher */
    private ReceivedMessagesAdapter listAdapter;

    /** Composant de refresh de la liste lorsque non vide */
    private SwipeRefreshLayout swipeLayoutList;

    /** Composant de refresh de la liste lorsque vide */
    private SwipeRefreshLayout swipeLayoutEmpty;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_messages_received, container, false);

        // ############################## Confivuration du layout liste ##############################
        listView = (ListView) view.findViewById(android.R.id.list);
        listAdapter = new ReceivedMessagesAdapter(getActivity(), this);
        listView.setAdapter(listAdapter);

        // ############################## Configuration des composant permettant de faire du pull down to refresh. ##############################
        // Il y en a deux en raison de l'existance d'un composant spécial affiché lorsqu'aucun élément dans la liste
        swipeLayoutList = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_messages);
        // Sur un refresh manuel de la liste non vide : On veut charger les nouveaux messages plus récents
        swipeLayoutList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Demander de mettre à jour toutes les valeurs dynamique de nos messages : ceci afin de détecter en réutilisant le code existant la suppression de message (Pour cet écran à ce jour les valeurs dynamique ne sont pas affiché de toute manière)
                loadingReceivedMessage(new MessageCriteria(true, USpreadItApplication.getInstance().getMessageCache().getOldestDateReceptionOfMessagesReceived(), MessageCriteria.VAL_AFTER_OR_EQUALS_DATE));

                // Demander un chargement des messages plus récents que le message le plus récent en notre possesion
                loadingReceivedMessage(new MessageCriteria(USpreadItApplication.getInstance().getMessageCache().getLatestDateReceptionOfMessagesReceived(), MessageCriteria.VAL_AFTER_DATE));
            }
        });
        ApplicationStyle.applySwipeLayoutStyle(swipeLayoutList);

        swipeLayoutEmpty = (SwipeRefreshLayout) view.findViewById(android.R.id.empty);
        // Sur un refresh manuel de la liste vide : On veut charger les NB_MESSAGE_PAGINATION premiers messages de cette liste
        swipeLayoutEmpty.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Chargement des NB_MESSAGE_PAGINATION premiers messages de la liste
                loadingReceivedMessage(new MessageCriteria(NB_MESSAGE_PAGINATION));
            }
        });
        ApplicationStyle.applySwipeLayoutStyle(swipeLayoutEmpty);

        // ############################## Configuration de la pagination ##############################
        onLoadMoreScrollListener = new OnLoadMoreScrollListener() {
            @Override
            public void loadMore() {
                swipeLayoutList.setRefreshing(true);
                new TaskListReceivedMessages(TaskListReceivedMessages.RESULT_CODE_LOAD_MORE, false).execute(new MessageCriteria(NB_MESSAGE_PAGINATION, USpreadItApplication.getInstance().getMessageCache().getOldestDateReceptionOfMessagesReceived(), MessageCriteria.VAL_BEFORE_DATE));
            }
        };
        if (savedInstanceState != null && savedInstanceState.containsKey(OnLoadMoreScrollListener.CONTEXT_DATA_REMAINING)) {
            onLoadMoreScrollListener.setDataRemaining(savedInstanceState.getBoolean(OnLoadMoreScrollListener.CONTEXT_DATA_REMAINING));
        }
        listView.setOnScrollListener(onLoadMoreScrollListener);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Recherche des messages en cache RAM
        final List<Message> listMessage = USpreadItApplication.getInstance().getMessageCache().getListMessageReceived();

        if (!listMessage.isEmpty()) {
            listAdapter.addAll(listMessage);
        }
        // Si aucun message n'est trouvé en cache : On veut charger les NB_MESSAGE_PAGINATION premiers messages de cette liste
        else {
            // Lancement de l'indicateur de chargement
            swipeLayoutEmpty.setProgressViewOffset(false, 0, ApplicationStyle.getActionBarSize(getActivity()));
            swipeLayoutEmpty.setRefreshing(true);
            // Chargement des NB_MESSAGE_PAGINATION premiers messages
            loadingReceivedMessage(new MessageCriteria(NB_MESSAGE_PAGINATION));
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(OnLoadMoreScrollListener.CONTEXT_DATA_REMAINING, onLoadMoreScrollListener.isDataRemaining());
    }

    @Override
    public void onViewInListClick(final View view, final int position) {
        final Message message = (Message) getListView().getItemAtPosition(position);
        switch (view.getId()) {
            case R.id.action_hyperlink:
                final PopupMenu popupLinks = new PopupMenu(getActivity(), view);
                MessageUtils.configurePopupLinks(getActivity(), popupLinks, message.getListLink());
                popupLinks.show();
                break;
            case R.id.action_spread:
                listAdapter.startProcessIndicatorOnMessage(message.getId());
                listView.invalidateViews();
                new TaskSpreadMessage(message.getId()).execute(message);
                break;
            case R.id.action_ignore:
                listAdapter.startProcessIndicatorOnMessage(message.getId());
                listView.invalidateViews();
                new TaskIgnoreMessage(message.getId()).execute(message);
                break;
            case R.id.action_moreAction:
                final PopupMenu popupMoreAction = new PopupMenu(getActivity(), view);
                popupMoreAction.getMenuInflater().inflate(R.menu.popup_messages_spread_moreaction, popupMoreAction.getMenu());
                popupMoreAction.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        final TaskReportMessage taskReport = new TaskReportMessage(message.getId());

                        switch (item.getItemId()) {
                            case R.id.action_report_spam:
                                listAdapter.startProcessIndicatorOnMessage(message.getId());
                                listView.invalidateViews();
                                taskReport.execute(message, ReportType.SPAM);
                                return true;
                            case R.id.action_report_inappropriate:
                                listAdapter.startProcessIndicatorOnMessage(message.getId());
                                listView.invalidateViews();
                                taskReport.execute(message, ReportType.INAPPROPRIATE);
                                return true;
                            case R.id.action_report_threat:
                                listAdapter.startProcessIndicatorOnMessage(message.getId());
                                listView.invalidateViews();
                                taskReport.execute(message, ReportType.THREAT);
                                return true;
                        }
                        return false;
                    }
                });
                popupMoreAction.show();
                break;
        }
    }

    /**
     * Chargement des message reçus.
     */
    private void loadingReceivedMessage(final MessageCriteria criteria) {
        new TaskListReceivedMessages(TaskListReceivedMessages.RESULT_CODE_GENERAL, criteria != null && criteria.isOnlyDynamicValue()).execute(criteria != null ? criteria : new MessageCriteria());
    }

    /**
     * Rafraichi la liste pour être synchrone avec le cache
     */
    public void refreshList() {
        listAdapter.getListElement().clear();
        listAdapter.getListElement().addAll(USpreadItApplication.getInstance().getMessageCache().getListMessageReceived());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
        if (taskResult.success) {
            if (taskResult.resultCode == TaskListReceivedMessages.RESULT_CODE_LOAD_MORE) {
                final List<Message> listMessage = (List<Message>) taskResult.resultData;
                if (listMessage.size() < NB_MESSAGE_PAGINATION) {
                    onLoadMoreScrollListener.setDataRemaining(false);
                }
            }

            if (taskResult.resultCode == TaskListReceivedMessages.RESULT_CODE_GENERAL || taskResult.resultCode == TaskListReceivedMessages.RESULT_CODE_LOAD_MORE) {
                refreshList();
            } else if (taskResult.resultCode == TaskSpreadMessage.RESULT_CODE) {
                final Message message = (Message) taskResult.resultData;
                listAdapter.remove(message);
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_spread), Toast.LENGTH_SHORT).show();
            } else if (taskResult.resultCode == TaskIgnoreMessage.RESULT_CODE) {
                final Message message = (Message) taskResult.resultData;
                listAdapter.remove(message);
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_ignore), Toast.LENGTH_SHORT).show();
            } else if (taskResult.resultCode == TaskReportMessage.RESULT_CODE) {
                final Message message = (Message) taskResult.resultData;
                listAdapter.remove(message);
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_report), Toast.LENGTH_SHORT).show();
            } else if (taskResult.resultCode == TaskLoadImageCacheOrWeb.RESULT_CODE) {
                listAdapter.notifyDataSetChanged();
            }
        }

        if (taskResult.resultCode == TaskListReceivedMessages.RESULT_CODE_GENERAL) {
            swipeLayoutEmpty.setRefreshing(false);
            swipeLayoutList.setRefreshing(false);
        } else if (taskResult.resultCode == TaskListReceivedMessages.RESULT_CODE_LOAD_MORE) {
            onLoadMoreScrollListener.setLoading(false);
            swipeLayoutList.setRefreshing(false);
        } else if (taskResult.resultCode == TaskSpreadMessage.RESULT_CODE || taskResult.resultCode == TaskIgnoreMessage.RESULT_CODE || taskResult.resultCode == TaskReportMessage.RESULT_CODE) {
            listAdapter.stopProcessIndicatorOnMessage(Task.retrieveIdReferred(taskResult.taskId));
            listView.invalidateViews();
        }
    }
}
