package it.uspread.android.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.List;

import it.uspread.android.R;
import it.uspread.android.USpreadItApplication;
import it.uspread.android.activity.NavigationDrawerActivity;
import it.uspread.android.data.Message;
import it.uspread.android.data.criteria.MessageCriteria;
import it.uspread.android.remote.USpreadItServer;
import it.uspread.android.remote.exception.USpreadItException;

/**
 * Service gérant les événements GCM
 *
 * @author Lone Décosterd,
 */
public class GcmIntentService extends IntentService {

    public static String BROADCAST_ACTION_REFRESH_MESSAGES_RECEIVED = "it.uspread.android.intent.action.REFRESH_MESSAGES_RECEIVED";

    /** Id de le notification de nouveaux messages */
    public static final int NOTIFICATION_ID_SYNC = 1;
    /** Log tag */
    private static final String LOG_TAG = "GcmIntentService";

    /**
     * Contructeur
     */
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Bundle extras = intent.getExtras();
        final GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        final String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                final String type = extras.getString("type");
                final String username = extras.getString("username");
                // Si l'utilisateur logué est bien celui indiqué dans la notification alors on continue de traiter
                if (type != null && username != null && username.equals(USpreadItApplication.getInstance().getSessionManager().getUsername())) {
                    try {
                        if ("SYNC".equals(type)) {
                            List<Message> listMessage = USpreadItServer.getInstance().getReceivedMessage(new MessageCriteria(USpreadItApplication.getInstance().getMessageCache().getLatestDateReceptionOfMessagesReceived(), MessageCriteria.VAL_AFTER_DATE));
                            USpreadItApplication.getInstance().getMessageCache().syncMessagesReceived(listMessage);

                            sendSyncNotification();
                        } else if ("DELETE".equals(type)) {
                            Message msg = new Message();
                            msg.setId(Long.valueOf(extras.getString("id")));
                            USpreadItApplication.getInstance().getMessageCache().deleteMessage(msg, true);
                        }

                        // Informe les activités de l'application enregistré qu'il y a un changement dans la liste des messages reçus
                        final Intent broadcast = new Intent(BROADCAST_ACTION_REFRESH_MESSAGES_RECEIVED);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
                    } catch (USpreadItException e) {
                        Log.i(LOG_TAG, "Error when loading new message from a notification");
                    }
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Crée ou met à jour la notification
     */
    private void sendSyncNotification() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, NavigationDrawerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("uSpreadIt")
                .setContentText(getResources().getString(R.string.notif_newMessage))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EMAIL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID_SYNC, builder.build());
    }


}
