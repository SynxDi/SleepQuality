package com.example.sleepqualitylogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AnalysisSleepActivity extends AppCompatActivity {

    private RecyclerView rvTrackerData;
    private TrackerAdapter adapter;
    private List<Tracker> trackerList = new ArrayList<>();
    private DatabaseReference trackerRef;
    private static final String TAG = "TrackerActivity";

    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_sleep);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("user_email", null);
        String age = sharedPreferences.getString("age", "Age");

        Log.d("ini email ditempat", email);
        Log.d("ini age ditempat", age);

        rvTrackerData = findViewById(R.id.rvTrackerData);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvTrackerData.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackerAdapter(trackerList);
        rvTrackerData.setAdapter(adapter);

        trackerRef = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("tracker");

        fetchLast7DaysDataWithEmailFilter(email);
    }

    private void fetchLast7DaysDataWithEmailFilter(final String emailFilter) {
        trackerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trackerList.clear();

                if (!snapshot.exists()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                }

                Date today = new Date();
                Date sevenDaysAgo = new Date(today.getTime() - (7L * 24 * 60 * 60 * 1000));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    Tracker tracker = childSnap.getValue(Tracker.class);

                    if (tracker != null && tracker.date != null && tracker.email != null) {
                        try {
                            Date trackerDate = sdf.parse(tracker.date);

                            if (trackerDate != null
                                    && !trackerDate.before(sevenDaysAgo)
                                    && !trackerDate.after(today)
                                    && tracker.email.trim().equalsIgnoreCase(emailFilter.trim())) {
                                trackerList.add(tracker);
                            }
                        } catch (ParseException e) {
                            Log.w(TAG, "Date parse error for entry: " + tracker.date, e);
                        }
                    }
                }

                trackerList.sort((t1, t2) -> {
                    try {
                        Date d1 = sdf.parse(t1.date);
                        Date d2 = sdf.parse(t2.date);
                        return d2.compareTo(d1);
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();

                if (trackerList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read tracker data", error.toException());
            }
        });
    }

    public static class Tracker {
        public String date;
        public String email;
        public Long sleepTime;
        public Long wakeUpTime;

        public Tracker() {
            // Default constructor for Firebase
        }
    }

    public class TrackerAdapter extends RecyclerView.Adapter<TrackerAdapter.TrackerViewHolder> {

        private List<Tracker> dataList;

        public TrackerAdapter(List<Tracker> dataList) {
            this.dataList = dataList;
        }

        @NonNull
        @Override
        public TrackerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_analysis_sleep_card, parent, false);
            return new TrackerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TrackerViewHolder holder, int position) {
            Tracker item = dataList.get(position);

            try {
                SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdfInput.parse(item.date);
                SimpleDateFormat sdfDay = new SimpleDateFormat("EEE", Locale.getDefault());
                holder.dayText.setText(sdfDay.format(date));
            } catch (Exception e) {
                holder.dayText.setText(item.date);
            }

            String start = item.sleepTime != null ? formatTime(item.sleepTime) : "??";
            String end = item.wakeUpTime != null ? formatTime(item.wakeUpTime) : "??";
            holder.rangeText.setText(start + " - " + end);

            if (item.sleepTime != null && item.wakeUpTime != null && item.wakeUpTime > item.sleepTime) {
                long durationMs = item.wakeUpTime - item.sleepTime;
                long totalMinutes = durationMs / (1000 * 60);
                long hours = totalMinutes / 60;
                long minutes = totalMinutes % 60;

                if (hours >= 8) {
                    holder.background.setBackgroundResource(R.drawable.alarm_gradient_a);
                } else if (hours >= 6) {
                    holder.background.setBackgroundResource(R.drawable.alarm_gradient_b);
                } else if (hours >= 4) {
                    holder.background.setBackgroundResource(R.drawable.alarm_gradient_c);
                } else {
                    holder.background.setBackgroundResource(R.drawable.alarm_gradient_d);
                }


                holder.durationHourText.setText(hours + "H");
                holder.durationMinuteText.setText(minutes + "M");
            } else {
                holder.durationHourText.setText("--");
                holder.durationMinuteText.setText("--");
            }
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        private String formatTime(Long ms) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(new Date(ms));
        }

        class TrackerViewHolder extends RecyclerView.ViewHolder {
            TextView dayText, rangeText, durationHourText, durationMinuteText;
            RelativeLayout background;

            public TrackerViewHolder(@NonNull View itemView) {
                super(itemView);
                dayText = itemView.findViewById(R.id.dayText);
                rangeText = itemView.findViewById(R.id.rangeText);
                durationHourText = itemView.findViewById(R.id.durationHourText);
                durationMinuteText = itemView.findViewById(R.id.durationMinuteText);
                background = itemView.findViewById(R.id.background);
            }
        }
    }
}