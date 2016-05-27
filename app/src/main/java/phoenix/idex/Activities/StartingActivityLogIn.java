package phoenix.idex.Activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.ProfileTracker;

import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.R;
import phoenix.idex.ServerRequestCallBacks.GetUserCallBack;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyGCM;
import phoenix.idex.VolleyServerConnections.VolleyUserInfo;

/**
 * Created by Ravinder on 5/26/16.
 */
public class StartingActivityLogIn extends AppCompatActivity implements View.OnClickListener  {
    private TextView mTextDetails;
    private CallbackManager mCallbackManager;
    private ImageButton imgbRegister, imgbLogin;
    private ImageView imgLogo;
    private Button bRegister, bLogin, bBrowseIdea;
    private AccessTokenTracker mTokenTracker;
    private ProfileTracker mProfileTracker;
    private View v;
    private int sizeOfActionBar;
    private RelativeLayout rLayoutMain;
    private UserLocalStore userLocalStore;
    private TextView etUsername, etPassword, tvIdexTitle, tvContinue, tvUsername, tvPassword;
    private android.support.v7.widget.Toolbar toolbar;
    private FragmentManager fragmentManager;
    private ButtonClickedSingleton clickActivity = ButtonClickedSingleton.getInstance();
    private VolleyUserInfo volleyUserInfo;
    private VolleyGCM volleyGCM;
    private BroadcastReceiver registrationBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.frag_login);


        imgbRegister = (ImageButton) findViewById(R.id.imgbRegister);
        imgbLogin = (ImageButton) findViewById(R.id.imgbLogin);
        etUsername = (TextView) findViewById(R.id.etUsername);
        etPassword = (TextView) findViewById(R.id.etPassword);
        tvContinue = (TextView) findViewById(R.id.tvContinue);
        imgLogo = (ImageView) findViewById(R.id.imgLogo);

        userLocalStore = new UserLocalStore(this);
        imgbLogin.setOnClickListener(this);
        imgbRegister.setOnClickListener(this);
        tvContinue.setOnClickListener(this);
        imgLogo.setOnClickListener(this);

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvPassword = (TextView) findViewById(R.id.tvPassword);

        Typeface osFont = Typeface.createFromAsset(this.getAssets(), "tt.otf");

        tvContinue.setTypeface(osFont);
        tvPassword.setTypeface(osFont);
        tvUsername.setTypeface(osFont);

        userLocalStore = new UserLocalStore(this);
        volleyGCM = new VolleyGCM(this);

        //userLocalStore.clearUserData(); // Clear the last logged in user before storing the new one
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbLogin:
                String  myAndroidDeviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                System.out.println("THE DEVICE ID IS : " + myAndroidDeviceId);
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                User user = new User(username, password);
                authenticate(user);
                break;
            case R.id.imgbRegister:
                Intent signUpIntent = new Intent(this, SignUpActivity.class);
                startActivity(signUpIntent);
                break;
            case R.id.tvContinue:
                clickActivity.setRollClicked();
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.imgLogo:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
    }

    // Authenticate that the correct user is trying to log in
    private void authenticate(User user){
        volleyUserInfo = new VolleyUserInfo(this);

        volleyUserInfo.fetchUserInfo(user, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                // Wrong username and password
                ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("Incorrect user details");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }

    // Error if the user info is incorrect
    private void showNoInternetError(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("You are not connected to the internet");
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();
    }


    // If the log in info is correct, store user in local store and show MainActivity
    private void logUserIn(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //It is use to finish current activity
        startActivity(intent);

    }


}
