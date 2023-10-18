package com.example.instapets.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instapets.R;
import com.example.instapets.adapters.MessageAdapter;
import com.example.instapets.models.User;
import com.example.instapets.models.Chat;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * this class represents the activity of chat with other user
 */

public class MessageActivity extends AppCompatActivity {
    CircleImageView profile_image;
    TextView username;
    User fellow;
    String fuser;
    DatabaseReference reference;
    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    Intent intent;

    String userid;

    SharedPrefUtils sharedPrefUtils;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        sharedPrefUtils = new SharedPrefUtils(this);

        Toolbar toolbar = findViewById(R.id.toolbars);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = sharedPrefUtils.get("email").replace(".","");

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString();
                String time = String.valueOf(System.currentTimeMillis());
                if (!msg.equals("")){
                    sendMessage(fuser, userid, msg, time);

                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        //fetches all the user's users from the firebase
        CollectionReference users = FirebaseFirestore.getInstance().collection("Users");
        users.document(userid).get().addOnSuccessListener(documentSnapshot -> {
            fellow = documentSnapshot.toObject(User.class);
            assert fellow != null;
            username.setText(fellow.getUsername());
            if (fellow.getProfileImageUrl()==null){
                profile_image.setImageResource(R.drawable.logo);
            } else {
                Glide.with(getApplicationContext()).load(fellow.getProfileImageUrl()).into(profile_image);
            }
            username.setText(fellow.getUsername());
            readMesagges(fuser, userid, fellow.getProfileImageUrl());
        });
    }

    //this methode sends in a real time a message and saves it into the firebase
    private void sendMessage(String sender, final String receiver, String message, String time){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("time", time);

        //pushes the message data to the "Chats" node in the Firebase Realtime
        reference.child("Chats").push().setValue(hashMap);

        //updates Chatlist for Sender
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser)
                .child(userid);

        /*
         * This code checks if the chat list entry for the sender
         * (fuser) and receiver (userid) already exists.
         * If it doesn't exist, it creates a new entry with the id
         * field set to the receiver's user ID.
         */
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser);
        chatRefReceiver.child("id").setValue(fuser);

    }


    // method is used to read chat messages from a Firebase Realtime
    private void readMesagges(final String myid, final String userid, final String imageurl){
        //stores the chat messages
        mchat = new ArrayList<>();
        //creates the message adapter that is shown in text
        messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                /*
                 * For each chat message, it deserializes the data
                 * into a Chat object.
                 * Checks if the message involves the current user (myid)
                 * and the chat partner (userid) as either sender or receiver.
                 * If the condition is met, the chat message is added to the mchat list.
                 */
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                    }
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // doesn't do a thing.. comes with the program
    @Override
    protected void onResume() {
        super.onResume();
    }
    // doesn't do a thing.. comes with the program
    @Override
    protected void onPause() {
        super.onPause();
    }

}