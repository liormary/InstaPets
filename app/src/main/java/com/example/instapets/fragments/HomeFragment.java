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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts);
        prefUtils = new SharedPrefUtils(requireActivity());
        toolbar = view.findViewById(R.id.top_menu);
        //to shrink "new post" button
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

        floatingActionButton = view.findViewById(R.id.extendedFloatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PromptDialog.getInstance(-1, dl).show(requireActivity().getSupportFragmentManager(), "");

            }
        });
        toolbar.setOnMenuItemClickListener(item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.nav_post_image:
                    intent = new Intent(requireActivity(), PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("type", "picture");
                    startActivity(intent);
                    break;
                case R.id.nav_post_text:
                    intent = new Intent(requireActivity(), PostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("type", "text");
                    startActivity(intent);
                    break;
                case R.id.nav_chat:
                    intent = new Intent(requireActivity(), ChatHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
            }
            return true;
        });

        postAdapter = new PostAdapter(requireContext());
        recyclerViewPosts.setHasFixedSize(true);
        recyclerViewPosts.setAdapter(postAdapter);
        readPosts();
        return view;
    }

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