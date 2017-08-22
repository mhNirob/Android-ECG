package ssaurel.com.testmp2;

import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private LineChart mChart;
    private LineChart[] myCharts;
    FakeDataSimple fakeData = new FakeDataSimple();

    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private boolean isAppRunning;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();

        Toast.makeText(this,ChooseDeviceActivity.device.getName(),Toast.LENGTH_LONG).show();

        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

        // create Line Chart
        //mChart = (LineChart) findViewById(R.id.myChart);
        for(int i=0;i<myCharts.length;i++) {
            // customize line chart
            myCharts[i].setDescription("");
            myCharts[i].setNoDataTextDescription("No data");

            // enable touch gestures
            myCharts[i].setTouchEnabled(true);

            // we want to also enable scaling an dragging
            myCharts[i].setDragEnabled(true);
            myCharts[i].setScaleEnabled(true);
            myCharts[i].setDrawGridBackground(false);

            // enable pinch zoom to avoid scaling x and y axis separately
            myCharts[i].setPinchZoom(true);

            // alternative background color
            myCharts[i].setBackgroundColor(Color.GRAY);

            // now we work on data
            LineData data = new LineData();
            data.setValueTextColor(Color.WHITE);

            // add data to line chart
            myCharts[i].setData(data);

            // get legend object
            Legend l = myCharts[i].getLegend();

            // customize legend
            l.setForm(Legend.LegendForm.LINE);
            l.setTextColor(Color.BLUE);

            XAxis xl = myCharts[i].getXAxis();
            xl.setTextColor(Color.BLACK);
            xl.setDrawGridLines(false);
            xl.setAvoidFirstLastClipping(true);

            YAxis yl = myCharts[i].getAxisLeft();
            yl.setTextColor(Color.BLACK);
            yl.setAxisMaxValue(1500f);
            yl.setAxisMinValue(0f);
            yl.setDrawGridLines(true);


            YAxis yl2 = myCharts[i].getAxisRight();
            yl2.setEnabled(false);
        }

    }

    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = ChooseDeviceActivity.device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    public class ReceiverThread extends Thread
    {
        public void run()
        {
            BTconnect();
            while(isAppRunning) {
                try {
                    int byteCount = inputStream.available();
                    if (byteCount > 0) {
                        byte[] rawBytes = new byte[byteCount];
                        inputStream.read(rawBytes);
                        final String string = new String(rawBytes, "UTF-8");
                        Log.i("nirob",string);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,string,Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }


    }


    private void initView() {
        myCharts = new LineChart[12];
        myCharts[0] = (LineChart) findViewById(R.id.myChart1);
        myCharts[1] = (LineChart) findViewById(R.id.myChart2);
        myCharts[2] = (LineChart) findViewById(R.id.myChart3);
        myCharts[3] = (LineChart) findViewById(R.id.myChart4);
        myCharts[4] = (LineChart) findViewById(R.id.myChart5);
        myCharts[5] = (LineChart) findViewById(R.id.myChart6);
        myCharts[6] = (LineChart) findViewById(R.id.myChart7);
        myCharts[7] = (LineChart) findViewById(R.id.myChart8);
        myCharts[8] = (LineChart) findViewById(R.id.myChart9);
        myCharts[9] = (LineChart) findViewById(R.id.myChart10);
        myCharts[10] = (LineChart) findViewById(R.id.myChart11);
        myCharts[11] = (LineChart) findViewById(R.id.myChart12);
        //myCharts[0] = (LineChart) findViewById(R.id.myChart1);



    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppRunning = true;
        new ReceiverThread().start();
        // now we're going to simulate real time data addition

        new Thread(new Runnable() {
            @Override
            public void run() {
                // add 100 entries
                float len = new FakeDataSimple().getLength();
                for (int i = 0; i < len; i++) {
                    final int c = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addEntry(c); // chart is notified of update in addEntry method
                        }
                    });

                    // pause between adds
                    if(i%12==11) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // manage error ...

                        }
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAppRunning = false;
    }
    // we need to create a method to add an entry to the line chart

    private void addEntry(int dataNum) {
            int chartNo = dataNum%12;
            LineData data = myCharts[chartNo].getData();

            if (data != null) {
                LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);

                if (set == null) {
                    // creation if null
                    set = createSet(chartNo);
                    data.addDataSet(set);
                }

                // add a new random value
                data.addXValue("");
//            data.addEntry(new Entry((float) (Math.random() * 110) + 5f, set
//                    .getEntryCount()), 0);

                float datum = fakeData.getData();
                data.addEntry(new Entry((float) datum, set.getEntryCount()), 0);

                // notify chart data have changed
                myCharts[chartNo].notifyDataSetChanged();

                // limit number of visible entries
                myCharts[chartNo].setVisibleXRangeMinimum(4);

                // but also ACTUALLY limit number of visible entries
                myCharts[chartNo].setVisibleXRangeMaximum(100);

                // scroll to the last entry
                myCharts[chartNo].moveViewToX(data.getXValCount() - 7);
            }

    }


    // method to create set
    private LineDataSet createSet(int channel) {
        String strChannel = Integer.toString(channel+1);
        LineDataSet set = new LineDataSet(null, strChannel);
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(4f);
        set.setCircleRadius(2f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 177));
        set.setDrawValues(false);
        //set.setValueTextColor(Color.BLACK);
        //set.setValueTextSize(10f);

        return set;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}