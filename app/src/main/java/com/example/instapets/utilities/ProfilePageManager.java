package com.example.instapets.utilities;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instapets.R;
import com.example.instapets.adapters.MyPictureAdapter;
import com.example.instapets.adapters.MyTextAdapter;
import com.example.instapets.models.Post;
import com.example.instapets.models.User;
import com.google.firebase.firestore.DocumentReference;

public class ProfilePageManager {
    public TextView noOfFollowers;
    public ImageView backgroundImage;
    MyPictureAdapter myPictureAdapter;
    MyTextAdapter myTextAdapter;
    ImageView profileImage;
    TextView name, username, bio, noOfPosts, noOfFollowing;
    ImageView picturesButton, textButton;
    RecyclerView recyclerViewMyPosts;
    GridLayoutManager layoutManager;
    View picturesIndicator, textIndicator;
    Context context;
    View view;

    public ProfilePageManager(View view) {
        this.view = view;
        view.setVisibility(View.INVISIBLE);

        backgroundImage = view.findViewById(R.id.img_background);
        profileImage = view.findViewById(R.id.img_profile);
        name = view.findViewById(R.id.txt_name);
        username = view.findViewById(R.id.txt_username);
        bio = view.findViewById(R.id.txt_bio);
        noOfPosts = view.findViewById(R.id.txt_post_count);
        noOfFollowers = view.findViewById(R.id.txt_followers_count);
        noOfFollowing = view.findViewById(R.id.txt_following_count);
        picturesButton = view.findViewById(R.id.btn_my_pictures);
        textButton = view.findViewById(R.id.btn_my_kitts);
        recyclerViewMyPosts = view.findViewById(R.id.recyclerview_my_posts);
        picturesIndicator = view.findViewById(R.id.indicator_pictures);
        textIndicator = view.findViewById(R.id.indicator_kitts);

        context = view.getContext();
        handlePostsArea();
    }

    //This private method is responsible for setting up the posts area of the user's profile.
    //It initializes MyPictureAdapter and MyTextAdapter for managing user posts,
    //configures the GridLayoutManager for the recyclerViewMyPosts,
    //and sets click listeners for the "My Pictures" and "My Kitts" buttons
    //to switch between different types of posts and update UI elements accordingly.
    private void handlePostsArea() {

        myPictureAdapter = new MyPictureAdapter(context);
        myTextAdapter = new MyTextAdapter(context);
        layoutManager = new GridLayoutManager(context, 3);

        recyclerViewMyPosts.setHasFixedSize(true);
        recyclerViewMyPosts.setAdapter(myPictureAdapter);
        recyclerViewMyPosts.setLayoutManager(layoutManager);

        picturesButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myPictureAdapter);
            layoutManager.setSpanCount(3); //number of posts in the grid
            picturesIndicator.setVisibility(View.VISIBLE);
            textIndicator.setVisibility(View.INVISIBLE);
        });
        textButton.setOnClickListener(v -> {
            recyclerViewMyPosts.setAdapter(myTextAdapter);
            layoutManager.setSpanCount(1);
            picturesIndicator.setVisibility(View.INVISIBLE);
            textIndicator.setVisibility(View.VISIBLE);
        });
    }

    //This method reads and populates the user's posts from a DocumentReference (userReference).
    //It retrieves user and post data from Firebase, and for each post,
    //it adds it to either myPictureAdapter or myTextAdapter based on the content type (image or text).
    public void readPosts(DocumentReference userReference) {
        userReference.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            assert user != null;
            myPictureAdapter.clearPosts();
            myTextAdapter.clearPosts();
            if(user.getPosts()!=null){
                for (DocumentReference postReference : user.getPosts()) {
                    postReference.get().addOnSuccessListener(postSnapshot -> {
                        Post post = postSnapshot.toObject(Post.class);
                        assert post != null;
                        if (!post.getImageUrl().isEmpty()) {
                            myPictureAdapter.addPost(postReference);
                        }
                        if (!post.getKitt().isEmpty()) {
                            myTextAdapter.addPost(postReference);
                        }
                    });
                }

            }
        });
    }

    //This method populates the user's profile data based on the provided User object.
    //It also sets the visibility of the view to View.VISIBLE.
    public void fillUserData(User user) {
        String txt_username = "@" + user.getUsername();

        username.setText(txt_username);
        name.setText(user.getName());
        bio.setText(user.getBio());
        if (user.getPosts() != null) {
            noOfPosts.setText(String.valueOf(user.getPosts().size()));

        }
        noOfFollowers.setText(String.valueOf(user.getFollowers().size()));
        noOfFollowing.setText(String.valueOf(user.getFollowing().size()));
        Glide.with(context).load(user.getProfileImageUrl()).into(profileImage);
        Glide.with(context).load(user.getBackgroundImageUrl()).into(backgroundImage);

        view.setVisibility(View.VISIBLE);
    }
}
