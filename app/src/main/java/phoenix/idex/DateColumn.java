package phoenix.idex;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by Ravinder on 3/20/16.
 */

public class DateColumn {

    public static int getRowNumber() {

        final java.util.Date currentTime = new java.util.Date();
        final SimpleDateFormat sdf =
                new SimpleDateFormat("dd");

        // Give it to me in GMT time.
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        int column = Integer.parseInt(sdf.format(currentTime));

        return ((column % 10) + 1);
    }
}
