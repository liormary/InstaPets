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


    private void letcodebegin() {
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

                if (prefUtils.get("email") != null) {
                    if (prefUtils.get("email").equals("null")) {
                        Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + prefUtils.get("email").replace(".", ""));
                        try {
                            userReference.get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    User user = task.getResult().toObject(User.class);
                                    Intent intent;
                                    if (user != null) {
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
                } else {
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