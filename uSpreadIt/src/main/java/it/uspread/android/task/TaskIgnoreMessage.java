package it.uspread.android.task;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache asynchrone de mise en terre d'un message
 */
public class TaskIgnoreMessage extends Task<Message, Void, Message> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     *
     * @param messsageId
     *         Id du message
     */
    public TaskIgnoreMessage(final long messsageId) {
        super(RESULT_CODE, "ignore" + TASK_IDPART_SEPARATOR + messsageId);
    }

    @Override
    protected Message executeTask(final Message... messages) throws USpreadItException {
        USpreadItServer.getInstance().ignoreMessage(messages[0]);
        return messages[0];
    }

    @Override
    protected void onFinalizeTask(final Message message) {
        // On indique la suppresion au cache.
        USpreadItApplication.getInstance().getMessageCache().deleteMessage(message, true);
    }
}
