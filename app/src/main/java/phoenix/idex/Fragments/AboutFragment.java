package phoenix.idex.Fragments;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import phoenix.idex.Activities.MainActivity;
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

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                System.out.println("SMALL SCREEN");
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                System.out.println("NORMAL SCREEN");
                MainActivity.rLayoutMain.setPadding(0, 290, 0, 0);

                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                System.out.println("LARGE SCREEN");
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                System.out.println("X-LARGE SCREEN");
                break;
            default:
                break;
        }

        tvInfo = (TextView) rootView.findViewById(R.id.tvInfo);
        Typeface osFont = Typeface.createFromAsset(getActivity().getAssets(), "tt.otf");
        tvInfo.setTypeface(osFont);

        return rootView;
    }


}
