package com.example.instapets.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.instapets.R;
import com.example.instapets.adapters.PostAdapter;
import com.example.instapets.models.Post;
import com.example.instapets.models.User;

/**
 * this class represents the activity of saved posts
 */
public class SavedPostsActivity extends AppCompatActivity {
    RecyclerView savedPostsRecyclerview;
    ImageView closeButton;
    PostAdapter postAdapter;
    private SharedPrefUtils prefUtils;

    //This method is called when the activity is created.
    //It initializes various UI elements, such as a RecyclerView for displaying saved posts and a close button.
    //It also sets up a click listener for the close button.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_saved_posts);
        prefUtils = new SharedPrefUtils(this);

        savedPostsRecyclerview = findViewById(R.id.recyclerview_saved_posts);
        closeButton = findViewById(R.id.btn_close);

        closeButton.setOnClickListener(view -> finish());

        savedPostsRecyclerview.setHasFixedSize(true);

        postAdapter = new PostAdapter(this);
        savedPostsRecyclerview.setAdapter(postAdapter);

        readPosts();
    }

    // This method is responsible for fetching and displaying the user's saved posts
    // from the firebase and load them.
    void readPosts() {
        DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + prefUtils.get("email").replace(".",""));
        userReference.get().addOnSuccessListener(userSnapshot -> {
            User user = userSnapshot.toObject(User.class);
            assert user != null;
            // fetches each saved post from the user's firebase database
            for (DocumentReference savedPostReference : user.getSaved()) {
                savedPostReference.get().addOnSuccessListener(savedPostSnapshot -> postAdapter.addPost(savedPostSnapshot.toObject(Post.class)));
            }
        });
    }
}