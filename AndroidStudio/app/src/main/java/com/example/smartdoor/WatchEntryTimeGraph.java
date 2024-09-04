package com.example.smartdoor;

import static com.android.volley.VolleyLog.TAG;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class WatchEntryTimeGraph extends AppCompatActivity {

    private ScatterChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_entry_time_graph);

        chart = findViewById(R.id.entryTimeGraph);

        // 기본설정
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setMaxHighlightDistance(50f);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setMaxVisibleValueCount(200);
        chart.setPinchZoom(true);

        // 라벨
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXOffset(5f);

        //y축 설정
        YAxis yl = chart.getAxisLeft();
        yl.setAxisMinimum(0);
        yl.setAxisMaximum(24);

        chart.getAxisRight().setEnabled(false);

        //축 설정
        XAxis xl = chart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setAxisMinimum(1);
        xl.setAxisMaximum(31);
        xl.setDrawGridLines(false);


        // 네트워크 요청 실행
        fetchDataAndPopulateChart();

        /*
        ArrayList<Entry> values1 = new ArrayList<>();

        // 그래프 값 넣기
        for (int i = 0; i < 11; i++) {
            values1.add(new Entry(i, (i)));
        }

        // create a data set and give it a type
        ScatterDataSet set1 = new ScatterDataSet(values1, "출입시간");

        // below line is use to set shape for our point on our graph.
        set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);

        // below line is for setting color to our shape.
        set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);

        // below line is use to set shape size
        // for our data set of the chart.
        set1.setScatterShapeSize(8f);

        // in below line we are creating a new array list for our data set.
        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();

        // in below line we are adding all
        // data sets to above array list.
        dataSets.add(set1); // add the data sets

        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);

        // below line is use to set data to our chart
        chart.setData(data);

        // at last we are calling
        // invalidate method on our chart.
        chart.invalidate();
        */
    }

    private void fetchDataAndPopulateChart() {
        // RequestQueue 초기화
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // URL 설정
        String url = "http://220.69.240.35/SmartDoor/watchEntryTimeGraph.php";

        // JsonArrayRequest 생성
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Entry> values1 = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject jsonObject = response.getJSONObject(i);
                                int day = jsonObject.getInt("day");
                                int hour = jsonObject.getInt("hour");
                                values1.add(new Entry(day, hour));
                            }

                            ScatterDataSet set1 = new ScatterDataSet(values1, "출입시간");
                            set1.setScatterShape(ScatterChart.ScatterShape.SQUARE);
                            set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
                            set1.setScatterShapeSize(8f);

                            ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set1);

                            ScatterData data = new ScatterData(dataSets);
                            chart.setData(data);
                            chart.invalidate();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "JSON Parsing Error!", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e(TAG, "Request Error!", error);
                    }
                }
        );

        // RequestQueue에 추가
        requestQueue.add(jsonArrayRequest);
    }
}
