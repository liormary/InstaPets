package com.example.instapets.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instapets.R;
import com.example.instapets.models.User;
import com.example.instapets.utilities.ProfilePageManager;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class represents the activity of other users profile
 */
public class OtherProfileActivity extends AppCompatActivity {
    ProfilePageManager profilePageManager;
    DocumentReference userReference, myReference;
    Button followButton, messageButton;
    boolean amFollowing;
    private SharedPrefUtils prefUtils;

    //This method is called when the activity is created.
    //It sets up click listeners for the follow/unfollow button and the message button,
    //it checks if the current user is viewing their own profile, and if so,
    // hides the follow/unfollow and message buttons.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_other_profile);
        prefUtils = new SharedPrefUtils(this);

        String userid = getIntent().getStringExtra("userid");
        userReference = FirebaseFirestore.getInstance().collection("Users").document(userid);
        myReference = FirebaseFirestore.getInstance().collection("Users").document(prefUtils.get("email").replace(".", ""));

        profilePageManager = new ProfilePageManager(this.getWindow().getDecorView());

        followButton = findViewById(R.id.btn_follow);
        messageButton = findViewById(R.id.btn_message);

        if (myReference.equals(userReference)) {
            followButton.setVisibility(View.GONE);
            messageButton.setVisibility(View.GONE);
        }

        fillUserData();
        readPosts();

        //preform the follow or unfollow action
        followButton.setOnClickListener(v -> toggleFollow());
        messageButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra("userid", userid);
            startActivity(intent);
        });
    }

    //This method allows the current user to follow or unfollow other user
    private void toggleFollow() {
        amFollowing = !amFollowing;
        //in any case of following or not - she shall update the firebase
        if (amFollowing) {//in case of follows
            profilePageManager.noOfFollowers.setText(String.valueOf(Integer.parseInt(profilePageManager.noOfFollowers.getText().toString()) + 1));

            myReference.update("following", FieldValue.arrayUnion(userReference));
            userReference.update("followers", FieldValue.arrayUnion(myReference)).addOnSuccessListener(unused -> readPosts());
            userReference.get().addOnSuccessListener(userSnapshot -> {
                User user = userSnapshot.toObject(User.class);
                assert user != null;
                List<DocumentReference> posts = user.getPosts();
                for (DocumentReference postReference : posts) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("postReference", postReference);
                    map.put("visited", false);
                    myReference.collection("feed").document(postReference.getId()).set(map);
                }
            });
        } else {//in case of unfollow
            profilePageManager.noOfFollowers.setText(String.valueOf(Integer.parseInt(profilePageManager.noOfFollowers.getText().toString()) - 1));

            myReference.update("following", FieldValue.arrayRemove(userReference));
            userReference.update("followers", FieldValue.arrayRemove(myReference)).addOnSuccessListener(unused -> readPosts());
            userReference.get().addOnSuccessListener(userSnapshot -> {
                User user = userSnapshot.toObject(User.class);
                assert user != null;
                List<DocumentReference> posts = user.getPosts();
                for (DocumentReference postReference : posts) {
                    myReference.collection("feed").document(postReference.getId()).delete();
                }
            });
        }
        updateFollowButton();
    }

    //This method updates the text and appearance of the follow button based on whether
    // the current user is following the other user.
    // It sets the button text to "Following" or "Follow" and changes the background color accordingly.
    void updateFollowButton() {
        if (amFollowing) {
            followButton.setText("Following");
            followButton.setBackgroundColor(getResources().getColor(R.color.button_gray));
        } else {
            followButton.setText("Follow");
            followButton.setBackgroundColor(getResources().getColor(R.color.button_pink));
        }
    }

    // This method creates the other user's page with all the info from firebase
    void fillUserData() {
        userReference.get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            assert user != null;

            profilePageManager.fillUserData(user);
            if (user.getFollowers() != null) {
                amFollowing = user.getFollowers().contains(myReference);
                updateFollowButton();
            }
        });
    }

    //This method displays the posts of the other user on his profile page.
    //It reads the posts associated with the other user from Firebase and displays them in a scrollable list
    private void readPosts() {
        profilePageManager.readPosts(userReference);
    }
}