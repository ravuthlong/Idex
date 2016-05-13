package phoenix.idex.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import phoenix.idex.R;
import phoenix.idex.ServerRequestCallBacks.GetUserCallBack;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyUserInfo;


public class SignUpActivity extends AppCompatActivity {
    private VolleyUserInfo volleyUserInfo;
    EditText etFirstName, etLastName, etEmail, etUsername, etPassword, etConfirmPass;
    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSignup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }        setTitle("Sign Up");
        userLocalStore = new UserLocalStore(this);

        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPass = (EditText) findViewById(R.id.etConfirmPass);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_signup, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.submitSignup:
                UserLocalStore.allowRefresh = true;
                inputValidation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // Validate that user input is in correct format
    public void inputValidation() {

        String lastname = etLastName.getText().toString();
        String firstname = etFirstName.getText().toString();
        String email = etEmail.getText().toString();
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();

        String errorFields = "";

        if (lastname.matches("")) {
            errorFields += "Missing Last Name \n";
        } else if (!isAlpha(lastname)) {
            errorFields += "Last Name must only contain letters\n";
        }
        if (firstname.matches("")) {
            errorFields += "Missing First Name \n";
        } else if (!isAlpha(firstname)) {
            errorFields += "First Name must only contain letters\n";
        }
        if (username.matches("")) {
            errorFields += "Missing Username \n";
        }
        if (password.matches("")) {
            errorFields += "Missing Password \n";
        } else if (!passwordLength(password)) {
            errorFields += "Password must be at least 6 characters long\n";
        }
        if (email.matches("")) {
            errorFields += "Missing Email \n";
        } else if (!isEmailValid(email)) {
            errorFields += "Email is in invalid format (Ex. xxxx@xxx.xxx)\n";
        }
        if(!etConfirmPass.getText().toString().equals(etPassword.getText().toString())) {
            errorFields += "Password must match \n";
        }
        if (errorFields.length() > 0) {
            Toast toast= Toast.makeText(getApplicationContext(),
                    errorFields, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
        if (errorFields.matches("")) {
            String timeStamp = new SimpleDateFormat("dd/MM/yyyy",  Locale.getDefault()).format(Calendar.getInstance().getTime());
            User user = new User(firstname, lastname, email, username, password, timeStamp);
            registerUser(user);
        }
    }

    public void registerUser(User user) {
        volleyUserInfo = new VolleyUserInfo(this);
        volleyUserInfo.storeUserInfo(user, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                userLocalStore.storeUserData(returnedUser);
                UserLocalStore.isUserLoggedIn = true;
                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
            }
        });
    }

    // Validate password length, must be 6 or more
    public static boolean passwordLength(String password) {
        boolean isValid = true;
        final int passwordLengthMin = 6;

        if (password.length() < passwordLengthMin) {
            isValid = false;
        }
        return isValid;
    }
    // Validate email input format
    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    // Validate string must only contain letters
    public static boolean isAlpha(String text) {
        char[] chars = text.toCharArray();
        boolean isValid = true;

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                isValid = false;
            }
        }
        return isValid;
    }
}