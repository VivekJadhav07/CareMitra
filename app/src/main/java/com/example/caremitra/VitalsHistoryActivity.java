package com.example.caremitra;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class VitalsHistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout vitalsTabs;
    private MaterialButtonToggleGroup dateRangeToggle;
    private LineChart vitalsChart;
    private TextView avgValue, maxValue, minValue;
    private String currentVital = "Heart Rate";
    private String currentDateRange = "Week";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitals_history);

        toolbar = findViewById(R.id.toolbar_vitals);
        vitalsTabs = findViewById(R.id.vitals_tabs);
        dateRangeToggle = findViewById(R.id.date_range_toggle);
        vitalsChart = findViewById(R.id.vitals_chart);
        avgValue = findViewById(R.id.avg_value);
        maxValue = findViewById(R.id.max_value);
        minValue = findViewById(R.id.min_value);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vitals History");
        }

        setupTabs();
        setupDateRangeToggle();
        setupChart();
        loadChartData();
    }

    private void setupTabs() {
        vitalsTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentVital = tab.getText().toString();
                setupChart();
                loadChartData();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupDateRangeToggle() {
        dateRangeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_day) {
                    currentDateRange = "Day";
                } else if (checkedId == R.id.btn_week) {
                    currentDateRange = "Week";
                } else if (checkedId == R.id.btn_month) {
                    currentDateRange = "Month";
                }
                setupChart();
                loadChartData();
            }
        });
    }

    private void setupChart() {
        vitalsChart.getDescription().setEnabled(false);
        vitalsChart.setTouchEnabled(true);
        vitalsChart.setDragEnabled(true);
        vitalsChart.setScaleEnabled(true);
        vitalsChart.setPinchZoom(true);
        vitalsChart.setDrawGridBackground(false);
        vitalsChart.getLegend().setEnabled(false);

        XAxis xAxis = vitalsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if ("Month".equals(currentDateRange)) {
                    return "Wk " + (int) value;
                } else if ("Day".equals(currentDateRange)) {
                    return "Hr " + (int) value; // Example for hourly data
                }
                return "Day " + (int) value;
            }
        });

        YAxis leftAxis = vitalsChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(Color.GRAY);

        if ("Heart Rate".equals(currentVital)) {
            leftAxis.setAxisMinimum(50f);
            leftAxis.setAxisMaximum(130f);
        } else if ("Oxygen".equals(currentVital)) {
            leftAxis.setAxisMinimum(90f);
            leftAxis.setAxisMaximum(101f);
        } else if ("Temperature".equals(currentVital)) {
            leftAxis.setAxisMinimum(95f);
            leftAxis.setAxisMaximum(105f);
        }

        vitalsChart.getAxisRight().setEnabled(false);
    }

    private void loadChartData() {
        List<Entry> entries = new ArrayList<>();
        Random random = new Random();

        int dataPoints = 7; // Default: Week
        if ("Day".equals(currentDateRange)) {
            dataPoints = 24; // 24 hours in a day
        } else if ("Month".equals(currentDateRange)) {
            dataPoints = 4; // 4 weeks in a month
        }

        for (int i = 1; i <= dataPoints; i++) {
            float value = 0;
            if ("Heart Rate".equals(currentVital)) {
                value = 65 + random.nextFloat() * 25; // 65-90
            } else if ("Oxygen".equals(currentVital)) {
                value = 95 + random.nextFloat() * 4;  // 95-99
            } else if ("Temperature".equals(currentVital)) {
                value = 97.5f + random.nextFloat() * 2; // 97.5-99.5
            }
            entries.add(new Entry(i, value));
        }

        if (entries.isEmpty()) {
            vitalsChart.clear();
            vitalsChart.invalidate();
            return;
        }

        float sum = 0f;
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (Entry entry : entries) {
            float value = entry.getY();
            sum += value;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        float avg = sum / entries.size();

        avgValue.setText(String.format(Locale.getDefault(), "%.1f", avg));
        maxValue.setText(String.format(Locale.getDefault(), "%.1f", max));
        minValue.setText(String.format(Locale.getDefault(), "%.1f", min));

        LineDataSet dataSet = new LineDataSet(entries, currentVital);

        int lineColor = ContextCompat.getColor(this, R.color.colorPrimary);
        Drawable gradientDrawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient);

        if ("Oxygen".equals(currentVital)) {
            lineColor = Color.parseColor("#4CAF50");
            gradientDrawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient_green);
        } else if ("Temperature".equals(currentVital)) {
            lineColor = Color.parseColor("#FF9800");
            gradientDrawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient_orange);
        }

        dataSet.setColor(lineColor);
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillDrawable(gradientDrawable);

        LineData lineData = new LineData(dataSet);
        vitalsChart.setData(lineData);
        vitalsChart.animateX(1000);
        vitalsChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}