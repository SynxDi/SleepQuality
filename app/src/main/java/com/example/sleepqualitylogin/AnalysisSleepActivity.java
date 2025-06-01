package com.example.sleepqualitylogin;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private String filterEmail = "paler@gmail.com";
    private TextView tvEmpty;
    private TextView tvAverageDuration; // Tambahan

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_sleep);

        rvTrackerData = findViewById(R.id.rvTrackerData);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvAverageDuration = findViewById(R.id.tvAverageDuration); // Tambahan

        rvTrackerData.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrackerAdapter(trackerList);
        rvTrackerData.setAdapter(adapter);

        trackerRef = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("tracker");

        Log.d(TAG, "onCreate: Fetching data for email = " + filterEmail);
        fetchLast7DaysDataWithEmailFilter(filterEmail);
    }

    private void fetchLast7DaysDataWithEmailFilter(final String emailFilter) {
        trackerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Called");
                trackerList.clear();

                if (!snapshot.exists()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvAverageDuration.setVisibility(View.GONE);
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

                // Sort descending by date
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
                    tvAverageDuration.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    tvAverageDuration.setVisibility(View.VISIBLE);

                    // Hitung rata-rata durasi tidur
                    long totalSleepDuration = 0;
                    int validCount = 0;

                    for (Tracker tracker : trackerList) {
                        if (tracker.sleepTime != null && tracker.wakeUpTime != null) {
                            long duration = tracker.wakeUpTime - tracker.sleepTime;
                            if (duration > 0) {
                                totalSleepDuration += duration;
                                validCount++;
                            }
                        }
                    }

                    if (validCount > 0) {
                        long avgDurationMs = totalSleepDuration / validCount;
                        long totalMinutes = avgDurationMs / (1000 * 60);
                        long hours = totalMinutes / 60;
                        long minutes = totalMinutes % 60;

                        String avgText = "Rata-rata Durasi Tidur: " + hours + " jam " + minutes + " menit\n";

                        if (hours >= 8) {
                            avgText += "Pesan: A (Durasi tidur sangat baik)";
                        } else if (hours >= 6) {
                            avgText += "Pesan: B (Durasi tidur cukup)";
                        } else if (hours >= 4) {
                            avgText += "Pesan: C (Durasi tidur kurang)";
                        } else {
                            avgText += "Pesan: D (Durasi tidur sangat kurang)";
                        }

                        tvAverageDuration.setText(avgText);
                    } else {
                        tvAverageDuration.setText("Tidak ada data tidur yang valid.");
                    }
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
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new TrackerViewHolder(view);
        }

        private String calculateSleepDuration(Long sleepTime, Long wakeUpTime) {
            long durationMs = wakeUpTime - sleepTime;
            if (durationMs <= 0) return "Invalid";

            long totalMinutes = durationMs / (1000 * 60);
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;

            return hours + " jam " + minutes + " menit";
        }

        @Override
        public void onBindViewHolder(@NonNull TrackerViewHolder holder, int position) {
            Tracker item = dataList.get(position);
            holder.tv1.setText(item.email);

            StringBuilder sb = new StringBuilder();
            sb.append("Tanggal: ").append(item.date);

            if (item.sleepTime != null) {
                sb.append("\nTidur: ").append(formatTimestamp(item.sleepTime));
            }

            if (item.wakeUpTime != null) {
                sb.append("\nBangun: ").append(formatTimestamp(item.wakeUpTime));
            }

            if (item.sleepTime != null && item.wakeUpTime != null) {
                sb.append("\nDurasi Tidur: ").append(calculateSleepDuration(item.sleepTime, item.wakeUpTime));
            }

            holder.tv2.setText(sb.toString());
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        class TrackerViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;

            public TrackerViewHolder(@NonNull View itemView) {
                super(itemView);
                tv1 = itemView.findViewById(android.R.id.text1);
                tv2 = itemView.findViewById(android.R.id.text2);
            }
        }

        private String formatTimestamp(Long ms) {
            if (ms == null) return "";
            Date date = new Date(ms);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date);
        }
    }
}
