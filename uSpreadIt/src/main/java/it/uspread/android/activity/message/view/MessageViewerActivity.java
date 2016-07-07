package it.uspread.android.activity.message.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.USpreadItActivity;
import it.uspread.android.data.Message;
import it.uspread.android.data.type.MessageType;
import it.uspread.android.message.MessageUtils;
import it.uspread.android.message.MessageView;
import it.uspread.android.task.Task;
import it.uspread.android.task.TaskDeleteMessage;
import it.uspread.android.task.TaskLoadImageCacheOrWeb;

/**
 * Activité de visualisation d'un message propagé ou écrit par l'utilisateur<br/>
 *
 * @author Lone Décosterd,
 */
public class MessageViewerActivity extends USpreadItActivity {

    /** Identification de la transition */
    public static final String TRANSITION_NAME = "DETAIL-MessageViewerActivity";

    /** Composant de visualisation du message */
    private MessageView messageView;

    /** Le message visualisé */
    private Message message;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        message = USpreadItApplication.getInstance().getMessageCache().getMessage(getIntent().getLongExtra(Message.class.getName(), -1));

        if (MessageType.WRITED.equals(message.getMessageType())) {
            setContentView(R.layout.activity_message_writed);
        } else if (MessageType.SPREAD.equals(message.getMessageType())) {
            setContentView(R.layout.activity_message_spread);
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        messageView = (MessageView) findViewById(R.id.message_render);
        messageView.setMessage(message);

        final TextView textNbDiffuse = (TextView) findViewById(R.id.text_nbdiffuse);
        textNbDiffuse.setText(MessageUtils.convertToLongNbSpread(message.getNbSpread()));

        final ImageButton link = (ImageButton) findViewById(R.id.action_hyperlink);
        link.setVisibility(message.hasLink() ? View.VISIBLE : View.GONE);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupLinks = new PopupMenu(MessageViewerActivity.this, view);
                MessageUtils.configurePopupLinks(MessageViewerActivity.this, popupLinks, message.getListLink());
                popupLinks.show();
            }
        });

        if (MessageType.WRITED.equals(message.getMessageType())) {
            final ImageButton actionDelete = (ImageButton) findViewById(R.id.action_delete);
            actionDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MessageViewerActivity.this);
                    builder.setMessage(getResources().getString(R.string.dialog_delete))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new TaskDeleteMessage().execute(message);
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }

        ViewCompat.setTransitionName(messageView, TRANSITION_NAME);
    }

    @Override
    public void onReceiveTaskResultUpdateUI(final Task.TaskResult taskResult) {
        if (taskResult.success) {
            if (taskResult.resultCode == TaskDeleteMessage.RESULT_CODE) {
                Toast.makeText(this, getResources().getString(R.string.toast_delete), Toast.LENGTH_SHORT).show();
                finish();
            } else if (taskResult.resultCode == TaskLoadImageCacheOrWeb.RESULT_CODE) {
                messageView.invalidate();
            }
        }
    }

}
