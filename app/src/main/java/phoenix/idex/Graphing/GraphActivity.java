package phoenix.idex.Graphing;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;

import phoenix.idex.ArrayListTools;
import phoenix.idex.R;

/**
 * Created by Ravinder on 2/19/16.
 */
public class GraphActivity extends AppCompatActivity {
    private XYPlot xyPlot;
    private Number[] trendLine;
    private ArrayListTools arrayListTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_graph);

        Bundle extras = getIntent().getExtras();

        ArrayList<Integer> fillArray = extras.getIntegerArrayList("fillArray");
        ArrayList<Integer> killArray = extras.getIntegerArrayList("killArray");

        ArrayList<Integer> trendList;

        arrayListTools = new ArrayListTools(fillArray, killArray);
        arrayListTools.sortFillArrayList();
        arrayListTools.sortKillArrayList();
        arrayListTools.sumSortedFillArray();
        arrayListTools.sumSortedKillArray();
        trendList = arrayListTools.getGraphList();

        trendLine = new Number[10];

        int j = 0;
        for (int value: trendList) {
            Integer intObj = value;
            trendLine[j++] = intObj;
        }

        xyPlot = (XYPlot) findViewById(R.id.xyplot);

        // Only plot if there are at least two points
        if (trendList.size() >= 2) {

            final String[] timeInterval = new String[10];
            for (int i = 0; i < 10; i++) {
                timeInterval[i] = "[" + String.valueOf(i + 1) + "]";
            }

            XYSeries trendSeries = new SimpleXYSeries(
                    Arrays.asList(trendLine),
                    SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Trend Line"
            );

            LineAndPointFormatter fillFormat = new LineAndPointFormatter(
                    Color.GREEN, Color.WHITE, null, null);
            xyPlot.addSeries(trendSeries, fillFormat);
            xyPlot.setDomainValueFormat(new Format() {

                @Override
                public StringBuffer format(Object object, StringBuffer buffer, FieldPosition field) {
                    return new StringBuffer(timeInterval[((Number) object).intValue()]);
                }

                @Override
                public Object parseObject(String string, ParsePosition position) {
                    return null;
                }
            });
            xyPlot.getGraphWidget().setPadding(2, 2, 2, 2);
            xyPlot.setDomainLabel("DAY");
            xyPlot.setRangeLabel("RATE OF CHANGE");
            xyPlot.setTicksPerRangeLabel(3);
            // Increment X-Axis by 1 value
            xyPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);

            // Reduce the number of range labels
            xyPlot.setTicksPerRangeLabel(2);

            BarRenderer renderer = (BarRenderer) xyPlot.getRenderer(BarRenderer.class);
        } else {
            Toast toast= Toast.makeText(getApplicationContext(),
                    "Not enough data", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
    }

}