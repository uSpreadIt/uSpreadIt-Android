package it.uspread.android.task;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.Message;
import it.uspread.android.data.type.ReportType;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache asynchrone de report d'un message
 */
public class TaskReportMessage extends Task<Object, Void, Message> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     *
     * @param messsageId
     *         Id du message
     */
    public TaskReportMessage(final long messsageId) {
        super(RESULT_CODE, "report" + TASK_IDPART_SEPARATOR + messsageId);
    }

    @Override
    protected Message executeTask(final Object... obj) throws USpreadItException {
        USpreadItServer.getInstance().reportMessage((Message) obj[0], (ReportType) obj[1]);
        return (Message) obj[0];
    }

    @Override
    protected void onFinalizeTask(final Message message) {
        // On indique la suppresion au cache.
        USpreadItApplication.getInstance().getMessageCache().deleteMessage(message, true);
    }
}
