package phoenix.idex.Fragments;

import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.R;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.ServerRequestCallBacks.GetUserCallBack;
import phoenix.idex.Activities.SignUpActivity;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment implements View.OnClickListener {
    private TextView mTextDetails;
    private CallbackManager mCallbackManager;
    private ImageButton imgbRegister, imgbLogin;
    private Button bRegister, bLogin, bBrowseIdea;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    private View v;
    private int sizeOfActionBar;
    RelativeLayout rLayoutMain;
    private UserLocalStore userLocalStore;
    private TextView etUsername, etPassword, tvIdexTitle, tvContinue, tvUsername, tvPassword;
    private android.support.v7.widget.Toolbar toolbar;
    private FragmentManager fragmentManager;
    private ButtonClickedSingleton clickActivity = ButtonClickedSingleton.getInstance();


    private FacebookCallback<LoginResult> mCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            Profile profile = Profile.getCurrentProfile();
            displayWelcomeMessage(profile);
        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onError(FacebookException e) {

        }
    };
    public LoginActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        // Track facebook users here
        AccessTokenTracker tracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {

            }
        };

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                displayWelcomeMessage(newProfile);
            }
        };


        tracker.startTracking();
        profileTracker.startTracking();
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
        tvIdexTitle = (TextView) v.findViewById(R.id.tvIdexTitle);
        etUsername = (TextView) v.findViewById(R.id.etUsername);
        etPassword = (TextView) v.findViewById(R.id.etPassword);
        tvContinue = (TextView) v.findViewById(R.id.tvContinue);
        tvIdexTitle.setOnClickListener(this);
        imgbLogin.setOnClickListener(this);
        imgbRegister.setOnClickListener(this);
        tvContinue.setOnClickListener(this);

        tvUsername = (TextView) v.findViewById(R.id.tvUsername);
        tvPassword = (TextView) v.findViewById(R.id.tvPassword);

        Typeface osFont = Typeface.createFromAsset(getActivity().getAssets(), "tt.otf");

        tvContinue.setTypeface(osFont);
        tvPassword.setTypeface(osFont);
        tvUsername.setTypeface(osFont);

        // Remove toolbar from login activity
        MainActivity.rLayoutMain.setPadding(0, 0, 0, 0);
        userLocalStore = new UserLocalStore(getActivity());
        userLocalStore.clearUserData(); // Clear the last logged in user before storing the new one
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbLogin:
                String   myAndroidDeviceId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
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
            case R.id.tvIdexTitle:
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new AboutFragment()).commit();
                MainActivity.listView.setItemChecked(2, true);
                break;
        }
    }

    // Authenticate that the correct user is trying to log in
    private void authenticate(User user){
        ServerRequests serverRequests = new ServerRequests(getActivity());
        serverRequests.logUserInDataInBackground(user, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                // Wrong username and password
                ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo  = connectivityManager.getActiveNetworkInfo();

                if (networkInfo == null) {
                    showNoInternetError();
                } else if(returnedUser == null){
                    showNoUserErrorMessage();
                }else{
                    userLocalStore.storeUserData(returnedUser);
                    UserLocalStore.isUserLoggedIn = true;
                    UserLocalStore.allowRefresh = true;
                    clickActivity.setRollClicked();
                    logUserIn();
                }
            }
        });
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("basic_info");
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mCallback);

        // Change title font
        Typeface myTypeface = Typeface.createFromAsset(getActivity().getAssets(), "Starjhol.ttf" );
        TextView idexTitle = (TextView) view.findViewById(R.id.tvIdexTitle);
        idexTitle.setTypeface(myTypeface);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        displayWelcomeMessage(profile);
    }

    private void displayWelcomeMessage(Profile profile){
        if(profile != null){
            mTextDetails.setText("Welcome " + profile.getName());
        }
    }
}
