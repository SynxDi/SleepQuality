package com.example.sleepqualitylogin;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // untuk Warna Teks Sign Up Merah Putih
        TextView textView = findViewById(R.id.textsignup);
        String fullText = "Don't have an account? Sign up";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Sign up");
        int endIndex = startIndex + "Sign up".length();
        spannable.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setTextColor(Color.WHITE);
        textView.setText(spannable);
        // END ( Merah Putih )

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        Button loginButton = findViewById(R.id.loginButton);
        EditText emailInput = findViewById(R.id.emailEditText);
        EditText passwordInput = findViewById(R.id.passwordEditText);

        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();


            if (email.equals("admin") && password.equals("admin")) {
                Intent admin = new Intent(LoginActivity.this, AdminActivity.class);
                startActivity(admin);
                return;
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill in all columns", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                Toast.makeText(this, "Format email not valid", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
                Toast.makeText(this, "Password must contain at least one uppercase letter and one number", Toast.LENGTH_SHORT).show();
                return;
            }


            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Login berhasil
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null && user.isEmailVerified()) {
                                // Simpan email ke SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("user_email", email);
                                editor.apply();

                                // Ambil status Dark Mode dari SharedPreferences
                                String userId = user.getUid();
                                boolean isDarkMode = sharedPreferences.getBoolean("DARK_MODE_" + userId, false);
                                setAppTheme(isDarkMode);

                                // Ambil data pengguna
                                fetchUserData(user.getEmail());

                                // Navigasi ke MainNavigation
                                startActivity(new Intent(LoginActivity.this, MainNavigation.class));
                                finish();

                            } else {
                                // Email belum diverifikasi
                                Toast.makeText(LoginActivity.this, "Please verify your email first.", Toast.LENGTH_LONG).show();
                                mAuth.signOut(); // Logout agar tidak tetap login meskipun belum verifikasi
                            }

                        } else {
                            // Tangani kesalahan login
                            String errorMessage = "Login failed.";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Incorrect email or password.";
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }

    // Method untuk mengatur tema
    private void setAppTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void fetchUserData(String email) {
        db.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);
                        String emailName = snapshot.child("email").getValue(String.class);
                        String WeightText = snapshot.child("weight").getValue(String.class);
                        String HeightText = snapshot.child("height").getValue(String.class);
                        String AgeText = snapshot.child("age").getValue(String.class);
                        // Save to SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("firstName", firstName);
                        editor.putString("lastName", lastName);
                        editor.putString("user_email", emailName);
                        editor.putString("weight", WeightText);
                        editor.putString("height", HeightText);
                        editor.putString("age", AgeText);
                        editor.apply();
                    }
                    // Start MainNavigation Activity
                    startActivity(new Intent(LoginActivity.this, MainNavigation.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "User  not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadUser Data:onCancelled", databaseError.toException());
            }
        });
    }
}
