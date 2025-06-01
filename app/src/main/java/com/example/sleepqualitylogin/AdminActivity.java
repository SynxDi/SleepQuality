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
public class AdminActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private DatabaseReference mIndexRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);
        // Inisialisasi Firebase Database
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
            // Ambil kunci terakhir dan tingkatkan
            mIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    int lastIndex = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                    int newIndex = lastIndex + 1;
                    // Simpan artikel dengan kunci baru
                    String articleKey = "article" + newIndex;
                    mDatabase.child(articleKey).setValue(new Article(authorName, title, content))
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Update kunci terakhir di database
                                    mIndexRef.setValue(newIndex);
                                    Toast.makeText(AdminActivity.this, "Artikel berhasil disimpan", Toast.LENGTH_SHORT).show();
                                    // Kosongkan EditText setelah berhasil disimpan
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
}