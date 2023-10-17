package com.example.instapets.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.instapets.utilities.SharedPrefUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.instapets.R;
import com.example.instapets.models.User;

/**
 * this class represents the activity of settings
 */
public class SettingsActivity extends AppCompatActivity {
    LinearLayout settingsList;
    TextView logout;
    GoogleSignInClient googleSignInClient;
    DocumentReference userReference;
    private SharedPrefUtils prefUtils;
    LinearLayout linearLayout;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_settings);
        prefUtils = new SharedPrefUtils(this);

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        userReference = FirebaseFirestore.getInstance().collection("Users").document(prefUtils.get("email").replace(".",""));

        settingsList = findViewById(R.id.settings_list);
        logout = findViewById(R.id.setting_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prefUtils.setString("email","null");
                finishAffinity();
            }
        });
        settingsList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, EditInfoActivity.class));

            }
        });

        updateUserData();
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
    }

    private void updateUserData() {
        userReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = task.getResult().toObject(User.class);
                assert user != null;

            }
        });
    }





}