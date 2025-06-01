package com.example.sleepqualitylogin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AlarmFragment extends Fragment {

    private DatabaseReference databaseReference;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public AlarmFragment() {
        // Required empty public constructor
    }

    public static AlarmFragment newInstance(String param1, String param2) {
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alarm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button sleepButton = view.findViewById(R.id.sleepButton);
        Button wakeupButton = view.findViewById(R.id.wakeupButton);
        Button AnalysisButton = view.findViewById(R.id.anotherButton);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
        String email = sharedPreferences.getString("user_email", null);
        TextView helloText = view.findViewById(R.id.helloText);
        helloText.setText("Hello, " + email);

        TextView sleepAvgText = view.findViewById(R.id.sleepAvgText);
        TextView qualitySummaryText = view.findViewById(R.id.qualitySummaryText);
        FrameLayout qualitySummaryBackground = view.findViewById(R.id.qualitySummaryBackground);
        BarChart barChart = view.findViewById(R.id.barChart);
        databaseReference = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("tracker");

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> dayLabels = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayFormatter = new SimpleDateFormat("EEE", Locale.getDefault()); // e.g., Mon, Tue

        Date today = new Date();
        Date sevenDaysAgo = new Date(today.getTime() - (7L * 24 * 60 * 60 * 1000));

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<AnalysisSleepActivity.Tracker> trackerList = new ArrayList<>();

                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    AnalysisSleepActivity.Tracker tracker = childSnap.getValue(AnalysisSleepActivity.Tracker.class);
                    if (tracker != null && tracker.date != null && tracker.email != null && tracker.email.equals(email)) {
                        try {
                            Date trackerDate = sdf.parse(tracker.date);
                            if (trackerDate != null && !trackerDate.before(sevenDaysAgo) && !trackerDate.after(today)) {
                                trackerList.add(tracker);
                            }
                        } catch (Exception e) {
                            Log.e("AlarmFragment", "Date parsing error", e);
                        }
                    }
                }

                trackerList.sort((t1, t2) -> {
                    try {
                        return sdf.parse(t1.date).compareTo(sdf.parse(t2.date));
                    } catch (Exception e) {
                        return 0;
                    }
                });

                if (trackerList.size() > 7) {
                    trackerList = new ArrayList<>(trackerList.subList(trackerList.size() - 7, trackerList.size()));
                }

                int index = 0;
                for (AnalysisSleepActivity.Tracker tracker : trackerList) {
                    if (tracker.sleepTime != null && tracker.wakeUpTime != null) {
                        long durationMs = tracker.wakeUpTime - tracker.sleepTime;
                        if (durationMs > 0) {
                            float hours = durationMs / (1000f * 60f * 60f); // Convert to hours
                            entries.add(new BarEntry(index, hours));
                            try {
                                Date trackerDate = sdf.parse(tracker.date);
                                String label = dayFormatter.format(trackerDate);
                                dayLabels.add(label);
                            } catch (Exception e) {
                                dayLabels.add("?");
                            }
                            index++;
                        }
                    }
                }

                float totalHours = 0;
                for (BarEntry entry : entries) {
                    totalHours += entry.getY();
                }
                float avgHours = entries.size() > 0 ? totalHours / entries.size() : 0;

                if (avgHours >= 8) {
                    qualitySummaryText.setText("Great sleep");
                    qualitySummaryBackground.setBackgroundResource(R.drawable.alarm_gradient_a);

                } else if (avgHours >= 6) {
                    qualitySummaryText.setText("Decent sleep");
                    qualitySummaryBackground.setBackgroundResource(R.drawable.alarm_gradient_b);
                } else if (avgHours >= 4) {
                    qualitySummaryText.setText("Too little sleep");
                    qualitySummaryBackground.setBackgroundResource(R.drawable.alarm_gradient_c);
                } else {
                    qualitySummaryText.setText("Severely sleep-deprived");
                    qualitySummaryBackground.setBackgroundResource(R.drawable.alarm_gradient_d);
                }

                updateChart(barChart, entries, dayLabels);
                sleepAvgText.setText(String.format(Locale.getDefault(), "AVG %.0fH", avgHours));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AlarmFragment", "Database error", error.toException());
            }
        });

        sleepButton.setOnClickListener(v -> {
            String checkUniqueKey = sharedPreferences.getString("uniqueKey", null);
            if (checkUniqueKey != null) {
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("Confirm Action")
                        .setMessage("Anda sebelumnya telah memasukkan jam tidur. Apakah Anda ingin menghapus?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            databaseReference.child(checkUniqueKey).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Firebase", "Data berhasil dihapus.");
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.remove("uniqueKey");
                                        editor.apply();
                                    })
                                    .addOnFailureListener(databaseError -> {
                                        Log.e("Firebase", "Gagal menghapus data: " + databaseError.getMessage());
                                    });
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            } else {
                TimePickerFragment timePicker = new TimePickerFragment();
                timePicker.show(getChildFragmentManager(), "timePicker");
            }
        });

        wakeupButton.setOnClickListener(v -> {
            timePickerWakeupFragment timePickerWakeup = new timePickerWakeupFragment();
            timePickerWakeup.show(getChildFragmentManager(), "timePickerWakeup");
        });

        AnalysisButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AnalysisSleepActivity.class);
            startActivity(intent);
        });
    }

    private void updateChart(BarChart barChart, ArrayList<BarEntry> entries, ArrayList<String> labels) {
        BarDataSet dataSet = new BarDataSet(entries, "Durasi Tidur");
        dataSet.setColor(getResources().getColor(R.color.sleep_blue));

        BarData barData = new BarData(dataSet);
        barData.setDrawValues(false);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ((int) value) + "h";
            }
        });

        barChart.getAxisRight().setEnabled(false);
        barChart.invalidate();
    }
}