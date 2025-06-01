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
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
        databaseReference = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("tracker");

        sleepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            .setNegativeButton("No", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .show();
                } else {
                    // Only show TimePicker if no uniqueKey is present
                    TimePickerFragment timePicker = new TimePickerFragment();
                    timePicker.show(getChildFragmentManager(), "timePicker");
                }
            }
        });

        wakeupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerWakeupFragment timePickerWakeup = new timePickerWakeupFragment();
                timePickerWakeup.show(getChildFragmentManager(), "timePickerWakeup");
            }
        });

        AnalysisButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AnalysisSleepActivity.class);
                startActivity(intent);
            }
        });
    }
}
