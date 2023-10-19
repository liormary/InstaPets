package com.example.instapets.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instapets.R;
import com.example.instapets.activities.ChatHomeActivity;
import com.example.instapets.activities.PostActivity;
import com.example.instapets.adapters.PostAdapter;
import com.example.instapets.models.Post;
import com.example.instapets.utilities.PromptDialog;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * this class represents the screen of the home page
 */

public class HomeFragment extends Fragment {

    RecyclerView recyclerViewPosts;
    PostAdapter postAdapter;
    Toolbar toolbar;
    private SharedPrefUtils prefUtils;
    ExtendedFloatingActionButton floatingActionButton;


    //This method is part of the Android Fragment lifecycle and is called
    // when the fragment needs to create its user interface (UI).
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts);
        prefUtils = new SharedPrefUtils(requireActivity());
        toolbar = view.findViewById(R.id.top_menu);
        floatingActionButton = view.findViewById(R.id.extendedFloatingButton);

        // When the user scrolls, it determines whether to extend or shrink the floating action button.
        recyclerViewPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    floatingActionButton.extend();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && floatingActionButton.isExtended()) {
                    floatingActionButton.shrink();
                }

            }
        });

        //When the user clicks the floating action button, it opens a dialog for creating a new post.
        // The specific type of post (text or picture) can be chosen.
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PromptDialog.getInstance(-1, dl).show(requireActivity().getSupportFragmentManager(), "");

            }
        });

        //The toolbar navigates to a chat activity.
        toolbar.setOnMenuItemClickListener(item -> {
            Intent intent;
            if (item.getItemId() == R.id.nav_chat) {
                    intent = new Intent(requireActivity(), ChatHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
            }
            return true;
        });

        postAdapter = new PostAdapter(requireContext());
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setAdapter(postAdapter);
        readPosts();
        return view;
    }

    //A PromptDialog is used for creating new posts.
    // It allows users to choose the type of post to create.
    //The dl listener handles the actions when the prompt dialog is dismissed.
    // Depending on the user's choice (picture or text post),
    // it starts the corresponding activity for creating a new post.
    PromptDialog.DismissListener dl = new PromptDialog.DismissListener() {

        @Override
        public void onDialogDismiss(int requestCode, int resultCode) {

        }

        @Override
        public void onpPhotoEntry(int requestCode, int resultCode) {
            Intent intent = new Intent(requireActivity(), PostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("type", "picture");
            startActivity(intent);
        }

        @Override
        public void onTextEntry(int requestCode, int resultCode) {
            Intent intent = new Intent(requireActivity(), PostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("type", "text");
            startActivity(intent);
        }


    };

    //This method fetches posts from a Firebase collection named "feed."
    // These posts are associated with the user's feed.
    // It clears the existing posts in the adapter and populates it with the retrieved posts.
    void readPosts() {
        CollectionReference feedReference = FirebaseFirestore.getInstance().collection("Users").document(prefUtils.get("email").replace(".", "")).collection("feed");
        feedReference.get().addOnSuccessListener(feedSnapshots -> {
            postAdapter.clearPosts();
            for (DocumentSnapshot feedSnapshot : feedSnapshots) {
                DocumentReference postReference = feedSnapshot.getDocumentReference("postReference");
                assert postReference != null;
                postReference.get().addOnSuccessListener(postSnapshot -> postAdapter.addPost(postSnapshot.toObject(Post.class)));
            }
        });
    }
}