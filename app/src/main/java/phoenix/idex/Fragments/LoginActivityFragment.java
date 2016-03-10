package phoenix.idex.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import phoenix.idex.MainActivity;
import phoenix.idex.R;
import phoenix.idex.ServerConnections.GetUserCallBack;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.SignUpActivity;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment implements View.OnClickListener {
    private TextView mTextDetails;
    private CallbackManager mCallbackManager;
    private Button bRegister;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    private View v;
    private Button bLogin;
    private UserLocalStore userLocalStore;
    private TextView etUsername, etPassword;

    private android.support.v7.widget.Toolbar toolbar;


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
        bRegister = (Button) v.findViewById(R.id.bRegister);
        bLogin = (Button) v.findViewById(R.id.bLogin);
        etUsername = (TextView) v.findViewById(R.id.etUsername);
        etPassword = (TextView) v.findViewById(R.id.etPassword);

        bLogin.setOnClickListener(this);
        bRegister.setOnClickListener(this);

        userLocalStore = new UserLocalStore(getActivity());

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));

        return v;
    }


/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bLogin:
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                User user = new User(username, password);
                authenticate(user);
                break;
            case R.id.bRegister:
                Intent signUpIntent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(signUpIntent);
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
                if(returnedUser == null){
                    showErrorMessage();
                }else{
                    userLocalStore.storeUserData(returnedUser);
                    userLocalStore.setUserLoggedIn(true);
                    logUserIn();
                }
            }
        });
    }

    // Error if the user info is incorrect
    private void showErrorMessage(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setMessage("Incorrect user details");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    // If the log in info is correct, store user in local store and show MainActivity
    private void logUserIn(){
        startActivity(new Intent(getActivity(), MainActivity.class));

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
        TextView idexTitle = (TextView) view.findViewById(R.id.idexTitle);
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
