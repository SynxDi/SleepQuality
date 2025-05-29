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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        databaseReference = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        // Set listener untuk tombol
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                int hour = timePicker.getHour(); // Menggunakan getHour() untuk API 23+
                int minute = timePicker.getMinute(); // Menggunakan getMinute() untuk API 23+

                // Ambil email dari SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                String email = sharedPreferences.getString("user_email", null);

                // Lakukan sesuatu dengan tanggal dan waktu yang dipilih
                Log.d("DateTimePicker", "Selected date and time: " + day + "/" + (month + 1) + "/" + year + " " + hour + ":" + minute);

                // Simpan data ke Firebase Realtime Database
                Log.d("email testing", email);
                if (email != null) {
                    // Mengganti karakter '.' pada email agar bisa dipakai sebagai key
                    String safeEmailKey = email.replace(".", ",");

                    // Membuat entry baru
                    DateTimeEntry entry = new DateTimeEntry(day, month + 1, year, hour, minute, email);
                    Log.d("Firebase", "Attempting to save data with key: " + safeEmailKey);
                    databaseReference.child(safeEmailKey).setValue(entry)
                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data saved successfully"))
                            .addOnFailureListener(e -> Log.e("Firebase", "Failed to save data: " + e.getMessage()));
                } else {
                    Log.e("Firebase", "Email is null, cannot save data.");
                }

                dialog.dismiss(); // Tutup dialog
            }
        });

        return dialog;
    }

    // Class untuk merepresentasikan entri tanggal dan waktu
    public static class DateTimeEntry {
        public int day;
        public int month;
        public int year;
        public int hour;
        public int minute;
        public String email;

        public DateTimeEntry(int day, int month, int year, int hour, int minute, String email) {
            this.day = day;
            this.month = month;
            this.year = year;
            this.hour = hour;
            this.minute = minute;
            this.email = email;
        }
    }
}
