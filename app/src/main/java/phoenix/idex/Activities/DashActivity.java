package phoenix.idex.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import phoenix.idex.VolleyServerConnections.VolleyConnections;
import phoenix.idex.R;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;


/**
 * Created by Ravinder on 2/23/16.
 */

public class DashActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    private  EditText editText;
    private Toolbar toolbardash;
    private TextView wordCounter;
    private ImageButton imgbPostIdea;
    private UserLocalStore userLocalStore;
    private ServerRequests serverRequests;
    private VolleyConnections volleyConnections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);

        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        toolbardash = (Toolbar) findViewById(R.id.toolbardash);
        editText = (EditText) findViewById(R.id.editTextDash);
        wordCounter = (TextView) findViewById(R.id.wordCounter);
        imgbPostIdea = (ImageButton) findViewById(R.id.imgbPostIdea);


        setSupportActionBar(toolbardash);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (toolbardash != null) {
            toolbardash.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        userLocalStore = new UserLocalStore(this);
        serverRequests = new ServerRequests(this);
        volleyConnections = new VolleyConnections(this);
        editText.addTextChangedListener(this);
        imgbPostIdea.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbPostIdea:
                String unixTime = Long.toString(System.currentTimeMillis());
                User user = userLocalStore.getLoggedInUser();

                volleyConnections.storeAPostVolley(editText.getText().toString(),
                        user.getUserID(), unixTime);
                /*
                serverRequests.storeAPostInBackground(editText.getText().toString(),
                        user.getUserID(), unixTime);*/

                // move this to volley

                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        wordCounter.setText(String.valueOf(s.length()));
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.useraccount_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.editAccount_setting:
                Toast.makeText(this, "Change User Settings...", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
