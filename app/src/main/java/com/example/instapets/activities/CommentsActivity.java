package com.example.instapets.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.instapets.R;
import com.example.instapets.adapters.CommentAdapter;
import com.example.instapets.models.Comment;
import com.example.instapets.utilities.DateFormatter;
import com.example.instapets.utilities.KeyboardManager;

import java.util.ArrayList;
import java.util.List;

/**
 * this class represents the activity of comment on a post
 */

public class CommentsActivity extends AppCompatActivity {
    RecyclerView recyclerViewComments;
    EditText commentText;
    ImageView sendButton, closeButton;
    CommentAdapter commentAdapter;
    List<Comment> comments;
    DatabaseReference commentsReference;
    private SharedPrefUtils prefUtils;

    // This method is called when the activity is created. Key functionality includes:
    //Setting the status bar color to white, initializing views and data structures,
    //registering click listeners for the send button and close button,
    //reading and displaying comments by calling the readComments() method
    //and handling a flag to open the keyboard if necessary.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_comments);
        prefUtils = new SharedPrefUtils(this);

        recyclerViewComments = findViewById(R.id.recyclerview_comments);
        commentText = findViewById(R.id.et_comment);
        sendButton = findViewById(R.id.btn_send_message);
        closeButton = findViewById(R.id.btn_close);

        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, comments);
        recyclerViewComments.setHasFixedSize(true);
        recyclerViewComments.setAdapter(commentAdapter);
        String postId = getIntent().getExtras().getString("postid");
        commentsReference = FirebaseDatabase.getInstance().getReference().child("comments").child(postId);

        readComments();
        boolean openKeyboard = getIntent().getExtras().getBoolean("openKeyboard");
        if (openKeyboard) {
            commentText.requestFocus();
            KeyboardManager.openKeyboard(this);
        }

        //committing here a comment in a post
        sendButton.setOnClickListener(v -> {
            String commentString = commentText.getText().toString();
            if (commentString.isEmpty()) return;
            DocumentReference userReference = FirebaseFirestore.getInstance().collection("Users").document(prefUtils.get("email").replace(".", ""));

            String commentId = DateFormatter.getCurrentTime();
            Comment comment = new Comment(userReference.getId(), commentString, commentId);

            KeyboardManager.closeKeyboard(this);
            commentText.clearFocus();
            commentText.setText("");
            commentsReference.child(commentId).setValue(comment).addOnSuccessListener(unused -> {
                comments.add(comment);
                commentAdapter.notifyDataSetChanged();
            });
        });
        closeButton.setOnClickListener(view -> finish());
    }

     // This method is used to intercept touch events sent to the activity before
     // they are dispatched to the window. In this case, it's being used to handle
     // touch events and close the keyboard if it is currently open
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View focused = getCurrentFocus();
        if (focused != null) {
            KeyboardManager.closeKeyboard(this);
        }
        return super.dispatchTouchEvent(event);
    }

    // this method meant for representing all the comments in a post from the firebase
    private void readComments() {
        //getting from the Firebase all the comments
        commentsReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //clears the arrayList before entering the relative comments
                comments.clear();
                for (DataSnapshot commentSnapshot : task.getResult().getChildren()) {
                    //all comments who has the correct value for our post - will be added to the list
                    comments.add(commentSnapshot.getValue(Comment.class));
                }
                commentAdapter.notifyDataSetChanged();
            }
        });
    }
}