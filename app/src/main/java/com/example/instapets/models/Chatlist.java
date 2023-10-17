package com.example.instapets.models;

/**
 * this class represents a user's chat list
 * in the app(most of his use is in the FB)
 */

public class Chatlist {
    public String id;

    public Chatlist(String id) {
        this.id = id;
    }

    public Chatlist() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}