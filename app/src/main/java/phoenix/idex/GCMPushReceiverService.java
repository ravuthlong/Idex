package phoenix.idex;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import phoenix.idex.Activities.MainActivity;

/**
 * Created by Ravinder on 5/25/16.
 */

// To receive message send from GCM server

public class GCMPushReceiverService extends GcmListenerService{

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        sendNotification(message);
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = 0; // our request code
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, intent, PendingIntent.FLAG_ONE_SHOT);

        // set notification
        // Sound
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Build notification
        android.support.v4.app.NotificationCompat.Builder noBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("GCM Test")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, noBuilder.build());
    }
}
