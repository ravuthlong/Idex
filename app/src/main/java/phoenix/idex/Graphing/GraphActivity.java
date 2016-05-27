package phoenix.idex.Graphing;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import phoenix.idex.R;
import phoenix.idex.ServerRequestCallBacks.GraphInfoCallBack;
import phoenix.idex.VolleyServerConnections.VolleyGCM;
import phoenix.idex.VolleyServerConnections.VolleyMainPosts;

/**
 * Created by Ravinder on 2/19/16.
 */
public class GraphActivity extends AppCompatActivity {
    private CandleStickChart mChart;
    private VolleyMainPosts volleyMainPosts;
    private ProgressDialog progressDialog;
    private TextView tvCurrentValue, tvValueTxt, tvPressedValue, tvPressedPercent;
    private LinearLayout linearGraph3, linearGraph4;
    private ArrayList<CandleEntry> yVals1 = new ArrayList<>();

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
        setContentView(R.layout.activity_graph);

        tvCurrentValue = (TextView) findViewById(R.id.tvCurrentValue);
        tvValueTxt = (TextView) findViewById(R.id.tvValueTxt);
        tvPressedValue = (TextView) findViewById(R.id.tvPressedValue);
        linearGraph3 = (LinearLayout) findViewById(R.id.linearGraph3);
        linearGraph4 = (LinearLayout) findViewById(R.id.linearGraph4);
        tvPressedPercent = (TextView) findViewById(R.id.tvPercent);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        mChart = (CandleStickChart) findViewById(R.id.chart);

        // *** Initialize chart here
        mChart.setNoDataText("");
        mChart.setDescription("Past Clicks");
        mChart.setMaxVisibleValueCount(60); // max 60 entries

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                linearGraph3.setVisibility(View.VISIBLE);
                tvPressedValue.setText(Float.toString(Math.round(yVals1.get(e.getXIndex()).getHigh())));


