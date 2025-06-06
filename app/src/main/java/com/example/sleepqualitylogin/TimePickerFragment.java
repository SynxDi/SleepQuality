package com.example.sleepqualitylogin;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TimePickerFragment extends DialogFragment {

    private DatabaseReference databaseReference;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Membuat dialog
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Menghilangkan judul dialog
        dialog.setContentView(R.layout.fragment_time_picker);

        // Mengatur ukuran dialog
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Atur lebar dialog
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // Atur tinggi dialog
        dialog.getWindow().setAttributes(layoutParams);

        // Mengambil referensi ke DatePicker, TimePicker, dan Button
        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        Button btnSetTime = dialog.findViewById(R.id.btnSetTime);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("tracker");

        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                // Ambil email dari SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                String email = sharedPreferences.getString("user_email", null);

                // Menghitung timestamp dari tanggal dan waktu yang dipilih
                long sleepTime = new java.util.GregorianCalendar(year, month, day, hour, minute).getTimeInMillis();
                long wakeupTime = 0;
                long currentTime = System.currentTimeMillis();

                if (sleepTime > currentTime) {
                    Toast.makeText(getActivity(), "Waktu tidur tidak boleh di masa depan.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Membuat format tanggal untuk validasi
                String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day); // Format yyyy-MM-dd

                // Query untuk memeriksa apakah sudah ada entri dengan tanggal dan email yang sama
                databaseReference.orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Jika tidak ada entri, simpan data baru
                        if (email != null) {
                            DatabaseReference newChildRef = databaseReference.push();
                            String uniqueKey = newChildRef.getKey();
                            Log.d("Firebase", "Generated unique key: " + uniqueKey);

                            // Simpan uniqueKey ke SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("uniqueKey", uniqueKey);
                            editor.apply();

                            DateTimeEntry entry = new DateTimeEntry(sleepTime, selectedDate, email); // Menyimpan sleepTime dan tanggal
                            newChildRef.setValue(entry)
                                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data saved successfully with key: " + uniqueKey))
                                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to save data: " + e.getMessage()));
                        } else {
                            Log.e("Firebase", "Email is null, cannot save data.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Firebase", "Database error: " + databaseError.getMessage());
                    }
                });

                dialog.dismiss(); // Tutup dialog
            }
        });


        return dialog;
    }

    // Class untuk merepresentasikan entri tanggal dan waktu
    public static class DateTimeEntry {
        public long sleepTime; // Menyimpan timestamp
        public String date; // Menyimpan tanggal dalam format yyyy-MM-dd
        public String email;

        public DateTimeEntry(long sleepTime, String date, String email) {
            this.sleepTime = sleepTime;
            this.date = date;
            this.email = email;
        }
    }

}
