package com.example.sleepqualitylogin;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

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

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi semua kolom", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Login berhasil
                            FirebaseUser  user = mAuth.getCurrentUser ();
                            SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_email", email);
                            editor.apply(); // atau .commit() jika ingin sinkron
                            startActivity(new Intent(LoginActivity.this, MainNavigation.class));
                            finish();
                        } else {
                            // Tangani kesalahan
                            String errorMessage = "Authentication failed.";
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Email atau password salah.";
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });


        });
    }
}
