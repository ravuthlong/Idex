package phoenix.idex.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import phoenix.idex.R;


/**
 * Created by Ravinder on 2/23/16.
 */
public class UserstatFragment extends android.support.v4.app.Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_user_stats, container, false);
        return v;
    }

}
