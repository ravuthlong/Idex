package phoenix.idex.Fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import phoenix.idex.MainActivity;
import phoenix.idex.R;

/**
 * Created by Ravinder on 2/19/16.
 */
public class AboutFragment extends android.support.v4.app.Fragment {

    private TextView tvInfo;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_about, container, false);

        int sizeOfActionBar = MainActivity.getThemeAttributeDimensionSize(getActivity(), R.attr.actionBarSize);
        MainActivity.rLayoutMain.setPadding(0, sizeOfActionBar, 0, 0);

        tvInfo = (TextView) rootView.findViewById(R.id.tvInfo);
        Typeface osFont = Typeface.createFromAsset(getActivity().getAssets(), "tt.otf");
        tvInfo.setTypeface(osFont);

        return rootView;
    }


}
