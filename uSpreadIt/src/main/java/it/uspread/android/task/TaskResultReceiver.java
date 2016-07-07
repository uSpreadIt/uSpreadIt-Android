package it.uspread.android.task;

/**
 * Interface pour la réception des résultats des tâches asynchrone
 */
public interface TaskResultReceiver {

    /**
     * Appelé immédiatement lorsqu'une tâche publie ses résultats. L'activité ou le fragment lié peut donc être inactif
     *
     * @param taskResult
     *         Le résultat de l'éxécution d'une tâche
     */
    void onReceiveTaskResult(final Task.TaskResult taskResult);

    /**
     * Appelé pour mettre à jour l'interface lorsqu'une tâche publie ses résultats.<br>
     * De ce fait attends que l'activité ou le fragment lié soit actif pour que la méthode soit éxécuté<br>
     * Cette méthode est toujours lancé sur la ThreadUI.
     *
     * @param taskResult
     *         Le résultat de l'éxécution d'une tâche
     */
    void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult);
}
