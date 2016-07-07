package it.uspread.android.task;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de création asynchrone d'un nouveau message
 */
public class TaskSendMessage extends Task<Message, Void, Message> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskSendMessage() {
        super(RESULT_CODE, true);
    }

    @Override
    protected Message executeTask(final Message... messages) throws USpreadItException {
        return USpreadItServer.getInstance().sendMessage(messages[0]);
    }

    @Override
    protected void onFinalizeTask(final Message message) {
        // Ajoute le message au cache
        USpreadItApplication.getInstance().getMessageCache().syncMessagesWrited(Message.inList(message));
    }
}
