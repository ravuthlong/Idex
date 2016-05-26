package phoenix.idex;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Ravinder on 5/25/16.
 */
public class GCMTokenRefreshListenerService extends InstanceIDListenerService {

    // When token is refreshed, start service to get new token
    @Override
    public void onTokenRefresh() {

        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        startService(intent);
    }
}
