package com.leon.barchartdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private static final String[] HORIZONTAL_AXIS= {"一月", "二月", "三月", "四月",
            "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};

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
