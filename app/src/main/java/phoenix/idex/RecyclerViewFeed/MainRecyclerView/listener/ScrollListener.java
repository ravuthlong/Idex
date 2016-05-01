package phoenix.idex.RecyclerViewFeed.MainRecyclerView.listener;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.R;

/**
 * Created by Ravinder on 2/21/16.
 */

// Class called in post_recyclerview XML
public class ScrollListener extends FloatingActionButton.Behavior {

    int sizeOfActionBar;
    public ScrollListener(Context context, AttributeSet attrs) {
        super();
    }

    public boolean onStartNestedScroll(CoordinatorLayout parent, FloatingActionButton child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        sizeOfActionBar = MainActivity.getThemeAttributeDimensionSize(parent.getContext(), R.attr.actionBarSize);

        return true;
    }

}
