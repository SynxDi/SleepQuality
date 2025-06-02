package com.example.sleepqualitylogin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private Context context;
    private List<Article> articleList;

    public ArticleAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articleList.get(position);
        holder.textTitle.setText(article.getTitle());
        holder.textAuthor.setText("Author: " + article.getAuthor());
        // Tidak menampilkan konten artikel di RecyclerView item

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetail.class);
            intent.putExtra(ArticleDetail.EXTRA_TITLE, article.getTitle());
            intent.putExtra(ArticleDetail.EXTRA_AUTHOR, article.getAuthor());
            intent.putExtra(ArticleDetail.EXTRA_CONTENT, article.getContent());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textAuthor;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            // textContent dihilangkan karena tidak dipakai
        }
    }
}
