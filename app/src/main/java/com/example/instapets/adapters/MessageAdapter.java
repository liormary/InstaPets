package com.example.instapets.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instapets.R;
import com.example.instapets.models.Chat;
import com.example.instapets.utilities.SharedPrefUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * this class represents the widget of the messages in the chat
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    SharedPrefUtils sharedPrefUtils;
    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;
    String fuser; //A string to store the user's email (used to identify sent messages).

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageurl = imageurl;
        sharedPrefUtils = new SharedPrefUtils(mContext);
    }

    //This method is called when the RecyclerView needs a new ViewHolder for a message item.
    //It inflates the appropriate layout for either sent or received messages based on the message type (viewType).
    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    //This method is called for each item in the RecyclerView to bind data to the ViewHolder.
    //It retrieves data from the mChat list for the specific position
    //and displays the message text, sender's profile image, and message status ("Seen" or "Delivered").
    //It also formats and displays the message timestamp.
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage());
        if (chat.getTime() != null && !chat.getTime().trim().equals("")) {
            holder.time_tv.setText(holder.convertTime(chat.getTime()));
        }

        if (imageurl == null || imageurl.equals("default")) {
            holder.profile_image.setImageResource(R.drawable.logo);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }

        if (position == mChat.size() - 1) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    //This method returns the total number of items in the RecyclerView, which is the size of the mChat list.
    @Override
    public int getItemCount() {
        return mChat.size();
    }

    //This inner class represents the ViewHolder for a message item in the RecyclerView.
    //It holds references to UI elements like the message text,
    //sender's profile image, message status, and timestamp.
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public TextView time_tv;
        public RelativeLayout rldelete;


        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            time_tv = itemView.findViewById(R.id.time_tv);
            rldelete = itemView.findViewById(R.id.leftRelative);
        }

        //This method is used to convert a timestamp (in milliseconds)
        //to a formatted time string in the "h:mm a" format
        public String convertTime(String time) {
            SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
            String dateString = formatter.format(new Date(Long.parseLong(time)));
            return dateString;
        }
    }

    //This method determines the view type (left or right) for a message item at a given position
    //based on whether the sender's email matches the user's email (stored in shared preferences).
    @Override
    public int getItemViewType(int position) {
        fuser = sharedPrefUtils.get("email").replace(".","");
        if (mChat.get(position).getSender().equals(fuser)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}