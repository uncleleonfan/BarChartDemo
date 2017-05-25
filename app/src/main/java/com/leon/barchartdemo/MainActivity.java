package com.leon.barchartdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String[] HORIZONTAL_AXIS= {"一", "二", "三", "四",
            "五", "六", "七", "八", "九", "十", "十一", "十二"};

    private static final float[] DATA = {12, 24, 45, 56, 89, 70, 49, 22, 23, 10, 12, 3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BarChartView lineChart = (BarChartView) findViewById(R.id.bar_chart);
        lineChart.setHorizontalAxis(HORIZONTAL_AXIS);
        lineChart.setDataList(DATA, 89);
    }
}
