package com.example.instapets.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.instapets.R;
import com.example.instapets.activities.CommentsActivity;
import com.example.instapets.activities.MainActivity;
import com.example.instapets.activities.OtherProfileActivity;
import com.example.instapets.models.Post;
import com.example.instapets.models.User;
import com.example.instapets.utilities.DateFormatter;
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
 * this class represents the widget of the posts in the main screen
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    Context mContext;
    TreeSet<Post> mPosts;
    FirebaseFirestore db;
    DocumentReference userReference;
    private SharedPrefUtils prefUtils;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; //milliseconds
    long lastClickTime = 0;

    //The constructor initializes the adapter with the provided Context.
    // It also initializes the mPosts collection, Firebase, and the userReference.
    // The constructor checks the Android API version and creates a TreeSet
    // with a comparator that orders posts based on their IDs.
    public PostAdapter(Context mContext) {
        this.mContext = mContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPosts = new TreeSet<>(Comparator.comparing(Post::getPostid));
        }
    }

    //This method adds a new post to the mPosts collection and notifies the adapter that the data has changed.
    public void addPost(Post post) {
        try {
            if (post.getPostid() != null) {
                mPosts.add(post);
                notifyDataSetChanged();
            }
        } catch (Exception e) {
        }
    }

    //This method clears all posts from the mPosts collection and notifies the adapter that the data has changed.
    public void clearPosts() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    //This method is called when the RecyclerView needs a new ViewHolder for a post item.
    // It inflates the appropriate layout for posts (R.layout.adapter_post)
    // and initializes shared preferences and Firebase.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_post, parent, false);
        prefUtils = new SharedPrefUtils(mContext);
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("Users").document(prefUtils.get("email").replace(".", ""));
        ViewHolder.userReference = userReference;
        return new ViewHolder(view);
    }

    //This method is called for each item in the RecyclerView to bind data to the ViewHolder.
    // It loads and displays posts, including the user's profile image,
    // post details (likes, comments, captions, and kitts),
    // and handles user interactions like liking, saving, and opening post previews.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.toArray(new Post[0])[position];
        db.collection("Users").document(post.getCreator()).get().addOnSuccessListener(snapshot -> {
            User creator = snapshot.toObject(User.class);
            Glide.with(mContext).load(creator.getProfileImageUrl()).into(holder.profileImage);
            holder.username.setText(creator.getUsername());
        });

        holder.post = post;
        holder.time.setText(DateFormatter.getTimeDifference(post.getPostid()));
        Glide.with(mContext).load(post.getImageUrl()).into(holder.postImage);
        holder.caption.setText(post.getCaption());
        holder.kitt.setText(post.getKitt());

        if (post.getPostid() != null) {
            if (prefUtils.get("email").replace(".", "").equals(post.getCreator())) {
                holder.delete.setVisibility(View.VISIBLE);
            } else {
                holder.delete.setVisibility(View.GONE);
            }

        }

        holder.delete.setOnClickListener(view -> {
            db.collection("Posts").document(post.getPostid()).delete().addOnSuccessListener(snapshot -> {

            });
            DocumentReference postRef = db.collection("Posts").document(post.getPostid());

            db.collection("Users").document(post.getCreator()).update("posts", FieldValue.arrayRemove(postRef)).addOnSuccessListener(snapshot -> {

            });
            db.collection("Users").document(post.getCreator()).collection("feed").document(post.getPostid()).delete().addOnSuccessListener(snapshot -> {

            });
            Intent intent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(intent);
        });
        if (post.getKitt().isEmpty()) {
            holder.kitt.setVisibility(View.GONE);
            holder.caption.setVisibility(View.VISIBLE);
        } else {
            holder.kitt.setVisibility(View.VISIBLE);
            holder.caption.setVisibility(View.GONE);
        }


        holder.checkIfLiked();
        holder.checkIfSaved();

        DatabaseReference commentsReference = FirebaseDatabase.getInstance().getReference().child("comments").child(post.getPostid());
        commentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long commentCount = snapshot.getChildrenCount();
                if (commentCount >= 1) {
                    String commentsCountText = "View all " + commentCount + " comments";
                    holder.commentCount.setText(commentsCountText);
                    holder.commentCount.setVisibility(View.VISIBLE);
                } else {
                    holder.commentCount.setText("");
                    holder.commentCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.postImage.setOnClickListener(view -> {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                holder.toggleLike();
            }
            lastClickTime = clickTime;
        });
        holder.like.setOnClickListener(v -> holder.toggleLike());
        holder.save.setOnClickListener(v -> holder.toggleSave());

        holder.commentCount.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("openKeyboard", false);
            mContext.startActivity(intent);
        });

        holder.comment.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("openKeyboard", true);
            mContext.startActivity(intent);
        });

        holder.header.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, OtherProfileActivity.class);
            intent.putExtra("userid", post.getCreator());
            mContext.startActivity(intent);
        });
    }

    //This method returns the total number of posts in the mPosts collection.
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    //This inner class represents the ViewHolder for a post item in the RecyclerView.
    // It holds references to UI elements like the post content (images, text), the user's profile image,
    // post details (likes, comments, captions, and kitts), and buttons for liking, saving,
    // and commenting on the post.
    // It also includes methods for updating likes and saves and handling user interactions.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage, like, comment, save, postImage;
        public View header;
        LottieAnimationView likeAnimation;
        public static DocumentReference userReference;
        public TextView username, noOfLikes, caption, kitt, commentCount, time;
        protected boolean isLiked, isSaved;
        public Post post;
        LinearLayout delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.img_post);
            profileImage = itemView.findViewById(R.id.img_profile);
            time = itemView.findViewById(R.id.txt_time);
            header = itemView.findViewById(R.id.header);
            delete = itemView.findViewById(R.id.delete);
            like = itemView.findViewById(R.id.btn_like);
            likeAnimation = itemView.findViewById(R.id.animation_like);
            save = itemView.findViewById(R.id.btn_save);
            comment = itemView.findViewById(R.id.btn_comment);
            noOfLikes = itemView.findViewById(R.id.txt_likes);
            username = itemView.findViewById(R.id.txt_username);
            caption = itemView.findViewById(R.id.caption);
            commentCount = itemView.findViewById(R.id.txt_comment_count);
            kitt = itemView.findViewById(R.id.txt_kitt);

            likeAnimation.addAnimatorListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(@NonNull Animator animation, boolean isReverse) {

                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation, boolean isReverse) {
                    likeAnimation.setVisibility(View.INVISIBLE);
                }
            });
        }

        //This method updates the UI elements based on the current state of the post item,
        // such as the number of likes and the "like" and "save" buttons' appearance (filled or
        // outlined heart and save icons).
        void update() {
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

            if (isSaved) save.setImageResource(R.drawable.ic_save);
            else save.setImageResource(R.drawable.ic_save_outlined);
        }

        //This method is called when the user interacts with the "like" button.
        // It toggles the "like" status, triggers the "like" animation if applicable,
        // and updates the likes data by either adding or removing the user's reference
        // to the post's likes. Finally, it calls update() to refresh the UI.
        public void toggleLike() {
            isLiked = !isLiked;
            if (isLiked) {
                likeAnimation.playAnimation();
                likeAnimation.setVisibility(View.VISIBLE);
            } else {
                likeAnimation.setVisibility(View.INVISIBLE);
            }
            updateLikesData();
        }

        //This method is called when the user interacts with the "save" button.
        // It toggles the "save" status and updates the saved data by either adding
        // or removing the post reference from the user's saved posts.
        // It then calls update() to update the UI.
        public void toggleSave() {
            isSaved = !isSaved;
            updateSavedData();
        }

        //This method checks if the user has already liked the post by checking
        // if the user's reference is present in the post's likes.
        // It updates the isLiked flag accordingly.
        private void checkIfLiked() {
            isLiked = post.getLikes().contains(userReference);
            update();
        }

        //This method updates the Firebase data for post likes based on the isLiked flag.
        // It adds or removes the user's reference to the post's likes and calls update() to refresh the UI.
        void updateLikesData() {
            DocumentReference postReference = FirebaseFirestore.getInstance().collection("Posts").document(post.getPostid());
            if (isLiked) {
                postReference.update("likes", FieldValue.arrayUnion(userReference));
                post.getLikes().add(userReference);
            } else {
                postReference.update("likes", FieldValue.arrayRemove(userReference));
                post.getLikes().remove(userReference);
            }
            update();
        }

        //This method checks if the user has saved the post by checking if
        // the post reference is present in the user's saved posts.
        // It updates the isSaved flag accordingly.
        public void checkIfSaved() {
            DocumentReference postReference = FirebaseFirestore.getInstance().collection("Posts").document(post.getPostid());
            userReference.get().addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    isSaved = user.getSaved().contains(postReference);

                }
                update();
            });
        }

        //This method updates the Firebase data for saved posts based on the isSaved flag.
        // It adds or removes the post reference from the user's saved posts and calls update() to update the UI.
        private void updateSavedData() {
            DocumentReference postReference = FirebaseFirestore.getInstance().collection("Posts").document(post.getPostid());
            if (isSaved) {
                userReference.update("saved", FieldValue.arrayUnion(postReference));
            } else {
                userReference.update("saved", FieldValue.arrayRemove(postReference));
            }
            update();
        }
    }
}
