package phoenix.idex;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import phoenix.idex.ServerConnections.GetUserCallBack;
import phoenix.idex.ServerConnections.ServerRequests;

/*
 *https://github.com/wrapp/floatlabelededittext
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    ServerRequests serverRequests;
    User signedUpUser;
    Button bSignUp;
    EditText etFirstName, etLastName, etEmail, etUsername, etPassword;
    private  UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSignUp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Sign Up");
        userLocalStore = new UserLocalStore(this);


        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bSignUp = (Button) findViewById(R.id.bSignUp);

        bSignUp.setOnClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bSignUp:
                signedUpUser = new User(etFirstName.getText().toString(), etLastName.getText().toString(),
                        etEmail.getText().toString(), etUsername.getText().toString(), etPassword.getText().toString());


                serverRequests = new ServerRequests(this);
                serverRequests.storeUserDataInBackground(signedUpUser, new GetUserCallBack() {
                    @Override
                    public void done(User returnedUser) {
                        // Username has been taken and user info will not be stored
                        if (returnedUser == null) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignUpActivity.this);
                            dialogBuilder.setMessage("User has been taken");
                            dialogBuilder.setPositiveButton("Ok", null);
                            dialogBuilder.show();
                        } else {
                            userLocalStore.storeUserData(returnedUser);
                            userLocalStore.setUserLoggedIn(true);
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        }
                    }
                });
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
