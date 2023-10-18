package com.example.instapets.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.instapets.R;
import com.example.instapets.activities.AddInfoActivity;

/**
 * this class represents the screen of choosing profile image after sign up
 */
public class ProfileImageFragment extends Fragment {
    Button chooseButton;
    ImageView profileImage;
    AddInfoActivity parent;

    // This is an ActivityResultLauncher for capturing the result of an activity started using an Intent.
    // In this case, it's used to start an image selection activity and handle the selected image.
    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                Glide.with(this).load(uri).into(profileImage);
                parent.profileImageUri = uri;
            }
        }
    });

    //Constructor for initializes the fragment with a reference to the parent activity.
    public ProfileImageFragment(AddInfoActivity parent) {
        this.parent = parent;
    }

    //This method is called when the fragment is created.
    //The chooseButton and profileImage views are initialized from the layout.
    //The header text in the parent activity is set to "Choose a profile image."
    //The default profile image is loaded into the profileImage using Glide.
    //A click listener for the chooseButton is set to trigger the image selection process.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_image, container, false);

        chooseButton = view.findViewById(R.id.btn_choose);
        profileImage = view.findViewById(R.id.img_profile);

        parent.headerText.setText("Choose a profile image");
        Glide.with(requireContext()).load(getResources().getString(R.string.default_profile_img_url)).into(profileImage);
        chooseButton.setOnClickListener(v -> {
            selectImage();
        });
        return view;
    }

    //This method is called when the user clicks the "Choose" button.
    // It creates an intent to pick an image from the device's gallery
    // and uses the myActivityResultLauncher to start the image selection activity.
    // After the user selects an image, the onActivityResult callback defined
    // within myActivityResultLauncher handles the result.
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }
}