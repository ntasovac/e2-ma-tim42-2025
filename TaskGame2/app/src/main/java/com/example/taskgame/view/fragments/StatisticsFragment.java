package com.example.taskgame.view.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.taskgame.R;
import com.example.taskgame.databinding.FragmentStatisticsBinding;
import com.example.taskgame.domain.models.SessionManager;
import com.example.taskgame.view.viewmodels.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;
    private StatisticsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        viewModel.getStreakCount().observe(getViewLifecycleOwner(), streak ->
                binding.tvStreak.setText(getString(R.string.streak, streak))
        );

        viewModel.updateStreak();

        String userId = SessionManager.getInstance().getUserId();
        viewModel.loadTaskStatistics(userId);
        setupPieChart(binding.pieChart);

        viewModel.getTaskStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats == null) return;
            updatePieChart(binding.pieChart, stats);
            binding.tvSummary.setText(
                    "Total: " + stats.total +
                            "\nDone: " + stats.done +
                            "\nActive: " + stats.active +
                            "\nCancelled: " + stats.cancelled
            );
        });
        viewModel.getTaskStreakCount().observe(getViewLifecycleOwner(), streak ->
                binding.tvTaskStreak.setText(getString(R.string.taskStreak, streak)));
        viewModel.updateTaskStreak(userId);

        setupBarChart(binding.barChart);

        viewModel.getTaskCategoryStats().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                updateBarChart(binding.barChart, categories);
            }
        });

        viewModel.updateTaskCategoryStats(userId);
    }

    private void setupPieChart(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(70f);
        chart.setTransparentCircleRadius(75f);
        chart.setDrawEntryLabels(false);
        chart.setCenterText("Tasks");
        chart.setCenterTextSize(16f);
        chart.setCenterTextColor(Color.DKGRAY);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    private void updatePieChart(PieChart chart, StatisticsViewModel.TaskStats stats) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(stats.done, "Done"));
        entries.add(new PieEntry(stats.active, "Active"));
        entries.add(new PieEntry(stats.cancelled, "Cancelled"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#F44336")
        );

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.invalidate();
    }
    private void setupBarChart(BarChart barChart) {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(true);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraOffsets(10f, 10f, 10f, 50f);
    }

    private void updateBarChart(BarChart barChart, Map<String, Integer> data) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        int index = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            categories.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "DONE by Category");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(14f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(categories));
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setTextSize(12f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}