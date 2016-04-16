package phoenix.idex.Graphing;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;

import java.util.ArrayList;

import phoenix.idex.VolleyServerConnections.VolleyConnections;
import phoenix.idex.R;
import phoenix.idex.ServerRequestCallBacks.GraphInfoCallBack;

/**
 * Created by Ravinder on 2/19/16.
 */
public class GraphActivity extends AppCompatActivity {
    private CandleStickChart mChart;
    private VolleyConnections volleyConnections;
    private ProgressDialog progressDialog;

    class FillKillObject {
        private int fill;
        private int kill;

        FillKillObject(int fill, int kill) {
            this.fill = fill;
            this.kill = kill;
        }

        int getFill() {
            return fill;
        }
        int getKill() {
            return kill;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.frag_graph);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        mChart = (CandleStickChart) findViewById(R.id.chart);

        // *** Initialize chart here
        mChart.setNoDataText("");
        mChart.setDescription("Past Clicks");
        mChart.setMaxVisibleValueCount(60); // max 60 entries

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(7, false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        mChart.getLegend().setEnabled(false);

        volleyConnections = new VolleyConnections(this);
        Bundle extra = getIntent().getExtras();
        int postID = extra.getInt("postID");

        volleyConnections.fetchAGraph(postID, progressDialog, new GraphInfoCallBack() {
            @Override
            public void getGraphInfo(Graph graph) {
                setGraph(graph);
            }
        });


        // ** end initialize chart


        /*
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
        // Only plot if there are at least two points
        if (trendList.size() >= 2) {
            final String[] timeInterval = new String[10];
            for (int i = 0; i < 10; i++) {
                timeInterval[i] = "[" + String.valueOf(i + 1) + "]";
            }
        } else {
            Toast toast= Toast.makeText(getApplicationContext(),
                    "Not enough data", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }
        **/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.candle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionToggleHighlight: {
                if(mChart.getData() != null) {
                    mChart.getData().setHighlightEnabled(!mChart.getData().isHighlightEnabled());
                    mChart.invalidate();
                }
                break;
            }
            case R.id.actionTogglePinch: {
                if (mChart.isPinchZoomEnabled())
                    mChart.setPinchZoom(false);
                else
                    mChart.setPinchZoom(true);

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleAutoScaleMinMax: {
                mChart.setAutoScaleMinMaxEnabled(!mChart.isAutoScaleMinMaxEnabled());
                mChart.notifyDataSetChanged();
                break;
            }
            case R.id.actionToggleMakeShadowSameColorAsCandle: {
                for (ICandleDataSet set : mChart.getData().getDataSets()) {
                    //TODO: set.setShadowColorSameAsCandle(!set.getShadowColorSameAsCandle());
                }

                mChart.invalidate();
                break;
            }
            case R.id.animateX: {
                mChart.animateX(3000);
                break;
            }
            case R.id.animateY: {
                mChart.animateY(3000);
                break;
            }
            case R.id.animateXY: {

                mChart.animateXY(3000, 3000);
                break;
            }
            case R.id.actionSave: {
                if (mChart.saveToGallery("title" + System.currentTimeMillis(), 50)) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!",
                            Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                            .show();
                break;
            }
        }
        return true;
    }

