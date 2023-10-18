package com.example.instapets.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.instapets.R;
import com.example.instapets.models.User;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * this class represents the activity of the animation of the entrance to the app
 */
public class StartActivity extends AppCompatActivity {
    ImageView logo;
    TextView appName;
    private SharedPrefUtils prefUtils;
    public static final String TAG = "com.example.instapets";

    //This method is called when the activity is created.
    //It initializes the UI elements, such as logo (ImageView) and appName (TextView).
    //It sets the SystemUiVisibility to fullscreen to hide the system UI elements.
    //It checks the user's dark mode preference and sets the app's dark mode accordingly.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_start);

        logo = findViewById(R.id.img_logo);
        appName = findViewById(R.id.txt_appname);
        prefUtils = new SharedPrefUtils(this);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", true);
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        letcodebegin();
    }

    //This method handles the animations and the logic for deciding whether
    // the user should be automatically logged in or directed to the login activity.
    // It starts two animations for the app logo and app name.
    // The appNameAnimation has an animation listener to detect when it ends.
    // If the user's email is found in the shared preferences and is not "null",
    // it attempts to perform an auto-login.
    // If there is a valid email, it checks if the user's details are available in Firebase.
    // If the user's details are found in Firebase and their registration is complete,
    // it directs the user to the main activity.
    // If the user's details are not found or their registration is incomplete,
    // it directs the user to the appropriate activity (either AddInfoActivity for completing
    // registration or MainActivity for a regular user).
    // If the user's email is not found or is "null" (indicating the need to log in manually),
    // it directs the user to the login activity.
    private void letcodebegin() {
        //preforms the entrance to the app!
        Animation logoAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_animation);
        Animation appNameAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.app_name_animation);
        logo.startAnimation(logoAnimation);
        appName.startAnimation(appNameAnimation);

        appNameAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                 // in case we have made back in time login with this machine,
                 // the app will try to preform an auto login
                if (prefUtils.get("email") != null) {//in case we had an email already in our context
                    if (prefUtils.get("email").equals("null")) {//not a real mail - therefor needs to return to the login activity
                        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        //we have a valid email and we are taking his details from firebase
                        DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + prefUtils.get("email").replace(".", ""));
                        try {
                            userReference.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    User user = task.getResult().toObject(User.class);
                                    Intent intent;
                                    if (user != null) {// checks if user haven't finish his registration
                                        if (user.getUsername().isEmpty()) {
                                            intent = new Intent(StartActivity.this, AddInfoActivity.class);
                                        } else {
                                            intent = new Intent(StartActivity.this, MainActivity.class);
                                        }
                                    } else {
                                        intent = new Intent(StartActivity.this, LoginActivity.class);
                                    }
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.e("signin", task.getException().getMessage());
                                }
                            });
                        } catch (Exception e) {
                            Log.d(TAG, "onAnimationEnd: " + e);
                            Toast.makeText(StartActivity.this, "something's wrong again", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {// needs to let the user to preform by himself the login
                    Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}