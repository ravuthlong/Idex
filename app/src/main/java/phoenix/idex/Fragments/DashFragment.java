package phoenix.idex.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.ButtonClickedSingleton;
import phoenix.idex.R;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.User;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyMainPosts;


/**
 * Created by Ravinder on 2/23/16.
 */

public class DashFragment extends Fragment implements TextWatcher, View.OnClickListener {

    private  EditText editText;
    private Toolbar toolbardash;
    private TextView wordCounter;
    private ImageButton imgbPostIdea;
    private UserLocalStore userLocalStore;
    private ServerRequests serverRequests;
    private VolleyMainPosts volleyMainPosts;
    private Button bDashRoll, bDashLog, bDashInfo;
    private ButtonClickedSingleton buttonMonitor = ButtonClickedSingleton.getInstance();
    private FragmentManager fragmentManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_dash, container, false);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        toolbardash = (Toolbar) v.findViewById(R.id.toolbar);
        editText = (EditText) v.findViewById(R.id.editTextDash);
        wordCounter = (TextView) v.findViewById(R.id.wordCounter);
        imgbPostIdea = (ImageButton) v.findViewById(R.id.imgbPostIdea);
        bDashRoll = (Button) v.findViewById(R.id.bDashRoll);
        bDashLog = (Button) v.findViewById(R.id.bDashLog);
        bDashInfo = (Button) v.findViewById(R.id.bDashInfo);

        buttonMonitor.setUpButtons(bDashRoll, bDashLog, bDashInfo);

        /*
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
        }*/


        userLocalStore = new UserLocalStore(getContext());
        serverRequests = new ServerRequests(getContext());
        volleyMainPosts = new VolleyMainPosts(getContext());
        editText.addTextChangedListener(this);
        imgbPostIdea.setOnClickListener(this);
        bDashInfo.setOnClickListener(this);
        bDashLog.setOnClickListener(this);
        bDashRoll.setOnClickListener(this);
        return v;

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgbPostIdea:
                String unixTime = Long.toString(System.currentTimeMillis());
                User user = userLocalStore.getLoggedInUser();

                volleyMainPosts.storeAPostVolley(editText.getText().toString(),
                        user.getUserID(), unixTime);

            case R.id.bDashRoll:
                buttonMonitor.cancelClicks(bDashRoll, bDashLog, bDashInfo);
                buttonMonitor.setRollClicked();
                bDashRoll.setBackgroundResource(R.drawable.rolled);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new PostListFragment()).commit();
                break;

            case R.id.bDashLog:
                buttonMonitor.cancelClicks(bDashRoll, bDashLog, bDashInfo);
                buttonMonitor.setLogClicked();
                bDashLog.setBackgroundResource(R.drawable.logged);
                break;
            case R.id.bDashInfo:
                buttonMonitor.cancelClicks(bDashRoll, bDashLog, bDashInfo);
                buttonMonitor.setInfoClicked();
                bDashInfo.setBackgroundResource(R.drawable.infoed);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.rLayoutMain,
                        new TabFragment()).commit();
                MainActivity.listView.setItemChecked(1, true);
                break;
        }
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

}