    void setGraph(Graph graph) {
        // *** Note: Value will be CandleEntry.class
        ArrayList<CandleEntry> yVals1 = new ArrayList<>();

        /** Those are the parameter for the CandleEntry .. Kinda not really understand what each of these mean
         *
         * @param xIndex The index on the x-axis.
         * @param shadowH The (shadow) high value.
         * @param shadowL The (shadow) low value.
         * @param open
         * @param close
         * @param data Spot for additional data this Entry represents.
         */


        int numFill = graph.getTotalFillFloor();
        int numKill = graph.getTotalKillFloor();
        int value = graph.getValue();
        int nextInsert = graph.getCurrentColumn();

        int[] arrayFillKill = new int[20];
        arrayFillKill[0] = graph.getC1();
        arrayFillKill[1] = graph.getC2();
        arrayFillKill[2] = graph.getC3();
        arrayFillKill[3] = graph.getC4();
        arrayFillKill[4] = graph.getC5();
        arrayFillKill[5] = graph.getC6();
        arrayFillKill[6] = graph.getC7();
        arrayFillKill[7] = graph.getC8();
        arrayFillKill[8] = graph.getC9();
        arrayFillKill[9] = graph.getC10();
        arrayFillKill[10] = graph.getC11();
        arrayFillKill[11] = graph.getC12();
        arrayFillKill[12] = graph.getC13();
        arrayFillKill[13] = graph.getC14();
        arrayFillKill[14] = graph.getC15();
        arrayFillKill[15] = graph.getC16();
        arrayFillKill[16] = graph.getC17();
        arrayFillKill[17] = graph.getC18();
        arrayFillKill[18] = graph.getC19();
        arrayFillKill[19] = graph.getC20();

        int size = arrayFillKill.length;

        // loop  3-2 index to 0 index then loop n down to nextInsert - 1

        int[] sortedFillKill = new int[size];

        int y = 0;
        for (int i = nextInsert - 1; i < arrayFillKill.length; i++) {
            sortedFillKill[y] = arrayFillKill[i];
            y++;
        }

        for (int i = 0; i < nextInsert - 1; i++) {
            sortedFillKill[y] = arrayFillKill[i];
            y++;
        }

        System.out.println("SORTED");
        for (int i = 0; i < sortedFillKill.length; i++) {
            System.out.println(sortedFillKill[i]);
        }


        FillKillObject[] fillKillArray = new FillKillObject[size];

        fillKillArray[size-1] = new FillKillObject(numFill, numKill);

        // At the same time loop through array fill and kill to check 0 or 1
        for (int i = sortedFillKill.length - 2; i >= 0; i--) {

            if ((sortedFillKill[i] == 0 && sortedFillKill[i+1] == 0) || (sortedFillKill[i] == 1 && sortedFillKill[i+1] == 0)) {
                numKill--;
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if ((sortedFillKill[i] == 1 && sortedFillKill[i+1] == 1) || (sortedFillKill[i] == 0 && sortedFillKill[i+1] == 1)) {
                numFill--;
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if ((sortedFillKill[i] == 0 && sortedFillKill[i+1] == -1) ) {
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if (sortedFillKill[i] == -1 && sortedFillKill[i+1] == -1) {
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if (sortedFillKill[i] == -1 && sortedFillKill[i+1] == 1) {
                numFill--;
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else {
                fillKillArray[i] = new FillKillObject(0, 0);
            }
        }


        for (int i = 0; i < fillKillArray.length; i ++) {
            System.out.println("INDEX: " + i);
            System.out.print(fillKillArray[i].getFill() + ",");
            System.out.print(fillKillArray[i].getKill());
            System.out.println();
        }

        for (int i = 0; i < fillKillArray.length; i ++) {
            int valuePoint = 2 * fillKillArray[i].getFill() - fillKillArray[i].getKill();
            System.out.println("VALUE " + valuePoint);

            if (sortedFillKill[i] == 1 || sortedFillKill[i] == 0) {
                if (sortedFillKill[i] == 1) {
                    yVals1.add(new CandleEntry(i, valuePoint, valuePoint - 2, valuePoint - 2, valuePoint));
                } else if (sortedFillKill[i] == 0) {
                    yVals1.add(new CandleEntry(i, valuePoint, valuePoint + 1, valuePoint + 1, valuePoint));
                }
            } else {
                yVals1.add(new CandleEntry(i, 0, 0, 0, 0));
            }
        }


/*
        yVals1.add(new CandleEntry(0, 42, 41, 41, 42));
        yVals1.add(new CandleEntry(1, 43, 42, 42, 43));
        yVals1.add(new CandleEntry(2, 43, (float) 42.5, 43, (float) 42.5));
        yVals1.add(new CandleEntry(3, (float) 43.5, (float) 42.5, (float) 42.5, (float) 43.5));

*/
        ArrayList<String> xVals = new ArrayList<>();

        xVals.add("" + 1);
        xVals.add("" + 2);
        xVals.add("" + 3);
        xVals.add("" + 4);
        xVals.add("" + 5);
        xVals.add("" + 6);
        xVals.add("" + 7);
        xVals.add("" + 8);
        xVals.add("" + 9);
        xVals.add("" + 10);
        xVals.add("" + 11);
        xVals.add("" + 12);
        xVals.add("" + 13);
        xVals.add("" + 14);
        xVals.add("" + 15);
        xVals.add("" + 16);
        xVals.add("" + 17);
        xVals.add("" + 18);
        xVals.add("" + 19);
        xVals.add("" + 20);

        CandleDataSet ySet = new CandleDataSet(yVals1, "Data Set");


        ySet.setAxisDependency(AxisDependency.LEFT);
//        set1.setColor(Color.rgb(80, 80, 80));
        ySet.setShadowColor(Color.DKGRAY);
        ySet.setShadowWidth(0.7f);
        ySet.setDecreasingColor(Color.RED);
        ySet.setDecreasingPaintStyle(Paint.Style.FILL);
        ySet.setIncreasingColor(Color.rgb(122, 242, 84));
        ySet.setIncreasingPaintStyle(Paint.Style.STROKE);
        ySet.setNeutralColor(Color.BLUE);
        //set1.setHighlightLineWidth(1f);

        // *** Note here: all X-Axis and Y-Axis Data is stored into CandleData.class
        // ** include y-set
        CandleData data = new CandleData(xVals, ySet); // xaxis
        // then add to the chart
        mChart.setData(data);
        mChart.invalidate();
        progressDialog.dismiss();

    }
}