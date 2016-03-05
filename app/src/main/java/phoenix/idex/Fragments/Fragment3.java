package phoenix.idex.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import phoenix.idex.R;

/**
 * Created by Ravinder on 2/19/16.
 */
public class Fragment3 extends android.support.v4.app.Fragment {

    public Fragment3() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_3, container, false);
        return rootView;
    }
}
