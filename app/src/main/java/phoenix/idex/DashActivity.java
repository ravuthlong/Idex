package phoenix.idex;

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
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by Ravinder on 2/23/16.
 */

public class DashActivity extends AppCompatActivity implements TextWatcher {

    EditText editText;
    Toolbar toolbardash;
    TextView wordCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);

        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        toolbardash = (Toolbar) findViewById(R.id.toolbardash);
        editText = (EditText) findViewById(R.id.editTextDash);
        wordCounter = (TextView) findViewById(R.id.wordCounter);

        setSupportActionBar(toolbardash);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Post");

        toolbardash.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        editText.addTextChangedListener(this);
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
