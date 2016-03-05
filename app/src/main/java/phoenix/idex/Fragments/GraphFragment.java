package phoenix.idex.Fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;

import phoenix.idex.R;

/**
 * Created by Ravinder on 2/19/16.
 */
public class GraphFragment extends android.support.v4.app.Fragment {
    private XYPlot xyPlot;
    public GraphFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_graph, container, false);

        xyPlot = (XYPlot) v.findViewById(R.id.xyplot);

        Number[] fillOverTime = {10, 20, 50,70, 20, 30, 50, 20};
        Number[] killOverTime = {10,15,12,11,14,17,16,12};
        final String[] timeInterval = {"7:30", "8:00", "8:30", "9:00", "9:30", "10:00", "10:30", "11:00"};
        XYSeries fillSeries = new SimpleXYSeries(
                Arrays.asList(fillOverTime),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Fill"
        );

        XYSeries killSeries = new SimpleXYSeries(
                Arrays.asList(killOverTime),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Kill"
        );

        LineAndPointFormatter fillFormat = new LineAndPointFormatter(
                Color.GREEN, Color.WHITE, null, null);

        LineAndPointFormatter killFormat = new LineAndPointFormatter(
                Color.BLUE, Color.YELLOW, null, null);

        xyPlot.addSeries(fillSeries, fillFormat);
        xyPlot.addSeries(killSeries, killFormat);

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
        xyPlot.setDomainLabel("Time");
        xyPlot.setRangeLabel("Amount");
        xyPlot.setTicksPerRangeLabel(3);
        // Increment X-Axis by 1 value
        xyPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);

        // Reduce the number of range labels
        xyPlot.setTicksPerRangeLabel(2);


        return v;
    }
}
