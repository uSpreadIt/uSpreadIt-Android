package it.uspread.android.activity.account;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import it.uspread.android.R;
import it.uspread.android.activity.USpreadItActivity;
import it.uspread.android.activity.misc.KeyboardUtils;
import it.uspread.android.data.User;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskRegister;

/**
 * Activité
 * <ul>
 * <li>Création du compte</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class SignupActivity extends USpreadItActivity {

    private EditText textUsername;
    private EditText textPassword;
    private EditText textEmail;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_signup);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        KeyboardUtils.registerHideSoftKeyBoard(this, findViewById(R.id.activity_account_signup), null);

        textUsername = (EditText) findViewById(R.id.edit_username);
        textPassword = (EditText) findViewById(R.id.edit_password);
        textEmail = (EditText) findViewById(R.id.edit_email);
    }

    /**
     * Callback pour la création du compte : retourne à l'activité de login en cas de succés
     *
     * @param v
     *         boutton
     */
    public void register(final View v) {
        final User user = new User(textUsername.getText().toString(), textPassword.getText().toString(), textEmail.getText().toString());

        new TaskRegister().execute(user);
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
       if (taskResult.success) {
           if (taskResult.resultCode == TaskRegister.RESULT_CODE) {
               Toast.makeText(this, getResources().getString(R.string.toast_accountCreated), Toast.LENGTH_SHORT).show();
               finish();
           }
       }
    }
}
