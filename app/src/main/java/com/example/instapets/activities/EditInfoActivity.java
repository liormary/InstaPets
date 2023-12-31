package com.example.instapets.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.instapets.R;
import com.example.instapets.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * this class represents the activity of edit user info
 */
public class EditInfoActivity extends AppCompatActivity {
    User user;
    DocumentReference userReference;
    EditText name, username, bio;
    ImageView profileImage;
    Uri profileImageUri = null;
    HashMap<String, Object> data = new HashMap<>();
    ImageView saveInfoButton, closeButton, profileImageEditButton;
    boolean valid;
    List<String> userNames;
    private SharedPrefUtils prefUtils;

    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
                profileImageUri = data.getData();
                Glide.with(this).load(profileImageUri).into(profileImage);
            }
        }
    });

    // This method is called when the activity is created. Key functionality includes:
    //Initializing views and data structures, registering click listeners for the "Change Profile Image",
    //"Save Info," and "Close" buttons, filling the user's data from Firebase to populate the UI elements,
    //monitoring user input for the username field in real-time to validate its format and check for uniqueness,
    //updating the data map with edited information (name, username, bio) and calling the updateData()
    // or updateImageAndData() methods to save the updated information to Firebase Storage.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_edit_info);
        prefUtils = new SharedPrefUtils(this);

        profileImage = findViewById(R.id.img_profile);
        name = findViewById(R.id.et_name);
        username = findViewById(R.id.et_username);
        bio = findViewById(R.id.et_bio);
        saveInfoButton = findViewById(R.id.btn_save_info);
        closeButton = findViewById(R.id.btn_close);
        profileImageEditButton = findViewById(R.id.btn_change_profile_img);
        fillUserData();

        profileImageEditButton.setOnClickListener(view -> selectImage());
        closeButton.setOnClickListener(view -> finish());

        //holds on all the user's followers in an arrayList
        userNames = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userSnapshot : userSnapshots) {
                User user = userSnapshot.toObject(User.class);
                userNames.add(user.getUsername());
            }
        });


         // monitors the text input in real-time and performs certain actions based on the input
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //before any changes have made
                valid = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //checks if the change is valid!
                if (!s.toString().matches("^\\w+$") || s.length() < 3 || s.length() > 15) {
                    valid = false;
                }
                //checks if its not exist already
                for (String name : userNames) {
                    if (!name.equals(user.getUsername()) && name.equals(s.toString())) {
                        valid = false;
                        break;
                    }
                }
                //saves the changes into firebase
                if (!valid) {
                    username.setTextColor(getResources().getColor(R.color.like));
                    saveInfoButton.setClickable(false);
                } else {
                    username.setTextColor(getResources().getColor(R.color.inverted));
                    saveInfoButton.setClickable(true);
                }
            }
        });

        //the action of the saving button
        saveInfoButton.setOnClickListener(view -> {
            data.put("name", name.getText().toString());
            data.put("username", username.getText().toString());
            data.put("bio", bio.getText().toString());
            if (profileImageUri != null)
                updateImageAndData();
            else
                updateData();
        });
    }

    private void updateData() {
        userReference = FirebaseFirestore.getInstance().collection("Users")
                .document(prefUtils.get("email").replace(".",""));
        userReference.update(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intentMain = new Intent(this, MainActivity.class);
                intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentMain.putExtra("page", "PROFILE");
                startActivity(intentMain);
                finish();
            }
        });
    }

    //This method updates the user's data from the firebase
    // It uses the DocumentReference to update the user's document with the modified data (name, username, bio).
    private void fillUserData() {
        userReference = FirebaseFirestore.getInstance().collection("Users")
                .document(prefUtils.get("email").replace(".",""));
        userReference.get().addOnSuccessListener(snapshot -> {
            user = snapshot.toObject(User.class);
            assert user != null;
            username.setText(user.getUsername());
            name.setText(user.getName());
            bio.setText(user.getBio());
            Glide.with(this).load(user.getProfileImageUrl()).into(profileImage);
        });
    }

    //This method opens the image gallery for the user to select a new profile image.
    // It utilizes an activity result launcher (myActivityResultLauncher) for the image selection.
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*").setAction(Intent.ACTION_GET_CONTENT);
        myActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    //This method updates the user's profile image and other data in Firebase Storage.
    // It first uploads the new profile image to Firebase Storage and then updates
    // the user's Firebase document with the image URL.
    // A progress dialog is used to inform the user about the image upload progress.
    private void updateImageAndData() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        ProgressDialog progressDialog = new ProgressDialog(this);

        //starts the proses
        progressDialog.setTitle("Saving Profile...");
        progressDialog.show();
        String postId = user.getId();
        StorageReference ref = storageReference.child("Profile Pictures/" + postId);

        //starts to upload it
        ref.putFile(profileImageUri).addOnSuccessListener(snapshot -> {
            Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
            //in case of succsess - saves the new update into the firebase
            storageReference.child("Profile Pictures/").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                data.put("profileImageUrl", uri.toString());
                updateData();
                progressDialog.dismiss();

                //in case of a failure in uploading it
            }).addOnFailureListener(e -> progressDialog.dismiss());
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();

            //in case of delay for any reason - we shall let it wait for a while and give it a chance...
        }).addOnProgressListener(taskSnapshot -> {
            double progress =
                    ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        });
    }
}