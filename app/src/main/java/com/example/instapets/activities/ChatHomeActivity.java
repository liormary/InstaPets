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

    DatabaseReference reference;

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

    private void readProfiles() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(prefUtils.get("email").replace(".",""));
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }
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
    private void chatList() {
        mUsers = new ArrayList<>();

        CollectionReference userReference = FirebaseFirestore.getInstance().collection("Users");
        userReference.get().addOnSuccessListener(usersSnapshots -> {
            mUsers.clear();
            for (DocumentSnapshot userSnapshot : usersSnapshots) {
                User user = userSnapshot.toObject(User.class);
                for (Chatlist chatlist : usersList){
                    if (user!= null && user.getId()!=null && chatlist!=null && chatlist.getId()!= null &&
                            user.getId().equals(chatlist.getId())){
                        mUsers.add(user);
                    }
                }
            }
            profileAdapter = new ProfileAdapter(ChatHomeActivity.this, mUsers, "MESSAGE");
            recyclerViewProfiles.setAdapter(profileAdapter);
        });

    }

}