package com.example.sleepqualitylogin;

public class User {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String age;
    public String height;
    public String weight;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String firstName, String lastName, String email, String password, String age, String height, String weight) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.age = age;
        this.height = height;
        this.weight = weight;
    }
}

