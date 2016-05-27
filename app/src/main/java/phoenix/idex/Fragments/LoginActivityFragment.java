package phoenix.idex.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.ProfileTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.Activities.SignUpActivity;
import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.GoogleCloudMessaging.GCMRegistrationIntentService;
import phoenix.idex.R;
import phoenix.idex.ServerRequestCallBacks.GetUserCallBack;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyGCM;
import phoenix.idex.VolleyServerConnections.VolleyUserInfo;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment implements View.OnClickListener {

    private CallbackManager mCallbackManager;
    private ImageButton imgbRegister, imgbLogin;
    private ImageView imgLogo;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    private View v;
    private UserLocalStore userLocalStore;
    private TextView etUsername, etPassword, tvContinue, tvUsername, tvPassword;
    private android.support.v7.widget.Toolbar toolbar;
    private FragmentManager fragmentManager;
    private ButtonClickedSingleton clickActivity = ButtonClickedSingleton.getInstance();
    private VolleyUserInfo volleyUserInfo;
    private VolleyGCM volleyGCM;
    private BroadcastReceiver registrationBroadcastReceiver;

    public LoginActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userLocalStore = new UserLocalStore(getActivity());
        volleyGCM = new VolleyGCM(getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTokenTracker != null) {
            mTokenTracker.stopTracking();
            mProfileTracker.stopTracking();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.frag_login, container, false);


        imgbRegister = (ImageButton) v.findViewById(R.id.imgbRegister);
        imgbLogin = (ImageButton) v.findViewById(R.id.imgbLogin);
        etUsername = (TextView) v.findViewById(R.id.etUsername);
        etPassword = (TextView) v.findViewById(R.id.etPassword);
        tvContinue = (TextView) v.findViewById(R.id.tvContinue);
        imgLogo = (ImageView) v.findViewById(R.id.imgLogo);

//        tvIdexTitle.setOnClickListener(this);
        imgbLogin.setOnClickListener(this);
        imgbRegister.setOnClickListener(this);
        tvContinue.setOnClickListener(this);
        imgLogo.setOnClickListener(this);

        tvUsername = (TextView) v.findViewById(R.id.tvUsername);
        tvPassword = (TextView) v.findViewById(R.id.tvPassword);

        Typeface osFont = Typeface.createFromAsset(getActivity().getAssets(), "tt.otf");

        tvContinue.setTypeface(osFont);
        tvPassword.setTypeface(osFont);
        tvUsername.setTypeface(osFont);

        // Remove toolbar from login activity
        MainActivity.rLayoutMain.setPadding(0, 0, 0, 0);
        userLocalStore = new UserLocalStore(getActivity());
        //userLocalStore.clearUserData(); // Clear the last logged in user before storing the new one
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbLogin:
                String  myAndroidDeviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                System.out.println("THE DEVICE ID IS : " + myAndroidDeviceId);
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                User user = new User(username, password);
                authenticate(user);
                break;
            case R.id.imgbRegister:
                Intent signUpIntent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(signUpIntent);
                break;
            case R.id.tvContinue:
                clickActivity.setRollClicked();
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new PostListFragment()).commit();
                MainActivity.listView.setItemChecked(1, true);
                break;
            case R.id.imgLogo:
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new AboutFragment()).commit();
                MainActivity.listView.setItemChecked(2, true);
                break;
        }
    }

    // Authenticate that the correct user is trying to log in
    private void authenticate(User user){
        volleyUserInfo = new VolleyUserInfo(getActivity());

        volleyUserInfo.fetchUserInfo(user, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                // Wrong username and password
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo  = connectivityManager.getActiveNetworkInfo();

                if (networkInfo == null) {
                    showNoInternetError();
                } else if(returnedUser == null){
                    showNoUserErrorMessage();
                } else {
                    System.out.println("RETURNED USER'S");

                    userLocalStore.storeUserData(returnedUser);
                    UserLocalStore.isUserLoggedIn = true;
                    UserLocalStore.allowRefresh = true;
                    clickActivity.setRollClicked();
                    logUserIn();

                    // UPDATE TOKEN IF PHONE TOKEN CHANGED
                    checkForTokenUpdate(returnedUser);
                    System.out.println("RETURNED USER'S TOKEN IS: " + returnedUser.getToken());
                }
            }
        });

    }

    // Check if there needs to be a token update based on token on current logged in device
    // and the token saved in the user's database
    private void checkForTokenUpdate(final User user) {

        System.out.println("WTFFFFF");

        // Check status of google play in the device
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext().getApplicationContext());
        if (ConnectionResult.SUCCESS != resultCode) {
            // Check the type of error
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getContext().getApplicationContext(), "Google play service is not enabled on this device ", Toast.LENGTH_SHORT).show();
                // So notify
                GooglePlayServicesUtil.showErrorNotification(resultCode, getContext().getApplicationContext());
            } else {
                Toast.makeText(getContext().getApplicationContext(), "Device doesn't support google play service ", Toast.LENGTH_SHORT).show();
            }
        } else {
            /*
             * Start service for registering GCM
             */
            Intent intent = new Intent(getActivity(), GCMRegistrationIntentService.class);
            getContext().startService(intent);

        }

        registrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("I RECEIVED SOME SHIT");

                // Check the type of intent filter
                if (intent.getAction().endsWith(GCMRegistrationIntentService.REGISTRATION_SUCCESS)) {
                    // Registration success
                    String currentDeviceToken = intent.getStringExtra("token");

                    // If the current logged in device doesn't match user's database token, update it
                    if (!currentDeviceToken.equals(user.getToken())) {

                        System.out.println("I'M UPDATING THE TOKEN NOW HAHAHAHA");
                        // Update to the logged in device's token for push notification
                        volleyGCM.updateGCMToken(user.getUserID(), currentDeviceToken);
                    }
                } else if (intent.getAction().endsWith(GCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    // Registration error
                } else {
                    // Tobe define
                }
            }
        };
    }

    // Error if the user info is incorrect
    private void showNoUserErrorMessage(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage("Incorrect user details");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    // Error if the user info is incorrect
    private void showNoInternetError(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage("You are not connected to the internet");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }


    // If the log in info is correct, store user in local store and show MainActivity
    private void logUserIn(){
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //It is use to finish current activity
        startActivity(intent);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
