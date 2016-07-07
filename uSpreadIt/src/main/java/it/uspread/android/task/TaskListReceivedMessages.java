package it.uspread.android.task;

import java.util.ArrayList;
import java.util.List;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de chargement asynchrone des messages en attentes de propagation
 */
public class TaskListReceivedMessages extends Task<MessageCriteria, Void, List<Message>> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE_GENERAL = Task.provideResultCode();
    /** Code de retour de la tâche lorsque utilisé pour un tentative de chargement des messages plus anciens s'il en existe */
    public static final int RESULT_CODE_LOAD_MORE = Task.provideResultCode();

    /** Indique que la tache est lancé uniquement pour maj des messages existants */
    protected final boolean onlyUpdateDynamicValue;

    /** Contenu de la liste des messages en cache au lancement de la mise à jour */
    protected List<Message> listMessageInCacheAtLaunch;

    /**
     * Constructeur.
     *
     * @param resultCode
     *         {@link #resultCode}
     * @param onlyUpdateDynamicValue
     *         {@link #onlyUpdateDynamicValue}
     */
    public TaskListReceivedMessages(final int resultCode, final boolean onlyUpdateDynamicValue) {
        super(resultCode);
        this.onlyUpdateDynamicValue = onlyUpdateDynamicValue;
        if (onlyUpdateDynamicValue) {
            listMessageInCacheAtLaunch = new ArrayList<>(USpreadItApplication.getInstance().getMessageCache().getListMessageReceived());
        }
    }

    @Override
    protected List<Message> executeTask(final MessageCriteria... criterias) throws USpreadItException {
        return USpreadItServer.getInstance().getReceivedMessage(criterias[0]);
    }

    @Override
    protected void onFinalizeTask(final List<Message> listMessage) {
        // MAJ du cache RAM
        USpreadItApplication.getInstance().getMessageCache().syncMessagesReceived(listMessage);

        // Suppression des messages qui n'existe plus sur le serveur (Lors d'un update de la liste existente le serveur nous renvoie les valeurs de tous les messages de notre liste coté client sauf si certain d'entre eux ont été supprimé entre temps)
        if (onlyUpdateDynamicValue) {
            for (Message msg : listMessageInCacheAtLaunch) {
                if (!listMessage.contains(msg)) {
                    USpreadItApplication.getInstance().getMessageCache().deleteMessage(msg, true);
                }
            }
        }
    }
}
