package it.uspread.android.activity.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import it.uspread.android.R;
import it.uspread.android.activity.USpreadItFragment;
import it.uspread.android.data.UserRanking;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskListUsersRanking;

/**
 * Activité
 * <ul>
 * <li>Page des scores</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class HallOfFameFragmentActivity extends USpreadItFragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_scores, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadingUsersRanking();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getResources().getString(R.string.activity_scores));
    }

    /**
     * Chargement du classement.
     */
    private void loadingUsersRanking() {
        new TaskListUsersRanking().execute();
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
        if (taskResult.success) {
            if (taskResult.resultCode == TaskListUsersRanking.RESULT_CODE) {
                final List<UserRanking> listUserRanking = (List<UserRanking>) taskResult.resultData;
                final UsersAdapter listAdapter = new UsersAdapter(getActivity(), listUserRanking);
                ((ListView) getActivity().findViewById(R.id.listUsers)).setAdapter(listAdapter);
            }
        }
    }
}
