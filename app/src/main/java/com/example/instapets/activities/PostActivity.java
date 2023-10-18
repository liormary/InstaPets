package com.example.instapets.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.instapets.R;
import com.example.instapets.models.Post;
import com.example.instapets.models.User;
import com.example.instapets.fragments.SelectSourceDialogFragment;
import com.example.instapets.utilities.DateFormatter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * this class represents the activity of upload post to the app
 */
public class PostActivity extends AppCompatActivity {
    ImageView imageView;
    TextView caption;
    Uri filePath = null;
    StorageReference storageReference;
    FirebaseFirestore db;
    FirebaseUser user;
    String currentPhotoPath;
    private SharedPrefUtils prefUtils;

    //in case of uploading a photo from the phone gallery
    ActivityResultLauncher<Intent> fromGalleryResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            // also important to check if the photo from the gallery isn't null unlike the
            // camera that creates it and makes sure its isn't null
            if (data != null && data.getData() != null) {
                filePath = data.getData();
                showActivity(true);
                Glide.with(this).load(filePath).into(imageView);
            }
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            startMainActivity();
        }
    });


    //in case of uploading a photo from the phone camera
    ActivityResultLauncher<Intent> fromCameraResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            showActivity(true);
            Glide.with(this).load(filePath).into(imageView);
        } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
            startMainActivity();
        }
    });

    //This method is called when the activity is created.
    //It initializes various UI elements, such as an ImageView for displaying the selected image,
    // a TextView for adding a caption, and buttons for posting or canceling.
    //It checks the type of content the user wants to post (either a picture or text)
    // based on the value passed through an intent.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_post);
        prefUtils = new SharedPrefUtils(this);

        imageView = findViewById(R.id.post_img);
        caption = findViewById(R.id.et_caption);
        ImageView closeButton = findViewById(R.id.btn_close);
        ImageView postButton = findViewById(R.id.btn_post);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        //the buttons of posting or cancel the posting
        closeButton.setOnClickListener(view -> {
            startMainActivity();
        });
        postButton.setOnClickListener(view -> {
            closeKeyboard();
            publishPost();
        });
        //

        String type = getIntent().getStringExtra("type");

        if (type.equals("picture")) {
            showDialog();
            showActivity(false);
        } else {
            TextInputLayout captionLayout = findViewById(R.id.layout_caption);
            captionLayout.setHint("Enter text content");
        }
    }

    //This method displays a select source dialog that allows the user to select the source of the content,
    // such as whether to choose an image from the gallery or capture a photo using the camera.
    void showDialog() {
        SelectSourceDialogFragment newFragment = SelectSourceDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }


    private File createImageFile() throws IOException {
        String timeStamp = DateFormatter.getCurrentTime();
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // This method is used to close the on-screen keyboard,
    // ensuring that the keyboard is hidden when the user finishes typing a caption for the post.
    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //This method is called when the user chooses to take a photo using the device's camera.
    //It creates a file to save the captured image and then launches the camera app to capture a photo.
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                filePath = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                fromCameraResultLauncher.launch(Intent.createChooser(takePictureIntent, "Select Picture"));
            }
        }
    }

    //This method is called when the user decides to publish a post.
    //If an image (filePath) is selected, it uploads the image to Firebase Storage and saves the download
    // URL to Firebase.
    //If the user only wants to create a text post, it creates a post object without an image URL.
    //It updates user-specific documents in the database to maintain information about posts and feeds.
    //Finally, it calls startMainActivity to navigate back to the main activity.
    private void publishPost() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);

            //starts the proses of uploading the post
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            //saves the upload time
            String postId = DateFormatter.getCurrentTime();
            StorageReference ref = storageReference.child("Posts/" + postId);

            //in case of success to uploading
            ref.putFile(filePath).addOnSuccessListener(snapshot -> {
                Toast.makeText(this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();

                storageReference.child("Posts").child(postId).getDownloadUrl().addOnSuccessListener(uri -> {
                    Post post = new Post(prefUtils.get("email").replace(".", ""), postId, uri.toString(), caption.getText().toString());
                    DocumentReference postRef = db.collection("Posts").document(postId);

                    postRef.set(post).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        startMainActivity();
                    });
                    updateUserPostsAndFeed(postRef);
                });

                //in case of success to uploading
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();

                //in case of delay for any reason - we shall let it wait for a while and give it a chance...
            }).addOnProgressListener(taskSnapshot -> {
                double progress =
                        ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            });
        } else {
            String postId = DateFormatter.getCurrentTime();
            Post post = new Post(prefUtils.get("email").replace(".", ""), postId, "", "");
            post.setKitt(caption.getText().toString());
            DocumentReference postRef = db.collection("Posts").document(postId);
            postRef.set(post).addOnCompleteListener(task -> {
                startMainActivity();
            });
            updateUserPostsAndFeed(postRef);
        }
    }

    // This method updates the user's posts and feed in the Firebase database.
    // It adds the post reference to the user's "posts" array and sets the "visited" status to
    // false in the "feed" collection.
    // It also updates the "feed" collection for followers of the user who created the post.
    void updateUserPostsAndFeed(DocumentReference postReference) {
        DocumentReference userReference = db.collection("Users").document(prefUtils.get("email").replace(".", ""));
        userReference.update("posts", FieldValue.arrayUnion(postReference));
        Map<String, Object> map = new HashMap<>();
        map.put("postReference", postReference);
        map.put("visited", false);
        userReference.collection("feed").document(postReference.getId()).set(map);
        userReference.get().addOnSuccessListener(userSnapshot -> {
            User me = userSnapshot.toObject(User.class);
            assert me != null;
            for (DocumentReference follower : me.getFollowers()) {
                follower.collection("feed").document(postReference.getId()).set(map);
            }
        });
    }

    //This method returns to the main activity of the app.
    // It's called after the user has successfully posted content.
    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //This method is used when the user wants to select an image from the gallery.
    // It launches the gallery to choose an image.
    private void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        fromGalleryResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    //This method shows or hides the main content of the activity based on the value of the set parameter.
    // It's used to control the visibility of UI elements during the content creation process.
    private void showActivity(boolean set) {
        if (set) findViewById(R.id.post_parent).setVisibility(View.VISIBLE);
        else findViewById(R.id.post_parent).setVisibility(View.INVISIBLE);
    }

    //This method is called when the user selects to capture a photo
    // from the camera in the source selection dialog.
    public void selectFromCamera() {
        takePicture();
    }

    //These methods are called when the user selects to pick an image
    // from the gallery in the source selection dialog.
    public void selectFromGallery() {
        selectPicture();
    }
}