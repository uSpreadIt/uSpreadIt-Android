package it.uspread.android.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Receveur des événements du GCM.
 *
 * @author Lone Décosterd,
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Lance le service en forçant le périphe à resté éveillé pour le traitement
        startWakefulService(context, (intent.setComponent(new ComponentName(context.getPackageName(), GcmIntentService.class.getName()))));
        setResultCode(Activity.RESULT_OK);
    }

}
