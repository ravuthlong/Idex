package phoenix.idex.RecyclerViewFeed.MainRecyclerView.listener;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import phoenix.idex.Activities.MainActivity;
import phoenix.idex.R;
import phoenix.idex.UserLocalStore;

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

    // Method will be called upon scroll in the post recycler view
    // Sets visibility of the floating post widget in main screen
    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed);

        if (dyConsumed > 0 && child.getVisibility() == View.VISIBLE) {
            child.hide();
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            if (UserLocalStore.isUserLoggedIn) {
                child.show();
            } else {
                child.hide();
            }
        }
    }
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if(dependency instanceof ListView)
            return true;
        return false;
    }
}
