package it.uspread.android.task;

import it.uspread.android.data.Status;
import it.uspread.android.data.criteria.StatusCriteria;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de récupération asynchrone des informations de status de l'utilisateur
 */
public class TaskUserStatus extends Task<StatusCriteria, Void, Status> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskUserStatus() {
        super(RESULT_CODE);
    }

    @Override
    protected it.uspread.android.data.Status executeTask(final StatusCriteria... criterias) throws USpreadItException {
        return USpreadItServer.getInstance().getUserStatus(criterias[0]);
    }

    @Override
    protected void onFinalizeTask(final it.uspread.android.data.Status status) {
        // Rien à faire de plus
    }
}
