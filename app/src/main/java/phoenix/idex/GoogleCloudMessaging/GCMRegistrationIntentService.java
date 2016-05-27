package phoenix.idex.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import phoenix.idex.R;

/**
 * Created by Ravinder on 5/24/16.
 */
public class GCMRegistrationIntentService extends IntentService {

    public static final String REGISTRATION_SUCCESS = "Registration Success";
    public static final String REGISTRATION_ERROR = "Registration Error";

    public GCMRegistrationIntentService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        registerGCM();
    }

    private void registerGCM() {
        Intent registrationComplete = null;
        String token = null;

        try {

            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.w("GCMRegIntentService", "token " + token);

            // Notify UI on completion
            registrationComplete = new Intent(REGISTRATION_SUCCESS);
            registrationComplete.putExtra("token", token);

        } catch (Exception e) {
            Log.v("GCMRegIntentService", "Registration Error");
            registrationComplete = new Intent(REGISTRATION_ERROR);

        }

        // Send Broadcast when registration is done
        // both case error or non-error
        // Broadcast is received in MainActivity.java
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
