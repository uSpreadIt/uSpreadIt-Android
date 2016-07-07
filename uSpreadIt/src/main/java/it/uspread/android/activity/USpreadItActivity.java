package it.uspread.android.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;
import java.util.List;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskResultReceiver;

/**
 * Activité intégrant les comportements suivants :
 * <ul>
 * <li>Indique à l'application qui est l'activité courante {@link USpreadItApplication}</li>
 * <li>Ecoute les résultats des tâches asynchrones de communication avec le serveur {@link it.uspread.android.task.Task} afin de pourvoir réagir si intéréssé</li>
 * <li>Gère les attentes bloquante des tâches</li>
 * </ul>
 */
public abstract class USpreadItActivity extends ActionBarActivity implements TaskResultReceiver {

    /** Clé de l'objet sauvegardé dans le contexte */
    private static final String CONTEXT_WAITING_TASK = "waiting_task";

    /** Indique si l'activité est active (cf Android Activity lifecycle running) */
    private boolean active = false;

    /** Stocke les résultats des taches en attendant que l'activité puisse les traiter */
    private List<Task.TaskResult> listTaskResult = new ArrayList<>();

    /** Animation d'attente bloquante */
    private ProgressDialog waitDialog = null;

    private ArrayList<String> listWaitingTask = new ArrayList<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Task.attachTaskResultReceiver(this);
        if (savedInstanceState != null) {
            listWaitingTask = savedInstanceState.getStringArrayList(CONTEXT_WAITING_TASK);
            if (listWaitingTask == null) {
                listWaitingTask = new ArrayList<>();
            }
            if (!listWaitingTask.isEmpty()) {
                startBlockingScreen(null);
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        USpreadItApplication.getInstance().setRunningActivity(this); // Car onActivityResult si appelé l'est avant le onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        active = true;
        USpreadItApplication.getInstance().setRunningActivity(this);

        if (!listTaskResult.isEmpty()) {
            for (Task.TaskResult taskResult : listTaskResult) {
                stopBlockingScreen(taskResult.taskId);
                onReceiveTaskResultUpdateUI(taskResult);
            }
            listTaskResult.clear();
        }
    }

    @Override
    protected void onPause() {
        final Activity currentActivity = USpreadItApplication.getInstance().getRunningActivity();
        if (currentActivity != null && currentActivity.equals(this)) {
            USpreadItApplication.getInstance().setRunningActivity(null);
        }
        active = false;
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(CONTEXT_WAITING_TASK, listWaitingTask);
    }

    @Override
    protected void onDestroy() {
        Task.detachTaskResultReceiver(this);
        super.onDestroy();
    }

    @Override
    public final void onReceiveTaskResult(final Task.TaskResult taskResult) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (active) {
                    stopBlockingScreen(taskResult.taskId);
                    onReceiveTaskResultUpdateUI(taskResult);
                } else {
                    listTaskResult.add(taskResult);
                }
            }
        });
    }

    /**
     * Lancement du blocage de l'activité pour attendre la fin d'une tâche
     *
     * @param taskId
     *         Id de la tâche demandant le blocage de l'écran.
     */
    public void startBlockingScreen(final String taskId) {
        if (taskId == null || listWaitingTask.isEmpty()) {
            waitDialog = new ProgressDialog(this);
            waitDialog.setMessage(getResources().getString(R.string.text_wait));
            waitDialog.setIndeterminate(true);
            waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitDialog.setCancelable(false);
            waitDialog.show();
        }
        if (taskId != null) {
            listWaitingTask.add(taskId);
        }
    }

    /**
     * Terminer le blocage de l'activité
     *
     * @param taskId
     *         Id de la tâche demandant le déblocage de l'écran.
     */
    public void stopBlockingScreen(final String taskId) {
        boolean removed = listWaitingTask.remove(taskId);
        if (removed && listWaitingTask.isEmpty()) {
            waitDialog.dismiss();
            waitDialog = null;
        }
    }

}


