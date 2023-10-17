package com.example.instapets.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instapets.R;
import com.example.instapets.activities.OtherProfileActivity;
import com.example.instapets.models.Comment;
import com.example.instapets.models.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * this class represents the widget of comment on a post
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    Context mContext;
    List<Comment> mComments;

    public CommentAdapter(Context mContext, List<Comment> mComments) {
        this.mContext = mContext;
        this.mComments = mComments;
    }

    //This method is called when the RecyclerView needs a new ViewHolder to represent an item.
    //It inflates the layout for an individual comment item (R.layout.adapter_comment)
    // and creates a new ViewHolder for it.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_comment, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    //This method is called for each item in the RecyclerView to bind data to the ViewHolder.
    //this method is loading the data from the mComments list from Firebase
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = mComments.get(position);
        DocumentReference publisherReference = FirebaseFirestore.getInstance().collection("Users").document(comment.getPublisherId());
        publisherReference.get().addOnSuccessListener(snapshot -> {
            User user = snapshot.toObject(User.class);
            if (user != null) {
                Glide.with(mContext).load(user.getProfileImageUrl()).into(holder.profileImage);
                holder.username.setText(user.getUsername());
                holder.comment.setText(comment.getText());

                holder.container.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, OtherProfileActivity.class);
                    intent.putExtra("userid", user.getId());
                    mContext.startActivity(intent);
                });
            }

        });
    }

    //This method returns the total number of items in the RecyclerView,
    //which is the size of the mComments list
    @Override
    public int getItemCount() {
        return mComments.size();
    }

    //This inner class represents the ViewHolder for an individual comment item in the RecyclerView.
    //It holds references to the profile image, username, comment text, and the comment container view.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username, comment;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            profileImage = itemView.findViewById(R.id.img_profile);
            username = itemView.findViewById(R.id.txt_username);
            comment = itemView.findViewById(R.id.txt_comment);
        }
    }
}
