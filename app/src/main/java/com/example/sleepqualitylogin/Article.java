package com.example.sleepqualitylogin;

public class Article {
    public String author;  // Existing variable
    public String title;   // Existing variable
    public String content; // Existing variable
    public String date;    // New variable for date

    // Default constructor
    public Article() {
    }

    // Constructor with parameters
    public Article(String author, String title, String content, String date) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.date = date; // Initialize the date field
    }

    // Getters
    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date; // Getter for the date
    }

    // Setters (optional)
    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDate(String date) {
        this.date = date; // Setter for the date
    }
}
