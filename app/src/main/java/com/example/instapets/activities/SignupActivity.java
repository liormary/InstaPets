package com.example.instapets.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.example.instapets.R;
import com.example.instapets.apiCalls.CommonApiCall;
import com.example.instapets.utilities.AppConfigs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * this class represents the activity of sign up to the app
 */

public class SignupActivity extends AppCompatActivity implements CommonApiCall.ResponseHandler {
    EditText email, password, confirmPassword;
    Button signupButton;
    FirebaseFirestore db;
    FirebaseAuth auth;
    ProgressDialog pd;
    private String url = AppConfigs.Sign_up;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    private CommonApiCall apiCall;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_signup_email);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        confirmPassword = findViewById(R.id.et_confirm_password);
        signupButton = findViewById(R.id.btn_signup);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        signupButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            String strConfirmPassword = confirmPassword.getText().toString();
            if (!strEmail.matches(emailPattern)) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (validatePassword(strPassword)) {
                if (validate(strEmail, strPassword, strConfirmPassword)) {
                    signupWithEmail(strEmail, strPassword);
                } else {
                    Toast.makeText(this, "Enter all details properly", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Password must contain one capital letter,one digit and special character", Toast.LENGTH_SHORT).show();
            }


        });
    }

    private boolean validate(String strEmail, String strPassword, String strConfirmPassword) {
        return !strEmail.isEmpty() && !strPassword.isEmpty() && strPassword.length() >= 6 && strPassword.equals(strConfirmPassword);
    }

    private void signupWithEmail(String strEmail, String strPassword) {
        Map<String, String> map = new HashMap<>();
        map.put("username", strEmail);
        map.put("password", strPassword);
        apiCall = new CommonApiCall(SignupActivity.this, url, map, "login");
        apiCall.Call();
    }

    //api call
    @Override
    public void onResponse(String response, String serviceType) {
        JSONObject reader = null;
        String jsonString = response;

// Remove the "connected" prefix
        int startIndex = jsonString.indexOf("{");
        String jsonSubstring = jsonString.substring(startIndex);
        try {
            JSONObject jsonObject = new JSONObject(jsonSubstring);
            String success = jsonObject.getString("success");
            String message = jsonObject.getString("message");
            if (success.equals("1")) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (Exception e) {

        }

    }

    @Override
    public void onError(VolleyError error, String serviceType) {
    }

    private boolean validatePassword(String password) {
        String passwordInput = password.toString().trim();

        if (!passwordInput.matches(".*[0-9].*")) {
            Toast.makeText(this, "Password should contain at least 1 digit", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordInput.matches(".*[a-z].*")) {
            Toast.makeText(this, "Password should contain at least 1 lower case letter", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordInput.matches(".*[A-Z].*")) {
            Toast.makeText(this, "Password should contain at least 1 upper case letter", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordInput.matches(".*[a-zA-Z].*")) {
            Toast.makeText(this, "Password should contain a letter", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordInput.matches(".{8,}")) {
            Toast.makeText(this, "Password should contain 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }
}