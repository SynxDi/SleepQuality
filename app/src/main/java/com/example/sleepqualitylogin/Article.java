package com.example.sleepqualitylogin;

public class Article {
    public String author;
    public String title;
    public String content;

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Article() {
    }
    public Article(String author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
    }
}

