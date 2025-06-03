package com.example.sleepqualitylogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Load user data
        loadUserData(view);

        // Set up EditProfile button click listener
        AppCompatButton editProfileBt = view.findViewById(R.id.editProfile);
        editProfileBt.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            String userId = mAuth.getCurrentUser ().getUid(); // Get the current user's ID
            intent.putExtra("USER_ID", userId);
            startActivityForResult(intent, 1); // Start activity for result
        });

        // Set up Logout button click listener
        AppCompatButton buttonLogout = view.findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            // Clear SharedPreferences if needed
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Logout from Firebase
            if (mAuth != null) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                // Handle case if mAuth is null
                Toast.makeText(getActivity(), "Gagal logout, coba lagi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Refresh user data
            loadUserData(getView());
        }
    }

    // Method to load user data from SharedPreferences and update UI
    private void loadUserData(View view) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String firstName = sharedPreferences.getString("firstName", "First Name");
        String lastName = sharedPreferences.getString("lastName", "Last Name");
        String email = sharedPreferences.getString("user_email", "Email");
        String weight = sharedPreferences.getString("weight", "Weight");
        String height = sharedPreferences.getString("height", "Height");
        String age = sharedPreferences.getString("age", "Age");

        // Update TextViews with user data
        TextView tvFirstName = view.findViewById(R.id.tvFirstName);
        TextView tvLastName = view.findViewById(R.id.tvLastName);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        TextView tvFullName = view.findViewById(R.id.FullNameText);
        TextView tvWeight = view.findViewById(R.id.tvWeight);
        TextView tvHeight = view.findViewById(R.id.tvHeight);
        TextView tvAge = view.findViewById(R.id.tvAge);

        tvFirstName.setText(firstName);
        tvLastName.setText(lastName);
        tvEmail.setText(email);
        tvFullName.setText(firstName + " " + lastName);

        if (weight == null || weight.isEmpty()) {
            tvWeight.setText("-");
        } else {
            tvWeight.setText(weight + "kg");
        }

        if (height == null || height.isEmpty()) {
            tvHeight.setText("-");
        } else {
            tvHeight.setText(height + "cm");
        }

        if (age == null || age.isEmpty()) {
            tvAge.setText("-");
        } else {
            tvAge.setText(age);
        }
    }
}