                if (e.getXIndex() == 0) {
                    tvPressedPercent.setText("0.00%");
                } else if (((Math.round(yVals1.get(e.getXIndex()).getHigh()) == 0) &&
                        (Math.round(yVals1.get(e.getXIndex()).getOpen()) == 0)) || (Math.round(yVals1.get(e.getXIndex() - 1).getClose()) == 0)) {
                    linearGraph4.setVisibility(View.GONE);
                } else {
                    //double avg = (Math.round(yVals1.get(e.getXIndex() - 1).getHigh()) + Math.round(yVals1.get(e.getXIndex()).getHigh())) / 2.0;
                    double percent = ((yVals1.get(e.getXIndex()).getHigh() -
                           yVals1.get(e.getXIndex() - 1).getHigh()) / yVals1.get(e.getXIndex() - 1).getHigh()) * 100.0;
                    float f = (Math.round(yVals1.get(e.getXIndex() - 1).getHigh()));
                    linearGraph4.setVisibility(View.VISIBLE);

                    NumberFormat formatter = new DecimalFormat("#0.00");

                    tvPressedPercent.setText(formatter.format(percent) + "%");
                }

            }

            @Override
            public void onNothingSelected() {

            }
        });

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

        volleyMainPosts = new VolleyMainPosts(this);
        Bundle extra = getIntent().getExtras();
        int postID = extra.getInt("postID");

        volleyMainPosts.fetchAGraph(postID, progressDialog, new GraphInfoCallBack() {
            @Override
            public void getGraphInfo(Graph graph) {
                setGraph(graph);
                tvValueTxt.setVisibility(View.VISIBLE);
                tvCurrentValue.setText(Integer.toString(graph.getValue()));
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
        yVals1 = new ArrayList<>();

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

        int[] arrayFillKill = new int[100];
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
        arrayFillKill[20] = graph.getC21();
        arrayFillKill[21] = graph.getC22();
        arrayFillKill[22] = graph.getC23();
        arrayFillKill[23] = graph.getC24();
        arrayFillKill[24] = graph.getC25();
        arrayFillKill[25] = graph.getC26();
        arrayFillKill[26] = graph.getC27();
        arrayFillKill[27] = graph.getC28();
        arrayFillKill[28] = graph.getC29();
        arrayFillKill[29] = graph.getC30();
        arrayFillKill[30] = graph.getC31();
        arrayFillKill[31] = graph.getC32();
        arrayFillKill[32] = graph.getC33();
        arrayFillKill[33] = graph.getC34();
        arrayFillKill[34] = graph.getC35();
        arrayFillKill[35] = graph.getC36();
        arrayFillKill[36] = graph.getC37();
        arrayFillKill[37] = graph.getC38();
        arrayFillKill[38] = graph.getC39();
        arrayFillKill[39] = graph.getC40();
        arrayFillKill[40] = graph.getC41();
        arrayFillKill[41] = graph.getC42();
        arrayFillKill[42] = graph.getC43();
        arrayFillKill[43] = graph.getC44();
        arrayFillKill[44] = graph.getC45();
        arrayFillKill[45] = graph.getC46();
        arrayFillKill[46] = graph.getC47();
        arrayFillKill[47] = graph.getC48();
        arrayFillKill[48] = graph.getC49();
        arrayFillKill[49] = graph.getC50();
        arrayFillKill[50] = graph.getC51();
        arrayFillKill[51] = graph.getC52();
        arrayFillKill[52] = graph.getC53();
        arrayFillKill[53] = graph.getC54();
        arrayFillKill[54] = graph.getC55();
        arrayFillKill[55] = graph.getC56();
        arrayFillKill[56] = graph.getC57();
        arrayFillKill[57] = graph.getC58();
        arrayFillKill[58] = graph.getC59();
        arrayFillKill[59] = graph.getC60();
        arrayFillKill[60] = graph.getC61();
        arrayFillKill[61] = graph.getC62();
        arrayFillKill[62] = graph.getC63();
        arrayFillKill[63] = graph.getC64();
        arrayFillKill[64] = graph.getC65();
        arrayFillKill[65] = graph.getC66();
        arrayFillKill[66] = graph.getC67();
        arrayFillKill[67] = graph.getC68();
        arrayFillKill[68] = graph.getC69();
        arrayFillKill[69] = graph.getC70();
        arrayFillKill[70] = graph.getC71();
        arrayFillKill[71] = graph.getC72();
        arrayFillKill[72] = graph.getC73();
        arrayFillKill[73] = graph.getC74();
        arrayFillKill[74] = graph.getC75();
        arrayFillKill[75] = graph.getC76();
        arrayFillKill[76] = graph.getC77();
        arrayFillKill[77] = graph.getC78();
        arrayFillKill[78] = graph.getC79();
        arrayFillKill[79] = graph.getC80();
        arrayFillKill[80] = graph.getC81();
        arrayFillKill[81] = graph.getC82();
        arrayFillKill[82] = graph.getC83();
        arrayFillKill[83] = graph.getC84();
        arrayFillKill[84] = graph.getC85();
        arrayFillKill[85] = graph.getC86();
        arrayFillKill[86] = graph.getC87();
        arrayFillKill[87] = graph.getC88();
        arrayFillKill[88] = graph.getC89();
        arrayFillKill[89] = graph.getC90();
        arrayFillKill[90] = graph.getC91();
        arrayFillKill[91] = graph.getC92();
        arrayFillKill[92] = graph.getC93();
        arrayFillKill[93] = graph.getC94();
        arrayFillKill[94] = graph.getC95();
        arrayFillKill[95] = graph.getC96();
        arrayFillKill[96] = graph.getC97();
        arrayFillKill[97] = graph.getC98();
        arrayFillKill[98] = graph.getC99();
        arrayFillKill[99] = graph.getC100();

        int size = arrayFillKill.length;

        // loop  3-2 index to 0 index then loop n down to nextInsert - 1

        int[] sortedFillKill = new int[size];
        //= arrayFillKill;


        int noValueCount = 0;
        for (int i = 0; i < arrayFillKill.length; i++) {
            if (arrayFillKill[i] == -1) {
                noValueCount++;
                break;
            }
        }

        int y = 0;


        if (noValueCount != 0) {
            System.out.println("FOUND NO VALUE COUNT -1");
            for (int i = 0; i < nextInsert - 1; i++) {
                sortedFillKill[y] = arrayFillKill[i];
                y++;
            }

            for (int i = nextInsert - 1; i < arrayFillKill.length; i++) {
                sortedFillKill[y] = arrayFillKill[i];
                y++;
            }

        } else {
            System.out.println("CANNOT FIND NO VALUE COUNT");

            for (int i = nextInsert - 1; i < arrayFillKill.length; i++) {
                sortedFillKill[y] = arrayFillKill[i];
                y++;
            }
            for (int i = 0; i < nextInsert - 1; i++) {
                sortedFillKill[y] = arrayFillKill[i];
                y++;
            }


        }
        /*
        System.out.println("SORTED");
        for (int i = 0; i < sortedFillKill.length; i++) {
            System.out.println(sortedFillKill[i]);
        }*/


        FillKillObject[] fillKillArray = new FillKillObject[size];

        fillKillArray[size-1] = new FillKillObject(numFill, numKill);

        // At the same time loop through array fill and kill to check 0 or 1
        for (int i = sortedFillKill.length - 2; i >= 0; i--) {

            if ((sortedFillKill[i] == 0 && sortedFillKill[i+1] == 0) || (sortedFillKill[i] == 1 && sortedFillKill[i+1] == 0)) {
                //System.out.println("11111");
                numKill--;
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if ((sortedFillKill[i] == 1 && sortedFillKill[i+1] == 1) || (sortedFillKill[i] == 0 && sortedFillKill[i+1] == 1)) {
                //System.out.println("222222");

                numFill--;
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if ((sortedFillKill[i] == 0 && sortedFillKill[i+1] == -1) ) {
                //System.out.println("333333");

                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if (sortedFillKill[i] == -1 && sortedFillKill[i+1] == -1) {
                //System.out.println("444444");

                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if (sortedFillKill[i] == -1 && sortedFillKill[i+1] == 1) {
                //System.out.println("5555555");

                numFill--;
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if ((sortedFillKill[i] == 1 && sortedFillKill[i+1] == -1) )  {

                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if (sortedFillKill[i] == -2 && sortedFillKill[i+1] == 1) {
                System.out.println("LOL");
                System.out.println("IN HERE: " + sortedFillKill[i] + " NEXT: " + sortedFillKill[i+1]);
                numFill--;
                fillKillArray[i] = new FillKillObject(numFill, numKill);
            } else if (sortedFillKill[i] == -2 && sortedFillKill[i+1] == -2 || sortedFillKill[i] == 0 && sortedFillKill[i+1] == -2) {
                System.out.println("LOLz");
                //System.out.println("IN HERE: " + sortedFillKill[i] + " NEXT: " + sortedFillKill[i+1]);
                fillKillArray[i] = new FillKillObject(numFill, numKill);

            } else {
                //System.out.println("6666666");

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
            //System.out.println("VALUE " + valuePoint);

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
        xVals.add("" + 21);
        xVals.add("" + 22);
        xVals.add("" + 23);
        xVals.add("" + 24);
        xVals.add("" + 25);
        xVals.add("" + 26);
        xVals.add("" + 27);
        xVals.add("" + 28);
        xVals.add("" + 29);
        xVals.add("" + 30);
        xVals.add("" + 31);
        xVals.add("" + 32);
        xVals.add("" + 33);
        xVals.add("" + 34);
        xVals.add("" + 35);
        xVals.add("" + 36);
        xVals.add("" + 37);
        xVals.add("" + 38);
        xVals.add("" + 39);
        xVals.add("" + 40);
        xVals.add("" + 41);
        xVals.add("" + 42);
        xVals.add("" + 43);
        xVals.add("" + 44);
        xVals.add("" + 45);
        xVals.add("" + 46);
        xVals.add("" + 47);
        xVals.add("" + 48);
        xVals.add("" + 49);
        xVals.add("" + 50);
        xVals.add("" + 51);
        xVals.add("" + 52);
        xVals.add("" + 53);
        xVals.add("" + 54);
        xVals.add("" + 55);
        xVals.add("" + 56);
        xVals.add("" + 57);
        xVals.add("" + 58);
        xVals.add("" + 59);
        xVals.add("" + 60);
        xVals.add("" + 61);
        xVals.add("" + 62);
        xVals.add("" + 63);
        xVals.add("" + 64);
        xVals.add("" + 65);
        xVals.add("" + 66);
        xVals.add("" + 67);
        xVals.add("" + 68);
        xVals.add("" + 69);
        xVals.add("" + 70);
        xVals.add("" + 71);
        xVals.add("" + 72);
        xVals.add("" + 73);
        xVals.add("" + 74);
        xVals.add("" + 75);
        xVals.add("" + 76);
        xVals.add("" + 77);
        xVals.add("" + 78);
        xVals.add("" + 79);
        xVals.add("" + 80);
        xVals.add("" + 81);
        xVals.add("" + 82);
        xVals.add("" + 83);
        xVals.add("" + 84);
        xVals.add("" + 85);
        xVals.add("" + 86);
        xVals.add("" + 87);
        xVals.add("" + 88);
        xVals.add("" + 89);
        xVals.add("" + 90);
        xVals.add("" + 91);
        xVals.add("" + 92);
        xVals.add("" + 93);
        xVals.add("" + 94);
        xVals.add("" + 95);
        xVals.add("" + 96);
        xVals.add("" + 97);
        xVals.add("" + 98);
        xVals.add("" + 99);
        xVals.add("" + 100);

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

        mChart.getXAxis().setTextColor(Color.WHITE);
        mChart.getAxisLeft().setTextColor(Color.WHITE);
        mChart.getLegend().setTextColor(Color.WHITE);
        mChart.setDescriptionColor(Color.WHITE);

        mChart.setData(data);
        mChart.invalidate();
        progressDialog.dismiss();

    }
}