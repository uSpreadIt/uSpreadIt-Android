package it.uspread.android.task;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.data.User;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache de login
 */
public class TaskLogin extends Task<User, Void, User> {

    /** Code de retour de la tâche */
    public static final int RESULT_CODE = Task.provideResultCode();

    /**
     * Constructeur.
     */
    public TaskLogin() {
        super(RESULT_CODE, true);
    }

    @Override
    protected User executeTask(final User... users) throws USpreadItException {
        USpreadItServer.getInstance().loginUser(users[0], USpreadItApplication.getInstance().getSessionManager().getGCMRegistrationId());
        return users[0];
    }

    @Override
    protected void onFinalizeTask(final User user) {
        // Login OK : on créé les infos de session de l'utilisateur
        USpreadItApplication.getInstance().getSessionManager().createSession(user);
    }
}
