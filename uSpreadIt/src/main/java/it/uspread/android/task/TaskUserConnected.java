package it.uspread.android.task;

import it.uspread.android.data.User;
import it.uspread.android.data.criteria.UserCriteria;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de récupération asynchrone des informations de l'utilisateur
 */
public class TaskUserConnected extends Task<UserCriteria, Void, User> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskUserConnected() {
        super(RESULT_CODE);
    }

    @Override
    protected it.uspread.android.data.User executeTask(final UserCriteria... criterias) throws USpreadItException {
        return USpreadItServer.getInstance().getUser();
    }

    @Override
    protected void onFinalizeTask(final User user) {
        // Rien à faire de plus
    }
}
