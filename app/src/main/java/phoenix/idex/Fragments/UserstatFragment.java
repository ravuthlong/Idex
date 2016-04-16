package phoenix.idex.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import phoenix.idex.Activities.DashActivity;
import phoenix.idex.Activities.MainActivity;
import phoenix.idex.R;
import phoenix.idex.UserLocalStore;


/**
 * Created by Ravinder on 2/23/16.
 */
public class UserstatFragment extends Fragment implements View.OnClickListener {

    private TextView etLoggedInUser;
    private UserLocalStore userLocalStore;
    private Button bStatDash, bStatRoll, bStatInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_user_stats, container, false);

        etLoggedInUser = (TextView) v.findViewById(R.id.etloggedInUser);

        bStatRoll = (Button) v.findViewById(R.id.bStatRoll);
        bStatDash = (Button) v.findViewById(R.id.bStatDash);
        bStatInfo = (Button) v.findViewById(R.id.bStatInfo);

        bStatRoll.setOnClickListener(this);
        bStatDash.setOnClickListener(this);
        bStatInfo.setOnClickListener(this);
        userLocalStore = new UserLocalStore(getActivity());

        etLoggedInUser.setText(userLocalStore.getLoggedInUser().getUsername());
        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bStatRoll:
                startActivity(new Intent(getContext(), MainActivity.class));
                break;
            case R.id.bStatDash:
                getContext().startActivity(new Intent(getActivity(), DashActivity.class));
                break;
            case R.id.bStatInfo:
                break;
        }
    }
}