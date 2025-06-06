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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class timePickerWakeupFragment extends DialogFragment {

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

        // Set listener untuk tombol
        btnSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) {
                    Log.e("timePickerWakeupFragment", "Activity is null");
                    return; // Menghindari NullPointerException
                }

                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();
                int hour = timePicker.getHour(); // Pastikan ini sesuai dengan API yang Anda gunakan
                int minute = timePicker.getMinute(); // Pastikan ini sesuai dengan API yang Anda gunakan

                // Ambil email dari SharedPreferenc`

                // Menghitung timestamp dari tanggal dan waktu yang dipilih
                long wakeupTime = new java.util.GregorianCalendar(year, month, day, hour, minute).getTimeInMillis();

                long currentTime = System.currentTimeMillis();

                if (wakeupTime > currentTime) {
                    Toast.makeText(getActivity(), "Waktu Bangun Tidur tidak boleh di masa depan.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Query untuk memeriksa apakah sudah ada entri dengan email dan tanggal yang relevan
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", getActivity().MODE_PRIVATE);
                String uniqueKey = sharedPreferences.getString("uniqueKey", null);

                if (uniqueKey != null) {
                    // Update hanya field wakeUpTime dari entri berdasarkan document ID
                    databaseReference.child(uniqueKey).child("wakeUpTime").setValue(wakeupTime)
                            .addOnSuccessListener(aVoid -> {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("uniqueKey", null);
                                editor.apply();
                                Log.d("Firebase", "WakeUpTime updated successfully for ID: " + uniqueKey);
                                Toast.makeText(getActivity(), "Waktu bangun berhasil disimpan.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firebase", "Gagal menyimpan wakeUpTime: " + e.getMessage());
                                Toast.makeText(getActivity(), "Gagal menyimpan waktu bangun.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getActivity(), "ID entri tidak ditemukan.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return dialog;
    }
}
