package phoenix.idex.Activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import phoenix.idex.R;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyUserInfo;

/**
 * Created by Ravinder on 5/6/16.
 */
public class EditPasswordActivity extends AppCompatActivity implements View.OnClickListener  {

    private EditText etOldPassword, etNewPassword, etNewPassword2;
    private ImageButton imgbSavePassword;
    private VolleyUserInfo volleyUserInfo;
    private UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarEditPassword);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        etOldPassword = (EditText) findViewById(R.id.etOldPassword);
        etNewPassword = (EditText) findViewById(R.id.etNewPassword);
        etNewPassword2 = (EditText) findViewById(R.id.etNewPassword2);
        imgbSavePassword = (ImageButton) findViewById(R.id.imgbSavePassword);
        imgbSavePassword.setOnClickListener(this);
        volleyUserInfo = new VolleyUserInfo(this);
        userLocalStore = new UserLocalStore(this);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbSavePassword:

                String oldPassword = etOldPassword.getText().toString();
                String newPassword = etNewPassword.getText().toString();
                String newPassword2 = etNewPassword2.getText().toString();

                if (newPassword.equals(newPassword2)) {
                    volleyUserInfo.updatePassword(userLocalStore.getLoggedInUser().getUserID(),
                            oldPassword, newPassword);
                } else {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                    dialogBuilder.setMessage("New passwords don't match");
                    dialogBuilder.setPositiveButton("Ok", null);
                    dialogBuilder.show();
                }
                break;
        }

    }
}
