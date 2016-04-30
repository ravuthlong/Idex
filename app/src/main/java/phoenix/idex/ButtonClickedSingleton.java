package phoenix.idex;

import android.widget.Button;

/**
 * Created by Ravinder on 4/28/16.
 */
public class ButtonClickedSingleton {
    private static final ButtonClickedSingleton instance = new ButtonClickedSingleton();
    private boolean isRollClicked = false;
    private boolean isLogClicked = false;
    private boolean isInfoClicked = false;


    public static ButtonClickedSingleton getInstance() {
        return instance;
    }

    public void setRollClicked() {
        isRollClicked = true;
    }
    public void setLogClicked() {
        isLogClicked = true;
    }
    public void setInfoClicked() {
        isInfoClicked = true;
    }

    public void cancelClicks(Button bRoll, Button bLog, Button bInfo) {
        isRollClicked = false;
        isLogClicked = false;
        isInfoClicked = false;
        bRoll.setBackgroundResource(R.drawable.roll);
        bLog.setBackgroundResource(R.drawable.log);
        bInfo.setBackgroundResource(R.drawable.info);
    }

    public void cancelClicks() {
        isRollClicked = false;
        isLogClicked = false;
        isInfoClicked = false;
    }

    public int getCurrentClick() {
        if (isRollClicked) {
            return 1;
        } else if (isLogClicked) {
            return 2;
        } else if (isInfoClicked) {
            return 3;
        }
        return 0;
    }

    public void setUpButtons(Button bRoll, Button bLog, Button bInfo) {
        int currentClicked = getCurrentClick();

        if (currentClicked == 1) {
            bRoll.setBackgroundResource(R.drawable.rolled);
        } else if (currentClicked == 2) {
            bLog.setBackgroundResource(R.drawable.logged);
        } else if (currentClicked == 3) {
            bInfo.setBackgroundResource(R.drawable.infoed);
        }

    }


}
