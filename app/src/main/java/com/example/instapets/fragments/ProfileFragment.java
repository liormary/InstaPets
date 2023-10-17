package com.example.instapets.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.instapets.R;
import com.example.instapets.activities.SavedPostsActivity;
import com.example.instapets.activities.SettingsActivity;
import com.example.instapets.models.User;
import com.example.instapets.utilities.ProfilePageManager;

/**
 * this class represents the screen of the user profile
 */

public class ProfileFragment extends Fragment {
    public DocumentReference userReference;
    Toolbar toolbar;
    ImageView backgroundImageEditButton;
    FirebaseFirestore db;
    String user;
    ProfilePageManager profilePageManager;
    private SharedPrefUtils prefUtils;

    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Glide.with(this).load(uri).into(profilePageManager.backgroundImage);
                postImage(uri);
            }
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = new SharedPrefUtils(requireActivity());

        user = prefUtils.get("email").replace(".","").toString();
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("Users").document(user);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePageManager = new ProfilePageManager(view);

        backgroundImageEditButton = view.findViewById(R.id.btn_change_background_img);
        toolbar = view.findViewById(R.id.top_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_saved:
                    startActivity(new Intent(requireActivity(), SavedPostsActivity.class));
                    break;
                case R.id.nav_settings:
                    startActivity(new Intent(requireActivity(), SettingsActivity.class));
                    break;
            }
            return true;
        });

        fillUserData();
        readPosts();

        backgroundImageEditButton.setOnClickListener(v -> {
            selectImage();
        });

        return view;
    }

    private void postImage(Uri filePath) {
        if (filePath != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            ProgressDialog progressDialog = new ProgressDialog(requireActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String postId = prefUtils.get("email").replace(".","");
            StorageReference ref = storageReference.child("Background Pictures/" + postId);
            ref.putFile(filePath).addOnSuccessListener(snapshot -> {
                Toast.makeText(requireActivity(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                storageReference.child("Background Pictures").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("Users").document(postId).update("backgroundImageUrl", uri.toString()).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                    });
                }).addOnFailureListener(e -> progressDialog.dismiss());
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }).addOnProgressListener(taskSnapshot -> {
                double progress =
                        ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });
        }
    }

    void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    void fillUserData() {
        try{
            userReference.get().addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                assert user != null;
                profilePageManager.fillUserData(user);
            });
        }catch (Exception e){

        }

    }

    void readPosts() {
        profilePageManager.readPosts(userReference);
    }
}