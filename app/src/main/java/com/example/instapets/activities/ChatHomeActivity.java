package com.example.instapets.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instapets.R;
import com.example.instapets.models.Chatlist;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.instapets.adapters.ProfileAdapter;
import com.example.instapets.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * this class represents the chat list of the user
 */

public class ChatHomeActivity extends AppCompatActivity {
    RecyclerView recyclerViewProfiles;
    List<User> profiles, allProfiles;
    ProfileAdapter profileAdapter;
    ImageView closeButton;
    private List<Chatlist> usersList;
    private List<User> mUsers;
    FrameLayout frameLayout;

    private SharedPrefUtils prefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_chat_home);
        recyclerViewProfiles = findViewById(R.id.recyclerview_profiles);
        closeButton = findViewById(R.id.btn_close);
        frameLayout = findViewById(R.id.es_layout);

        prefUtils = new SharedPrefUtils(this);
        usersList = new ArrayList<>();

        profiles = new ArrayList<>();
        allProfiles = new ArrayList<>();
        profileAdapter = new ProfileAdapter(this, profiles, "MESSAGE");
        recyclerViewProfiles.setHasFixedSize(true);
        recyclerViewProfiles.setAdapter(profileAdapter);

        readProfiles();
        closeButton.setOnClickListener(view -> {
            finish();
        });
    }

    //These method is used to read and display user profiles in a chat application
    private void readProfiles() {
        //bringing up the FireBase instance for load the chatList from the current user by using the email key
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(prefUtils.get("email").replace(".",""));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //starting to upload the users into our arrayList
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }
                //in case user has no friends..
                if(usersList.size()==0){
                    frameLayout.setVisibility(View.VISIBLE);
                }
                else{
                    frameLayout.setVisibility(View.GONE);
                }

                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*
     * this method is responsible for populating a list of users (mUsers)
     * for a chat application
     */
    private void chatList() {
        mUsers = new ArrayList<>();

        //bringing up the users collection from the FireBase instance
        CollectionReference userReference = FirebaseFirestore.getInstance().collection("Users");
        userReference.get().addOnSuccessListener(usersSnapshots -> {
            mUsers.clear();
            //now for filling up the list we will pick only the users that got the same chat id with our user
            for (DocumentSnapshot userSnapshot : usersSnapshots) {
                User user = userSnapshot.toObject(User.class);
                for (Chatlist chatlist : usersList){
                    if (user!= null && user.getId()!=null && chatlist!=null && chatlist.getId()!= null &&
                            user.getId().equals(chatlist.getId())){
                        mUsers.add(user);
                    }
                }
            }
            /*
             * creates a profile adapters in the ChatHomeActivity for
             * all the users that added into the mUsers
             */
            profileAdapter = new ProfileAdapter(ChatHomeActivity.this, mUsers, "MESSAGE");
            recyclerViewProfiles.setAdapter(profileAdapter);
        });

    }

}