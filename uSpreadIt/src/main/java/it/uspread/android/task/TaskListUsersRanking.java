package it.uspread.android.task;

import java.util.List;

import it.uspread.android.data.UserRanking;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de chargement asynchrone des statistiques des utilisateurs
 */
public class TaskListUsersRanking extends Task<Void, Void, List<UserRanking>> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskListUsersRanking() {
        super(RESULT_CODE);
    }

    @Override
    protected List<UserRanking> executeTask(final Void... params) throws USpreadItException {
        return USpreadItServer.getInstance().getUsersRanking();
    }

    @Override
    protected void onFinalizeTask(final List<UserRanking> listUser) {
        // Rien à faire de plus
    }
}
