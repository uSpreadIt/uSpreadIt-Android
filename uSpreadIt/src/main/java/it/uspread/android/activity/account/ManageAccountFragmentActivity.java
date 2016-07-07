package it.uspread.android.activity.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.USpreadItFragment;
import it.uspread.android.data.User;
import it.uspread.android.data.criteria.UserCriteria;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskUserConnected;

/**
 * Fragment d'activité pour la gestion du compte utilisateur
 *
 * @author Lone Décosterd,
 */
public class ManageAccountFragmentActivity extends USpreadItFragment {

    private EditText textUsername;
    private EditText textEmail;

    private boolean editMode = false;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_account_manage, container, false);

        // Retrieve the elements from the view
        textUsername = (EditText) view.findViewById(R.id.edit_username);
        textUsername.setEnabled(false);
        textEmail = (EditText) view.findViewById(R.id.edit_email);
        textEmail.setEnabled(false);

        // Fetch current user informations & update the view
        new TaskUserConnected().execute(new UserCriteria(USpreadItApplication.getInstance().getSessionManager().getGCMRegistrationId()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getResources().getString(R.string.activity_account));
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
        if (taskResult.success) {
            if (taskResult.resultCode == TaskUserConnected.RESULT_CODE) {
                final User user = (User) taskResult.resultData;
                textUsername.setText(user.getUsername());
                textEmail.setText(user.getEmail());
            }
        }
    }
}
