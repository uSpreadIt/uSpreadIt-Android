package it.uspread.android.activity;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import it.uspread.android.task.Task;
import it.uspread.android.task.TaskResultReceiver;

/**
 * Fragment intégrant les comportements suivants :
 * <ul>
 * <li>Ecoute les résultats des tâches asynchrones de communication avec le serveur {@link it.uspread.android.task.Task} afin de pourvoir réagir si intéréssé</li>
 * </ul>
 */
public abstract class USpreadItFragment extends Fragment implements TaskResultReceiver {

    /** Indique si le fragment est actif (cf Android Fragment lifecycle running) */
    private boolean active = false;

    /** Stocke les résultats des taches en attendant que le fragment puisse les traiter */
    private List<Task.TaskResult> listTaskResult = new ArrayList<>();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Task.attachTaskResultReceiver(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        active = true;

        if (!listTaskResult.isEmpty()) {
            for (Task.TaskResult taskResult : listTaskResult) {
                onReceiveTaskResultUpdateUI(taskResult);
            }
            listTaskResult.clear();
        }
    }

    @Override
    public void onPause() {
        active = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Task.detachTaskResultReceiver(this);
        super.onDestroy();
    }


    @Override
    public final void onReceiveTaskResult(final Task.TaskResult taskResult) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (active) {
                    onReceiveTaskResultUpdateUI(taskResult);
                } else {
                    listTaskResult.add(taskResult);
                }
            }
        });
    }
}
