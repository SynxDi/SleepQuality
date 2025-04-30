package com.example.sleepqualitylogin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login);

        // Mengatur insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Mewarnai teks "Sign up" merah dan sisanya putih
        TextView textView = findViewById(R.id.textsignup);
        String fullText = "Don't have an account? Sign up";
        SpannableString spannable = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Sign up");
        int endIndex = startIndex + "Sign up".length();
        // Warna merah untuk "Sign up"
        spannable.setSpan(
                new ForegroundColorSpan(Color.RED),
                startIndex,
                endIndex,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // Atur warna default putih (opsional)
        textView.setTextColor(Color.WHITE);
        textView.setText(spannable);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}