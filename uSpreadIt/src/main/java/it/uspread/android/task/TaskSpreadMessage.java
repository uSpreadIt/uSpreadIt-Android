package it.uspread.android.task;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache asynchrone de propagation d'un message
 */
public class TaskSpreadMessage extends Task<Message, Void, Message> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     *
     * @param messsageId
     *         Id du message
     */
    public TaskSpreadMessage(final long messsageId) {
        super(RESULT_CODE, "spread" + TASK_IDPART_SEPARATOR + messsageId);
    }

    @Override
    protected Message executeTask(final Message... messages) throws USpreadItException {
        return USpreadItServer.getInstance().spreadMessage(messages[0]);
    }

    @Override
    protected void onFinalizeTask(final Message message) {
        // On indique le transfert de liste du message au cache.
        USpreadItApplication.getInstance().getMessageCache().deleteMessage(message, false);
        USpreadItApplication.getInstance().getMessageCache().syncMessagesSpread(Message.inList(message));
    }
}
