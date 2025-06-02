package com.example.sleepqualitylogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etWeight, etHeight, etAge;
    private Button btnSave;

    private TextView tvEmail;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        // Get the user ID from the Intent
        String userId = getIntent().getStringExtra("USER_ID");

        // Initialize UI elements
        etFirstName = findViewById(R.id.etFirstName);
        tvEmail = findViewById(R.id.tvEmail);
        etLastName = findViewById(R.id.etLastName);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etAge = findViewById(R.id.etAge);
        btnSave = findViewById(R.id.btnSave);

        // Load user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        etFirstName.setText(sharedPreferences.getString("firstName", ""));
        tvEmail.setText(sharedPreferences.getString("user_email","Email"));
        etLastName.setText(sharedPreferences.getString("lastName", ""));
        etWeight.setText(sharedPreferences.getString("weight", ""));
        etHeight.setText(sharedPreferences.getString("height", ""));
        etAge.setText(sharedPreferences.getString("age", ""));

        btnSave.setOnClickListener(v -> {
            // Get updated values
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String weight = etWeight.getText().toString().trim();
            String height = etHeight.getText().toString().trim();
            String age = etAge.getText().toString().trim();

            // Validate input
            if (firstName.isEmpty() || lastName.isEmpty() || weight.isEmpty() || height.isEmpty() || age.isEmpty()) {
                Toast.makeText(EditProfileActivity.this, "Isi semua kolom", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save updated data to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("firstName", firstName);
            editor.putString("lastName", lastName);
            editor.putString("weight", weight);
            editor.putString("height", height);
            editor.putString("age", age);
            editor.apply();

            // Update data in Firebase Realtime Database using the user ID
            mDatabase.child(userId).child("firstName").setValue(firstName);
            mDatabase.child(userId).child("lastName").setValue(lastName);
            mDatabase.child(userId).child("weight").setValue(weight);
            mDatabase.child(userId).child("height").setValue(height);
            mDatabase.child(userId).child("age").setValue(age).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK); // Set result to OK
                    finish(); // Close the activity and return to ProfileFragment
                } else {
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

    }

}
