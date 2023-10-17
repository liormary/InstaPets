package com.example.instapets.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

/**
 * this class represent a user in the application
 */

public class User {
    String id;
    String name;
    String username;
    String email;
    String bio; //user's bio in his profile page
    String profileImageUrl;
    String backgroundImageUrl;
    String playerDeviceId; //players device
    List<DocumentReference> posts; //All the posts that the user has upload
    List<DocumentReference> saved; //All the saved posts that the user found interested
    List<DocumentReference> following; //All the users that our user follows after
    List<DocumentReference> followers; //All the users that follow after our user

    //Empty constructor
    public User() {
    }

    public User(String id, String name, String username, String email, String profileImageUrl, String backgroundImageUrl) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.playerDeviceId = "";
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.bio = "";
        this.posts = new ArrayList<>();
        this.saved = new ArrayList<>();
        this.following = new ArrayList<>();
        this.followers = new ArrayList<>();
    }

    public String getPlayerDeviceId() {
        return playerDeviceId;
    }

    public void setPlayerDeviceId(String playerDeviceId) {
        this.playerDeviceId = playerDeviceId;
    }

    public List<DocumentReference> getFollowing() {
        return following;
    }

    public List<DocumentReference> getFollowers() {
        return followers;
    }

    public List<DocumentReference> getSaved() {
        return saved;
    }

    public List<DocumentReference> getPosts() {
        return posts;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getBio() {
        return bio;
    }
    public String getEmail() {
        return email;
    }
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }
}