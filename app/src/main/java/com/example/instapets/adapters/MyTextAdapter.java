package com.example.instapets.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.instapets.activities.CommentsActivity;
import com.example.instapets.models.User;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.example.instapets.R;
import com.example.instapets.models.Post;
import com.example.instapets.utilities.DateFormatter;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * this class represents the widget of the text posts that the user upload in his profile
 */

public class MyTextAdapter extends RecyclerView.Adapter<MyTextAdapter.ViewHolder> {
    Context mContext;
    TreeSet<DocumentReference> mPosts;
    FirebaseFirestore db;
    DocumentReference userReference;
    private SharedPrefUtils prefUtils;
    boolean isLiked;

    //The constructor initializes the adapter with the provided Context.
    //It also initializes the mPosts collection, Firebase, and the userReference.
    //The constructor checks the Android API version and creates a TreeSet with a comparator
    //that orders document references based on their IDs in reverse order (latest first).
    public MyTextAdapter(Context mContext) {
        this.mContext = mContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPosts = new TreeSet<>(Comparator.comparing(DocumentReference::getId).reversed());
        }
    }

    //This method adds a new text post (represented by a DocumentReference)
    //to the mPosts collection and notifies the adapter that the data has changed.
    public void addPost(DocumentReference post) {
        mPosts.add(post);
        notifyDataSetChanged();
    }

    //This method clears all text posts from the mPosts collection
    //and notifies the adapter that the data has changed.
    public void clearPosts() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    //This method is called when the RecyclerView needs a new ViewHolder for a text post item.
    //It inflates the appropriate layout for text posts (R.layout.adapter_my_kitt).
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_my_kitt, parent, false);
        prefUtils = new SharedPrefUtils(mContext);
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("Users").document(prefUtils.get("email").replace(".",""));
        return new MyTextAdapter.ViewHolder(view);
    }

    //This method is called for each item in the RecyclerView to bind data to the ViewHolder.
    // It loads and displays text posts, including the user's profile image, post likes, comments,
    // and the post's content (kitt).
    // It also handles user interactions like liking posts and opening a detailed preview of the text post.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentReference postReference = mPosts.toArray(new DocumentReference[0])[position];
        Dialog dialog = new Dialog(mContext);
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        dialog.setContentView(R.layout.dialog_my_kitt_preview);
        TextView previewTextView = dialog.findViewById(R.id.kitt_mypost);
        ImageView  like;
        ImageView profileImage, comment, save, postImage;
        View header;
        LottieAnimationView likeAnimation;
        TextView username, noOfLikes, kitt, commentCount, time;

        ImageView previewImageView = dialog.findViewById(R.id.img_mypost);
        postImage = dialog.findViewById(R.id.img_post);
        profileImage = dialog.findViewById(R.id.img_profile);
        time = dialog.findViewById(R.id.txt_time);
        header = dialog.findViewById(R.id.header);
        like = dialog.findViewById(R.id.btn_like);
        likeAnimation = dialog.findViewById(R.id.animation_like);
        save = dialog.findViewById(R.id.btn_save);
        comment = dialog.findViewById(R.id.btn_comment);
        noOfLikes = dialog.findViewById(R.id.txt_likes);
        username = dialog.findViewById(R.id.txt_username);
        commentCount = dialog.findViewById(R.id.txt_comment_count);
        kitt = dialog.findViewById(R.id.txt_kitt);

        postReference.get().addOnSuccessListener(postSnapshot -> {
            Post post = postSnapshot.toObject(Post.class);
            assert post != null;

            db.collection("Users").document(post.getCreator()).get().addOnSuccessListener(snapshot -> {
                User creator = snapshot.toObject(User.class);
                Glide.with(mContext).load(creator.getProfileImageUrl()).into(profileImage);
                username.setText(creator.getUsername());
            });
            holder.kitt.setText(post.getKitt());
            holder.time.setText(DateFormatter.getTimeDifference(post.getPostid()));
            holder.container.setVisibility(View.VISIBLE);
            previewTextView.setText(holder.kitt.getText().toString());
            int n = post.getLikes().size();
            if (n > 0) {
                noOfLikes.setVisibility(View.VISIBLE);
                String str = n + (n > 1 ? " likes" : " like");
                noOfLikes.setText(str);
            } else {
                noOfLikes.setVisibility(View.GONE);
            }
            isLiked = post.getLikes().contains(userReference);

            if (isLiked) like.setImageResource(R.drawable.ic_heart);
            else like.setImageResource(R.drawable.ic_heart_outlined);
            DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference().child("comments").child(post.getPostid());

            commentsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long commentCounts = snapshot.getChildrenCount();
                    if (commentCounts >= 1) {
                        String commentsCountText = "View all " + commentCounts + " comments";
                        commentCount.setText(commentsCountText);
                        commentCount.setVisibility(View.VISIBLE);
                    } else {
                        commentCount.setText("");
                        commentCount.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            commentCount.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("openKeyboard", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(intent);
            });

            comment.setOnClickListener(view -> {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("openKeyboard", true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mContext.startActivity(intent);
            });

            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    isLiked = !isLiked;
                    if (isLiked) {
                        likeAnimation.playAnimation();
                        likeAnimation.setVisibility(View.VISIBLE);
                    } else {
                        likeAnimation.setVisibility(View.INVISIBLE);
                    }
                    DocumentReference postReference = FirebaseFirestore.getInstance().collection("Posts").document(post.getPostid());
                    if (isLiked) {
                        postReference.update("likes", FieldValue.arrayUnion(userReference));
                        post.getLikes().add(userReference);
                    } else {
                        postReference.update("likes", FieldValue.arrayRemove(userReference));
                        post.getLikes().remove(userReference);
                    }
                    int n = post.getLikes().size();
                    if (n > 0) {
                        noOfLikes.setVisibility(View.VISIBLE);
                        String str = n + (n > 1 ? " likes" : " like");
                        noOfLikes.setText(str);
                    } else {
                        noOfLikes.setVisibility(View.GONE);
                    }

                    if (isLiked) like.setImageResource(R.drawable.ic_heart);
                    else like.setImageResource(R.drawable.ic_heart_outlined);
                }
            });

        });


        holder.container.setOnClickListener(v -> {
            dialog.show();
        });
    }

    //This method returns the total number of text posts in the mPosts collection.
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    //This inner class represents the ViewHolder for a text post item in the RecyclerView.
    // It holds references to UI elements like the text content (kitt), the post's timestamp,
    // and the post container view.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView kitt, time;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            kitt = itemView.findViewById(R.id.txt_kitt);
            time = itemView.findViewById(R.id.txt_time);
            container = itemView.findViewById(R.id.container);
        }
    }
}
