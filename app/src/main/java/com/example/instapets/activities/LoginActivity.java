package com.example.instapets.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.example.instapets.R;
import com.example.instapets.apiCalls.CommonApiCall;
import com.example.instapets.models.User;
import com.example.instapets.utilities.AppConfigs;
import com.example.instapets.utilities.SharedPrefUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * this class represents the activity of login to the app
 */
public class LoginActivity extends AppCompatActivity implements CommonApiCall.ResponseHandler {
    EditText email, password;
    Button loginButton;
    TextView signupText;
    FirebaseAuth auth;
    private String url = AppConfigs.Login;
    private CommonApiCall apiCall;
    CollectionReference usersReference;
    private SharedPrefUtils prefUtils;

    //This method is called when the activity is created.
    // It initializes the UI elements, such as email, password, loginButton and signupText.
    // It sets click listeners on the login button and the signup text,
    // allowing users to either log in or navigate to the signup screen.
    // It also initializes the SharedPrefUtils and Firebase authentication.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        loginButton = findViewById(R.id.btn_login);
        signupText = findViewById(R.id.txt_signup);
        prefUtils = new SharedPrefUtils(this);

        auth = FirebaseAuth.getInstance();
        usersReference = FirebaseFirestore.getInstance().collection("Users");
        loginButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            login(strEmail, strPassword);
        });

        signupText.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

    }

    // This method is called when the login button is clicked.
    // It performs basic validation checks and then makes an API call for login.
    // The API call is performed using the CommonApiCall class.
    void login(String strEmail, String strPassword) {
        if (strEmail.isEmpty() || strPassword.isEmpty()) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> map = new HashMap<>();
        map.put("username", strEmail);
        map.put("password", strPassword);
        apiCall = new CommonApiCall(LoginActivity.this, url, map, "login");
        apiCall.Call();
    }

    // After successful login, this method is called to check if the user has already created a profile.
    // It updates the onesignalPlayerId field to an empty string in the user's Firestore document.
    // It then checks if the user's Firestore document exists.
    // If the user document doesn't exist, it creates a new user document with default profile
    // information and navigates to the AddInfoActivity for the user to complete their registration.
    // If the user document exists but doesn't have a username (indicating incomplete registration),
    // it navigates to the AddInfoActivity.
    // If the user document exists and has a username, it navigates to the MainActivity.
    private void doValidUserCheck() {
        DocumentReference userReference = FirebaseFirestore.getInstance().document("Users/" + prefUtils.get("email").replace(".", ""));
        userReference.update("onesignalPlayerId", "");
        userReference.get().addOnCompleteListener(task0 -> {
            if (task0.isSuccessful()) {
                User user = task0.getResult().toObject(User.class);
                if (user == null) {
                    user = new User(prefUtils.get("email").replace(".", ""), "", "", prefUtils.get("email"), getResources().getString(R.string.default_profile_img_url), getResources().getString(R.string.default_background_img_url));
                    userReference.set(user).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                        } else {
                            Toast.makeText(LoginActivity.this, task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    startAddInfoActivity();
                } else {
                    if (user.getUsername().isEmpty()) {
                        startAddInfoActivity();
                    } else {
                        startMainActivity();
                    }
                }
            } else {
                Toast.makeText(this, task0.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // This method is responsible for navigating to AddInfoActivity.
    private void startAddInfoActivity() {
        Intent intent = new Intent(LoginActivity.this, AddInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // This method is responsible for navigating to MainActivity.
    void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //This methods is part of the CommonApiCall.ResponseHandler interface and are used to handle API responses.
    //In this method the app processes the response data, extracts the login information,
    // and checks if the login was successful.
    // If the login is successful, it calls the doValidUserCheck method to determine the next steps.
    @Override
    public void onResponse(String response, String serviceType) {
        String jsonString = response;
        int startIndex = jsonString.indexOf("{");
        String jsonSubstring = jsonString.substring(startIndex);
        try {
            JSONObject jsonObject = new JSONObject(jsonSubstring);

            // Extract "login" array
            JSONArray loginArray = jsonObject.getJSONArray("login");
            for (int i = 0; i < loginArray.length(); i++) {
                JSONObject loginObject = loginArray.getJSONObject(i);
                String userid = loginObject.getString("userid");
                String username = loginObject.getString("username");
                prefUtils.setString("email", username);
                // Use the data from the "login" array
                System.out.println("User ID: " + userid);
                System.out.println("Username: " + username);
            }

            // Extract other values
            String success = jsonObject.getString("success");
            String message = jsonObject.getString("message");
            Toast.makeText(this, "" + message, Toast.LENGTH_SHORT).show();
            if (success.equals("1")) {
                doValidUserCheck();

            }

            // Use the other data as needed
            System.out.println("Success: " + success);
            System.out.println("Message: " + message);
        } catch (JSONException e) {
            e.printStackTrace();
            // Handle the parsing error here
        }

    }

    @Override
    public void onError(VolleyError error, String serviceType) {

    }
}