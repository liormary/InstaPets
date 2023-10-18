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

    // This is an ActivityResultLauncher for capturing the result of an activity started using an Intent.
    // In this case, it's used to start an image selection activity and handle the selected image.
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

    //This method is called when the fragment is first created.
    //It initializes various variables and references, such as prefUtils (used for managing shared preferences),
    // the Firebase database reference (userReference), and the user's email address.
    //The userReference points to the Firebase document representing the current user's profile.
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = new SharedPrefUtils(requireActivity());

        user = prefUtils.get("email").replace(".","").toString();
        db = FirebaseFirestore.getInstance();
        userReference = db.collection("Users").document(user);
    }

    //This method is part of the Android Fragment lifecycle and is called
    // when the fragment needs to create its user interface (UI).
    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profilePageManager = new ProfilePageManager(view);
        backgroundImageEditButton = view.findViewById(R.id.btn_change_background_img);
        toolbar = view.findViewById(R.id.top_menu);

        //Sets up the toolbar's menu items (nav_saved and nav_settings)
        // and defines actions to perform when these menu items are clicked.
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

        //Calls fillUserData to retrieve and display user profile data.
        fillUserData();

        //Calls readPosts to retrieve and display user-related posts.
        readPosts();

        //Change Background Image button to allow the user to select and upload a new background image.
        backgroundImageEditButton.setOnClickListener(v -> {
            selectImage();
        });

        return view;
    }

    //This method is used to upload a selected background image to Firebase Storage.
    //It takes a filePath parameter, representing the URI of the selected image.
    //It uploads the image to the Firebase Storage, showing a progress dialog during the upload.
    //Upon successful upload, it retrieves the download URL of the image and updates the user's
    // Firebase document with the new background image URL.
    //It also dismisses the progress dialog.
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

    //This method is responsible for allowing the user to select a new background image.
    //It creates an intent for picking an image from the device's gallery and launches an
    // activity to handle the selection.
    //The myActivityResultLauncher handles the result of the image selection,
    // calling postImage to upload the selected image.
    void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    //This method fetches user profile data from Firebase using userReference.
    //It retrieves the user's profile information (username, bio, profile image URL)
    // and updates the UI to display this information using profilePageManager.
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

    //This method is used to retrieve posts associated with the user's profile.
    //It invokes the readPosts method of the profilePageManager to fetch and display posts.
    void readPosts() {
        profilePageManager.readPosts(userReference);
    }
}