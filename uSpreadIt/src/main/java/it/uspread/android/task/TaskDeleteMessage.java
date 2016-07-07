package it.uspread.android.task;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache asynchrone de suppresion d'un message
 */
public class TaskDeleteMessage extends Task<Message, Void, Message> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskDeleteMessage() {
        super(RESULT_CODE, true);
    }

    @Override
    protected Message executeTask(final Message... messages) throws USpreadItException {
        USpreadItServer.getInstance().deleteMessage(messages[0]);
        return messages[0];
    }

    @Override
    protected void onFinalizeTask(final Message message) {
        // Supprime le message du cache
        USpreadItApplication.getInstance().getMessageCache().deleteMessage(message, true);
    }

}

