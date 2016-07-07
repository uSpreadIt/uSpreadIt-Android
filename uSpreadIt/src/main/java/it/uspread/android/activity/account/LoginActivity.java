package it.uspread.android.activity.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.NavigationDrawerActivity;
import it.uspread.android.activity.USpreadItActivity;
import it.uspread.android.activity.misc.KeyboardUtils;
import it.uspread.android.data.User;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskLogin;

/**
 * Activité
 * <ul>
 * <li>Login</li>
 * </ul>
 *
 * @author Lone Décosterd,
 */
public class LoginActivity extends USpreadItActivity {

    private EditText textUsername;
    private EditText textPassword;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_login);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        KeyboardUtils.registerHideSoftKeyBoard(this, findViewById(R.id.activity_account_login), null);

        textUsername = (EditText) findViewById(R.id.edit_username);
        textPassword = (EditText) findViewById(R.id.edit_password);
    }

    /**
     * Callback pour la connection
     *
     * @param v
     *         boutton
     */
    public void login(final View v) {
        final User user = new User(textUsername.getText().toString(), textPassword.getText().toString());

        new TaskLogin().execute(user);
    }

    /**
     * Callback pour le lancement de l'activité d'enregistrement
     *
     * @param v
     *         boutton
     */
    public void launchRegisterActivity(final View v) {
        final Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
        if (taskResult.success) {
            if (taskResult.resultCode == TaskLogin.RESULT_CODE) {
                Toast.makeText(this, getResources().getString(R.string.toast_login) + " " + USpreadItApplication.getInstance().getSessionManager().getUsername(), Toast.LENGTH_SHORT).show();

                // Ouverture de l'activité principale et fermeture de l'activité de login
                final Intent intent = new Intent(this, NavigationDrawerActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
