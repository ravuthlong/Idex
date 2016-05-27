package phoenix.idex.Activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import phoenix.idex.R;

/**
 * Created by Ravinder on 5/26/16.
 */
public class AboutActivity extends AppCompatActivity {

    private TextView tvInfoAbout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        tvInfoAbout = (TextView) findViewById(R.id.tvInfoAbout);
        toolbar = (Toolbar) findViewById(R.id.toolbarabout);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }        setTitle("About");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Typeface osFont = Typeface.createFromAsset(this.getAssets(), "tt.otf");
        tvInfoAbout.setTypeface(osFont);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
