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


    //This method is called when the activity is created.
    //It initializes various UI elements, such as email, password, and confirmPassword EditText fields,
    // and the signupButton button. It sets up an OnClickListener for the signupButton.
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

        //when pressing the signup Button
        signupButton.setOnClickListener(view -> {
            String strEmail = email.getText().toString();
            String strPassword = password.getText().toString();
            String strConfirmPassword = confirmPassword.getText().toString();

            if (!strEmail.matches(emailPattern)) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (validatePassword(strPassword)) {
                // in case all parameters are valid, we shall login
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

    //This method checks if the provided email, password, and confirmPassword are valid.
    // It ensures that the email is not empty, the password is at least 6 characters long,
    // and the password and confirmPassword match.
    // If these conditions are met, the method returns true, indicating that the input is valid.
    private boolean validate(String strEmail, String strPassword, String strConfirmPassword) {
        return !strEmail.isEmpty() && !strPassword.isEmpty() && strPassword.length() >= 6 && strPassword.equals(strConfirmPassword);
    }

    //This method is called when the user clicks the "signupButton."
    //It takes the email and password provided by the user as parameters and makes an API call.
    //In this case, the CommonApiCall class is used to send a POST request to the specified URL
    // with the user's email and password.
    //The API call is asynchronous, and when it completes, it invokes the onResponse method.
    private void signupWithEmail(String strEmail, String strPassword) {
        Map<String, String> map = new HashMap<>();
        map.put("username", strEmail);
        map.put("password", strPassword);
        apiCall = new CommonApiCall(SignupActivity.this, url, map, "login");
        apiCall.Call();
    }

    //This method handles the response received from the API call.
    // It extracts the response data and checks if the registration was successful based on
    // the "success" field in the JSON response.
    // If registration is successful, it displays a toast message and navigates the user to the login page.
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
        }catch(Exception e){
        }
    }

    //This method is called when an error occurs during the API call.
    @Override
    public void onError(VolleyError error, String serviceType) {
    }

    //This method validates the password to ensure it meets certain conditions:
    //Contains at least one digit.
    //Contains at least one lowercase letter.
    //Contains at least one uppercase letter.
    //Contains at least one letter (uppercase or lowercase).
    //Is at least 8 characters long.
    //If the password meets these conditions, the method returns true, indicating that the password is valid.
    private boolean validatePassword(String password) {
        String passwordInput = password.toString().trim();
        if (!passwordInput.matches(".[0-9].")) {
            Toast.makeText(this, "Password should contain at least 1 digit", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordInput.matches(".[a-z].")) {
            Toast.makeText(this, "Password should contain at least 1 lower case letter", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordInput.matches(".[A-Z].")) {
            Toast.makeText(this, "Password should contain at least 1 upper case letter", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordInput.matches(".[a-zA-Z].")) {
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