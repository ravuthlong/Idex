package phoenix.idex.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.R;
import phoenix.idex.ServerRequestCallBacks.JSONObjectCallBack;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyUserInfo;


/**
 * Created by Ravinder on 2/23/16.
 */
public class UserstatFragment extends Fragment implements View.OnClickListener {

    private TextView etLoggedInUser, etSignedUpDate, etSinceSignedUp, etTotalIdeas, etTotalKills,
            etTotalFills, etFillRate;
    private UserLocalStore userLocalStore;
    private Button bStatLog, bStatRoll, bStatInfo;
    private ButtonClickedSingleton buttonMonitor = ButtonClickedSingleton.getInstance();
    private FragmentManager fragmentManager;
    private VolleyUserInfo volleyUserInfo;
    private int totalFills, totalKills = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_user_stats, container, false);

        setUpVariables(v);
        String user = userLocalStore.getLoggedInUser().getFirstname() + " "
                + userLocalStore.getLoggedInUser().getLastname();
        etLoggedInUser.setText(user);

        String signupFormatted = "";

        String daysAgo = "";
        try {
            SimpleDateFormat formatStandard = new SimpleDateFormat("MM/dd/yyyy",  Locale.getDefault());
            //Date signedup = formatStandard.parse(userLocalStore.getLoggedInUser().getTime());

            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy",  Locale.getDefault());
            Date past = format.parse(userLocalStore.getLoggedInUser().getTime());
            Date now = new Date();

            signupFormatted = formatStandard.format(past);

            daysAgo = TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime()) + "";
        }
        catch (Exception j){
            j.printStackTrace();
        }
        etSignedUpDate.setText(signupFormatted);
        etSinceSignedUp.setText(daysAgo + " days ago");
        buttonMonitor.setUpButtons(bStatRoll, bStatLog, bStatInfo);


        // Set retrieved stats about user's posts and member status
        volleyUserInfo.fetchPostStat(userLocalStore.getLoggedInUser().getUserID(), new JSONObjectCallBack() {
            @Override
            public void returnedJSONObject(JSONObject jsonObject) {
                try {
                    etTotalIdeas.setText(String.valueOf(jsonObject.getInt("postCount")));

                    if (jsonObject.isNull("totalFill")) {
                        totalFills = 0;
                        etTotalFills.setText("0");
                    } else {
                        totalFills = jsonObject.getInt("totalFill");
                        etTotalFills.setText(String.valueOf(totalFills));
                    }

                    if (jsonObject.isNull("totalKill")) {
                        totalKills = 0;
                        etTotalKills.setText("0");
                    } else {
                        totalKills = jsonObject.getInt("totalKill");
                        etTotalKills.setText(String.valueOf(totalKills));
                    }

                    if (totalKills > 0) {
                        double fillRate = ((totalFills * 1.0) / totalKills) * 100.0;
                        NumberFormat formatter = new DecimalFormat("#0.00");
                        etFillRate.setText(formatter.format(fillRate) + "%");
                    } else {
                        etFillRate.setText("0%");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bStatRoll:
                buttonMonitor.cancelClicks(bStatRoll, bStatLog, bStatInfo);
                buttonMonitor.setRollClicked();
                bStatRoll.setBackgroundResource(R.drawable.rolled);

                fragmentManager = getParentFragment().getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new PostListFragment()).commit();
                break;
            case R.id.bStatLog:
                buttonMonitor.cancelClicks(bStatRoll, bStatLog, bStatInfo);
                buttonMonitor.setLogClicked();
                bStatLog.setBackgroundResource(R.drawable.logged);

                fragmentManager = getParentFragment().getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new DashFragment()).commit();
                break;
            case R.id.bStatInfo:
                break;
        }
    }

    private void setUpVariables(View v) {
        etLoggedInUser = (TextView) v.findViewById(R.id.etLoggedInUser);
        etSignedUpDate = (TextView) v.findViewById(R.id.etSignedUpDate);
        etSinceSignedUp = (TextView) v.findViewById(R.id.etSinceSignedUp);
        etTotalIdeas = (TextView) v.findViewById(R.id.etTotalIdeas);
        etTotalKills = (TextView) v.findViewById(R.id.etTotalKills);
        etTotalFills = (TextView) v.findViewById(R.id.etTotalFills);
        etFillRate = (TextView) v.findViewById(R.id.etFillRate);

        bStatRoll = (Button) v.findViewById(R.id.bStatRoll);
        bStatLog = (Button) v.findViewById(R.id.bStatLog);
        bStatInfo = (Button) v.findViewById(R.id.bStatInfo);

        bStatRoll.setOnClickListener(this);
        bStatLog.setOnClickListener(this);
        bStatInfo.setOnClickListener(this);
        userLocalStore = new UserLocalStore(getContext());
        volleyUserInfo = new VolleyUserInfo(getContext());
    }
}