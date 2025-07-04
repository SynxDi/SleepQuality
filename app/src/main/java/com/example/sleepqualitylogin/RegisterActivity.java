package com.example.sleepqualitylogin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView = findViewById(R.id.textLogin);
        String fullText = "Already Have Account? Login here";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Login here");
        int endIndex = startIndex + "Login here".length();
        spannable.setSpan(
                new ForegroundColorSpan(Color.RED),
                startIndex,
                endIndex,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        textView.setTextColor(Color.WHITE);
        textView.setText(spannable);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        Button registerButton = findViewById(R.id.registerButton);
        EditText emailInput = findViewById(R.id.emailEditText);
        EditText passwordInput = findViewById(R.id.passwordEditText);
        EditText firstNameInput = findViewById(R.id.firstNameET);
        EditText lastNameInput = findViewById(R.id.lastNameET);
        EditText confirmPasswordInput = findViewById(R.id.confirmPasswordET);
        String age = "";
        String height = "";
        String weight = "";

        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
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

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Password field does not match the confirm password field", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                       .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(verificationTask -> {
                        if (verificationTask.isSuccessful()) {
                            Toast.makeText(this, "A verification email has been sent to " + email, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Failed to send verification email: " + verificationTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                    // Dapatkan UID pengguna yang baru dibuat
                    String userId = mAuth.getCurrentUser ().getUid();
                    // Buat objek User
                    User user = new User(firstName, lastName, email, password, age, height, weight);
                    // Simpan data pengguna ke Realtime Database
                    mDatabase.child(userId).setValue(user)
                            .addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(this, "Register successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Failed to save data: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Failed to register: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}