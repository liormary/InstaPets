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
import com.example.instapets.activities.MessageActivity;
import com.example.instapets.activities.OtherProfileActivity;
import com.example.instapets.models.User;

import java.util.List;
/**
 * this class represents the widget of the users profiles (for the chatlist and the search)
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {
    Context mContext;
    List<User> mUsers;
    String use;

    //The constructor accepts a Context, a list of User objects (mUsers), and a String (use).
    // This information is typically used to determine the purpose of the profile adapter
    // (displaying profiles for the current user's profile or for use in a messaging activity).
    public ProfileAdapter(Context mContext, List<User> mUsers, String use) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.use = use;
    }

    //Inflates the layout for each item in the RecyclerView using the R.layout.adapter_profile resource.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adapter_profile, parent, false);
        return new ProfileAdapter.ViewHolder(view);
    }

    //Binds the data of a User object to the corresponding views within a ViewHolder (holder).
    // This includes loading the user's profile image, setting their username and name.
    // The use parameter helps decide the action to be taken when a user clicks on a profile item.
    // For example, it may navigate to the user's profile or open a messaging activity.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);
        Glide.with(mContext).load(user.getProfileImageUrl()).into(holder.profileImage);
        holder.username.setText(user.getUsername());
        holder.name.setText(user.getName());

        holder.container.setOnClickListener(view -> {
            Intent intent;
            if (use.equals("PROFILE")) {
                intent = new Intent(mContext, OtherProfileActivity.class);
            } else {
                intent = new Intent(mContext, MessageActivity.class);
            }
            intent.putExtra("userid", user.getId());
            mContext.startActivity(intent);
        });
    }

    //Returns the number of items in the list of mUsers,
    // which corresponds to the number of profiles to be displayed.
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    //Updates the list of mUsers with a filtered list and notifies the adapter that the data has changed.
    // This is often used for implementing search or filtering functionality in the RecyclerView.
    public void filterList(List<User> filteredList) {
        mUsers = filteredList;
        notifyDataSetChanged();
    }

    //This inner class represents the view holder for each item in the RecyclerView.
    // It holds references to the views within the layout for a profile item.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView username, name;
        View container;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            profileImage = itemView.findViewById(R.id.img_profile);
            username = itemView.findViewById(R.id.txt_username);
            name = itemView.findViewById(R.id.txt_name);
        }
    }
}
