package com.example.sleepqualitylogin;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private DatabaseReference mIndexRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("articleData");
        mIndexRef = FirebaseDatabase.getInstance("https://sleepanalysis-ac0b7-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("articleIndex");

        EditText author = findViewById(R.id.authorET);
        EditText judul = findViewById(R.id.judulET);
        EditText berita = findViewById(R.id.beritaET);
        Button submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String authorName = author.getText().toString().trim();
            String title = judul.getText().toString().trim();
            String content = berita.getText().toString().trim();

            if (authorName.isEmpty() || title.isEmpty() || content.isEmpty()) {
                Toast.makeText(AdminActivity.this, "Isi semua kolom", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current date
            String currentDate = getCurrentDate();

            // Get the last key and increment
            mIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    int lastIndex = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    int newIndex = lastIndex + 1;

                    // Save article with new key and include the date
                    String articleKey = "article" + newIndex;
                    mDatabase.child(articleKey).setValue(new Article(authorName, title, content, currentDate)) // Include date
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Update last key in the database
                                    mIndexRef.setValue(newIndex);
                                    Toast.makeText(AdminActivity.this, "Artikel berhasil disimpan", Toast.LENGTH_SHORT).show();
                                    // Clear EditText after successful save
                                    author.setText("");
                                    judul.setText("");
                                    berita.setText("");
                                } else {
                                    Toast.makeText(AdminActivity.this, "Gagal menyimpan artikel: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(AdminActivity.this, "Gagal mengambil kunci terakhir: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.forLanguageTag("id-ID"));
        return dateFormat.format(calendar.getTime());
    }

}
