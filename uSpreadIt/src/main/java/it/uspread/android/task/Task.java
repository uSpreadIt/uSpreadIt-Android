package it.uspread.android.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import it.uspread.android.USpreadItApplication;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Tache asynchrone pour la communication avec le serveur (voir {@link android.os.AsyncTask})<br>
 * Intégre :
 * <ul>
 * <li>La gestion des erreurs</li>
 * <li>La gestion d'une attente bloquante (facultatif)</li>
 * <li>La gestion d'unicité du lancement d'une task sur le même objet (facultatif) : lorsque l'attente est non bloquante il peut être utile de pas permettre de lancer deux fois la
 * même action. pour cela utiliser le contructeur permettant de spécifier l'id de la tâche</li>
 * </ul>
 * <br>
 * Dans l'idéal et pour rester cohérant il faudrait que une tâche soit un service au lieu d'un AsyncTask qui n'est - théoriquement - pas adapté aux appel Réseaux (tâche "longue" et qui n'ont
 * pas de rapport avec le cycle de vie de l'activité ne doivent pas utiliser AsyncTask). C'est pas fait pour gagner du temps. Cela dit le système développé place résoud les problémes
 *
 * @author Lone Décosterd,
 */
public abstract class Task<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    /**
     * Le résultat de l'éxécution d'une tâche
     */
    public static class TaskResult {

        /** Id de la tache */
        public String taskId;
        /** Indique si la tache s'est terminé avec succé */
        public boolean success;
        /** Le code de retour identiant le type de résultat de la tâche (Ceux çi sont référencer dans chaque classe de Tâche) */
        public int resultCode;
        /** Les donnée retourné par la tâche ou null si aucune donné */
        public Object resultData;

        /**
         * Constructeur
         *
         * @param taskId
         *         {@link #success}
         * @param success
         *         {@link #success}
         * @param resultCode
         *         {@link #resultCode}
         * @param resultData
         *         {@link #resultData}
         */
        public TaskResult(final String taskId, final boolean success, final int resultCode, final Object resultData) {
            this.taskId = taskId;
            this.success = success;
            this.resultCode = resultCode;
            this.resultData = resultData;
        }
    }

    /** Valeur courante du provider de code */
    private static int RESULT_CODE_INCREMENT = 0;

    /**
     * Fourni un nouveau code de retour unique pour caractériser un nouveau résultat de tâche
     *
     * @return Un nouveau code unique
     */
    public static synchronized int provideResultCode() {
        RESULT_CODE_INCREMENT++;
        return RESULT_CODE_INCREMENT;
    }

    /** Valeur courante du provider d'id */
    private static long TASK_ID_INCREMENT = 0;

    /**
     * Fourni un nouveau Id de tâche unique
     *
     * @return Un nouvel ID unique
     */
    public static synchronized long provideTaskId() {
        TASK_ID_INCREMENT++;
        return TASK_ID_INCREMENT;
    }

    /** La liste des objets intéressés par les résultats des tâches accomplie avec succés */
    private static final Set<TaskResultReceiver> listTaskResultReceiver = Collections.synchronizedSet(new HashSet<TaskResultReceiver>());

    /** Liste des tache active */
    private static final Set<Task> listTaskActive = Collections.synchronizedSet(new HashSet<Task>());

    /** Le caractère utilisé pour séparé les éléments du {@link #taskId} */
    protected static String TASK_IDPART_SEPARATOR = "#";

    /** Id de la tache */
    private final String taskId;
    /** Le code de retour identiant le type de résultat de la tâche (Ceux çi sont référencer dans chaque classe de Tâche) */
    private int resultCode;
    /** Indique si la tâche doit bloquer l'activité */
    private boolean useBlockingWaitScreen;
    /** Exception pouvant survenir lors du traitement */
    private USpreadItException exception;

    /**
     * Tâche pouvant s'éxécuter sans contrôle particulier
     *
     * @param resultCode
     *         {@link #resultCode}
     */
    public Task(final int resultCode) {
        this(resultCode, false, null);
    }

    /**
     * Tâche ne se lançant que si aucune autre tâche de même identifiant n'est lancé
     *
     * @param resultCode
     *         {@link #resultCode}
     * @param taskId
     *         L'id de l'objet référé par la tâche précédé de son nom séparé par {@link #TASK_IDPART_SEPARATOR}. Sera utilisé pour identifier la tâche. Permettant ainsi de
     *         garentir une non concurrence de lancement de même tâche sur le même objet
     */
    public Task(final int resultCode, final String taskId) {
        this(resultCode, false, taskId);
    }

    /**
     * Tâche pouvant s'éxécuter sans contrôle particulier mais ou on peut spécifier si l'activité sera mise en attente
     *
     * @param resultCode
     *         {@link #resultCode}
     * @param useBlockingWaitScreen
     *         Vrai pour afficher un boite de dialogue d'attente bloquante pendant l'éxécution de la tache.
     */
    public Task(final int resultCode, final boolean useBlockingWaitScreen) {
        this(resultCode, useBlockingWaitScreen, null);
    }

    /**
     * Constructeur.
     *
     * @param resultCode
     *         {@link #resultCode}
     * @param useBlockingWaitScreen
     *         Vrai pour afficher un boite de dialogue d'attente bloquante pendant l'éxécution de la tache.
     * @param taskId
     *         L'id de l'objet référé par la tâche précédé de son nom séparé par {@link #TASK_IDPART_SEPARATOR}. Sera utilisé pour identifier la tâche. Permettant ainsi de
     *         garentir une non concurrence de lancement de même tâche sur le même objet
     */
    public Task(final int resultCode, final boolean useBlockingWaitScreen, final String taskId) {
        this.resultCode = resultCode;
        if (taskId == null) {
            this.taskId = String.valueOf(provideTaskId());
        } else {
            this.taskId = "task" + TASK_IDPART_SEPARATOR + taskId;
        }
        this.useBlockingWaitScreen = useBlockingWaitScreen;
    }

    /**
     * @return {@link #taskId}
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * Permet de retrouver l'id de l'objet référé par la tâche. S'utilise que si la tâche  à utilisé le constructeur permettant de spécifier sa propre ID
     *
     * @param taskId
     *         {@link #taskId}
     * @return L'id de l'objet concerné par la tâche
     */
    public static long retrieveIdReferred(final String taskId) {
        String[] str = taskId.split(TASK_IDPART_SEPARATOR);
        return Long.parseLong(str[str.length - 1]);
    }

    /**
     * Attache un objet intéressé par les résultats des tâches accomplie avec succés
     *
     * @param taskResultReceiver
     *         L'objet à attacher
     */
    public static void attachTaskResultReceiver(final TaskResultReceiver taskResultReceiver) {
        listTaskResultReceiver.add(taskResultReceiver);
    }

    /**
     * Détache un objet qui ne s'intéresse plus aux résultats des tâches accomplie avec succés
     *
     * @param taskResultReceiver
     *         L'objet à détacher
     */
    public static void detachTaskResultReceiver(final TaskResultReceiver taskResultReceiver) {
        listTaskResultReceiver.remove(taskResultReceiver);
    }

    /**
     * Informe qu'une tâche se termine avec succès
     *
     * @param taskResult
     *         Le résultat de la tâche
     */
    private static void dispatchTaskResult(TaskResult taskResult) {
        // Cette synchronisation de listTaskResultReceiver  est INUTILE car tous ces accé sont lancé sur la thread UI mais si on change à un l'utilisation d'un service laisser ça permettra de se rappeler de faire attention
        final Set<TaskResultReceiver> listTaskResultReceiverCopy;
        synchronized (listTaskResultReceiver) {
            listTaskResultReceiverCopy = new HashSet<>(listTaskResultReceiver);
        }
        for (TaskResultReceiver taskResultReceiver : listTaskResultReceiverCopy) {
            taskResultReceiver.onReceiveTaskResult(taskResult);
        }
    }

    /**
     * Vérifie que la tâche peut s'éxécuter
     *
     * @return Vrai si aucune autre tâche ayant le même sens métier n'est trouvé
     */
    private synchronized boolean checkTaskCanExecute() {
        synchronized (listTaskActive) {
            for (Task task : listTaskActive) {
                if (task.getTaskId().equals(taskId)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Annule toutes les task en cours d'éxécution
     */
    public static void cancelAllPendingTask() {
        synchronized (listTaskActive) {
            for (Task task : listTaskActive) {
                task.cancel(true);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        if (!checkTaskCanExecute()) {
            cancel(false);
        } else {
            listTaskActive.add(this);
            if (useBlockingWaitScreen) {
                USpreadItApplication.getInstance().getRunningActivity().startBlockingScreen(taskId);
            }
        }
    }

    @Override
    protected final Result doInBackground(final Params... params) {
        if (!isCancelled()) {
            try {
                Result result = executeTask(params);
                onFinalizeTask(result);
                return result;
            } catch (USpreadItException e) {
                exception = e;
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Result result) {
        if (exception == null) {
            onSuccess(result);
        } else {
            onFailure();
        }

        // Publication du résultat en succé ou en échec de la tâche
        dispatchTaskResult(new TaskResult(taskId, exception == null, resultCode, result));

        listTaskActive.remove(this);
    }

    @Override
    protected void onCancelled(final Result result) {
        // Publication de l'échec de la tâche
        dispatchTaskResult(new TaskResult(taskId, false, resultCode, result));

        listTaskActive.remove(this);
    }

    /**
     * Lance le traitement asynchrone
     *
     * @param params
     *         Les paramètres de la tache
     * @return Le résultat du traitement
     * @throws it.uspread.android.remote.exception.USpreadItException
     *         En cas d'echec de la tache
     */
    protected abstract Result executeTask(final Params... params) throws USpreadItException;

    /**
     * Appelé en fin de executeTask s'il n'y a pas eu d'erreur : on est donc toujours dans la tache asynchrone (hors de l'UI thread).
     *
     * @param result
     *         Le résultat rendu
     */
    protected abstract void onFinalizeTask(final Result result);

    /**
     * Appelé en Thread UI lors du succès de l'éxécution de la tache asynchrone.<br>
     * <b>Cette méthode doit être prudente pour toucher à l'UI (en régle générale elle pourra pas faire grand chose). Ceci pour se protéger des problématique de cycle de vie d'une
     * Activité VS Requete réseaux : utiliser {@link TaskResultReceiver} pour mettre à jour l'UI à partir des résultats de la tâche</b>
     *
     * @param result
     *         Le résultat rendu
     */
    protected void onSuccess(final Result result) {
    }

    /**
     * Appelé en Thread UI lors de l'echec de l'éxécution de la tache asynchrone
     */
    protected void onFailure() {
        // Information de l'utilisateur avec un toast s'il est sur l'application
        final Activity currentActivity = USpreadItApplication.getInstance().getRunningActivity();
        if (currentActivity != null) {
            Toast.makeText(currentActivity, exception.getUserMessage(USpreadItApplication.getInstance().getResources()), Toast.LENGTH_LONG).show();
        }

        // Si l'erreur est une authentification nécessaire il faut rediriger vers la page de login
        if (USpreadItException.Type.AUTHENTICATION_REQUIRED.equals(exception.getType())) {
            // Lancer un logout provoquera le relancement de l'application
            // (Attention sur des changement de cette dynamqiue s'assurer que l'écran de login n'ai pas de back possible et ne soit pas accessible par un back)
            USpreadItApplication.getInstance().getSessionManager().logout();
        }
        // Si l'erreur est non géré loguer
        else if (USpreadItException.Type.OTHERS.equals(exception.getType())) {
            Log.e("SERVER", exception.getMessage());
            exception.printStackTrace();
            if (exception.getCause() != null) {
                exception.getCause().printStackTrace();
            }
        }
    }

}
