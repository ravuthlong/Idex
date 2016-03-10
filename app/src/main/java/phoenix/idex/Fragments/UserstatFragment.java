package phoenix.idex.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import phoenix.idex.R;
import phoenix.idex.UserLocalStore;


/**
 * Created by Ravinder on 2/23/16.
 */
public class UserstatFragment extends Fragment {

    private TextView etLoggedInUser;
    private UserLocalStore userLocalStore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_user_stats, container, false);

        etLoggedInUser = (TextView) v.findViewById(R.id.etloggedInUser);

        userLocalStore = new UserLocalStore(getActivity());

        etLoggedInUser.setText(userLocalStore.getLoggedInUser().getUsername());

        return v;
    }

}
