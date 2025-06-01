package com.example.sleepqualitylogin;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ArticleDetail extends AppCompatActivity {

    public static final String EXTRA_AUTHOR = "extra_author";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_CONTENT = "extra_content";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        TextView textTitle = findViewById(R.id.textTitleDetail);
        TextView textAuthor = findViewById(R.id.textAuthorDetail);
        TextView textContent = findViewById(R.id.textContentDetail);

        // Terima data dari Intent
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String author = getIntent().getStringExtra(EXTRA_AUTHOR);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);

        textTitle.setText(title);
        textAuthor.setText("Author: " + author);
        textContent.setText(content);
    }
}
