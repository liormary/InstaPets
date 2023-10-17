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
import com.example.instapets.R;
import com.example.instapets.activities.CommentsActivity;
import com.example.instapets.models.Post;
import com.example.instapets.models.User;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Comparator;
import java.util.TreeSet;

/**
 * this class represents the widget of the pictures that the user upload in his profile
 */

public class MyPictureAdapter extends RecyclerView.Adapter<MyPictureAdapter.ViewHolder> {
    Context mContext;
    TreeSet<DocumentReference> mPosts;
    FirebaseFirestore db;
    DocumentReference userReference;
    private SharedPrefUtils prefUtils;
    boolean isLiked;


    public MyPictureAdapter(Context mContext) {
        this.mContext = mContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPosts = new TreeSet<>(Comparator.comparing(DocumentReference::getId).reversed());
        }
    }

    public void addPost(DocumentReference post) {
        mPosts.add(post);
        notifyDataSetChanged();
    }

    public void clearPosts() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_my_picture, parent, false);
        prefUtils = new SharedPrefUtils(mContext);
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("Users").document(prefUtils.get("email").replace(".", ""));
        return new MyPictureAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentReference postReference = mPosts.toArray(new DocumentReference[0])[position];
        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_my_picture_preview);
        TextView caption;
        View header;
        LottieAnimationView likeAnimation;
        ImageView profileImage, like, comment, save, postImage;
        TextView username, noOfLikes, kitt, commentCount, time;
        ImageView previewImageView = dialog.findViewById(R.id.img_mypost);
        profileImage = dialog.findViewById(R.id.img_profile);
        noOfLikes = dialog.findViewById(R.id.txt_likes);
        username = dialog.findViewById(R.id.txt_username);
        likeAnimation = dialog.findViewById(R.id.animation_like);
        caption = dialog.findViewById(R.id.caption);
        commentCount = dialog.findViewById(R.id.txt_comment_count);
        like = dialog.findViewById(R.id.btn_like);
        comment = dialog.findViewById(R.id.btn_comment);
        postReference.get().addOnSuccessListener(postSnapshot -> {
            Post post = postSnapshot.toObject(Post.class);
            assert post != null;

            db.collection("Users").document(post.getCreator()).get().addOnSuccessListener(snapshot -> {
                User creator = snapshot.toObject(User.class);
                Glide.with(mContext).load(creator.getProfileImageUrl()).into(profileImage);
                username.setText(creator.getUsername());
            });
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
            Glide.with(mContext).load(post.getImageUrl()).into(holder.myPostImage);
            Glide.with(dialog.getContext()).load(post.getImageUrl()).into(previewImageView);
            caption.setText(post.getCaption());

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


        holder.myPostImage.setOnClickListener(v -> {
            dialog.show();
//            return true;
        });


    }


    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView myPostImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myPostImage = itemView.findViewById(R.id.img_mypost);

        }

    }
}
