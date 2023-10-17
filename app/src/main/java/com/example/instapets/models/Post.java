package com.example.instapets.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

/**
 * this class represent a post in the app
 * that made by a user
 */

public class Post {
    String creator, postid, imageUrl, caption, kitt;//kitt - the Text for the Text Post
    List<DocumentReference> likes;//the users that done like to the post that saved by FireBase

    public Post(String creator, String postid, String imageUrl, String caption) {
        this.creator = creator;
        this.postid = postid;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.likes = new ArrayList<>();
        this.kitt = "";
    }

    public String getKitt() {
        return kitt;
    }

    public Post() {
    }

    public void setKitt(String kitt) {
        this.kitt = kitt;
    }

    public List<DocumentReference> getLikes() {
        return likes;
    }

    public void setLikes(List<DocumentReference> likes) {
        this.likes = likes;
    }


    public String getPostid() {
        return postid;
    }

    public String getCreator() {
        return creator;
    }


    public String getImageUrl() {
        return imageUrl;
    }


    public String getCaption() {
        return caption;
    }

}