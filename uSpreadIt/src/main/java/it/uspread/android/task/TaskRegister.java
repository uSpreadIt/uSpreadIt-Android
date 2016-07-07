package it.uspread.android.task;

import it.uspread.android.data.User;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache d'enregistrement
 */
public class TaskRegister extends Task<User, Void, Void> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskRegister() {
        super(RESULT_CODE, true);
    }

    @Override
    protected Void executeTask(final User... users) throws USpreadItException {
        USpreadItServer.getInstance().createUser(users[0]);
        return null;
    }

    @Override
    protected void onFinalizeTask(Void result) {
        // Rien à faire de plus
    }
}