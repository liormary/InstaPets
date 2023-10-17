package com.example.instapets.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instapets.R;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.instapets.fragments.DetailsFragment;
import com.example.instapets.fragments.ProfileImageFragment;
import com.example.instapets.fragments.UsernameFragment;

import java.util.HashMap;

/**
 * this class represent the activity of the info
 * initialization when the user has been just created
 */
public class AddInfoActivity extends AppCompatActivity {
    public enum Fragments {USERNAME, PROFILE_IMAGE, DETAILS}
    public ImageView nextButton;
    public HashMap<String, Object> data;
    public Uri profileImageUri = null;

    public TextView headerText;
    Fragments current;
    DocumentReference userReference;
    private SharedPrefUtils prefUtils;

    //when we are just entered to this activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_add_info);
        prefUtils = new SharedPrefUtils(this);
        userReference = FirebaseFirestore.getInstance().collection("Users").document(prefUtils.get("email").replace(".",""));
        nextButton = findViewById(R.id.btn_next);
        headerText = findViewById(R.id.txt_header);

        data = new HashMap<>();
        current = Fragments.USERNAME;
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new UsernameFragment(this)).commit();
        nextButton.setOnClickListener(view -> {
            nextFragment();
        });
    }

    //this method moves between fragments when updating details in our add info activity!
    public void nextFragment() {
        if (current == Fragments.USERNAME) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new ProfileImageFragment(this)).commit();
            current = Fragments.PROFILE_IMAGE;
        } else if (current == Fragments.PROFILE_IMAGE) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new DetailsFragment(this)).commit();
            current = Fragments.DETAILS;
        } else if (current == Fragments.DETAILS) {
            if (profileImageUri != null)
                updateDataWithImage();
            else
                updateData();
        }
    }

    void updateData() {
        userReference.update(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    //this method is for storing the chosen image to the FireBase
    void updateDataWithImage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();//bringing the the FireBase data obj for updates
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving Profile...");
        progressDialog.show();

        String postId = prefUtils.get("email").replace(".","");
        StorageReference ref = storageReference.child("Profile Pictures/" + postId);

        ref.putFile(profileImageUri).addOnCompleteListener(task0 -> {
            if (task0.isSuccessful()) {
                storageReference.child("Profile Pictures/").child(postId).getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        data.put("profileImageUrl", task.getResult().toString());
                        updateData();
                    }
                    progressDialog.dismiss();
                });
            } else {
                Toast.makeText(this, "Failed " + task0.getException().getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnProgressListener(taskSnapshot -> {
            double progress = ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
            progressDialog.setMessage("Saving " + (int) progress + "%");
        });
    }


}