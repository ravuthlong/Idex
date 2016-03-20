package phoenix.idex.Graphing;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.androidplot.xy.BarRenderer;
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
public class GraphActivity extends AppCompatActivity {
    private XYPlot xyPlot;
    private Number[] fill, kill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_graph);

        Bundle extras = getIntent().getExtras();

        fill = new Number[15];
        kill = new Number[15];
        if(extras != null){
            for(int i = 0; i<15; i++){
                fill[i] = extras.getInt("fill") + i;
                kill[i] = extras.getInt("kill") + i;
            }
        }

        xyPlot = (XYPlot) findViewById(R.id.xyplot);

        // Reading for the database
        Number[] fillOverTime = fill;
        Number[] killOverTime = kill;
        //Number[] fillOverTime = {10, 20, 50,70, 20, 30, 50, 20};
        //Number[] killOverTime = {10,15,12,11,14,17,16,12};
        final String[] timeInterval = new String[15];
        for(int i = 0; i<15; i++){
            timeInterval[i] =  "[" + String.valueOf(i+1) + "]";
        }

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
                Color.RED, Color.YELLOW, null, null);

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
        xyPlot.getGraphWidget().setPadding(2,2,2,2);
        xyPlot.setDomainLabel("Time");
        xyPlot.setRangeLabel("Amount");
        xyPlot.setTicksPerRangeLabel(3);
        // Increment X-Axis by 1 value
        xyPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);

        // Reduce the number of range labels
        xyPlot.setTicksPerRangeLabel(2);

        BarRenderer renderer = (BarRenderer) xyPlot.getRenderer(BarRenderer.class);
    }

}