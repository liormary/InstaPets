package com.example.instapets.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.instapets.R;
import com.example.instapets.activities.AddInfoActivity;
import com.example.instapets.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
/**
 * this class represents the screen of choosing a username after sign up
 */
public class UsernameFragment extends Fragment {

    EditText username;
    AddInfoActivity parent;
    List<String> userNames;
    boolean valid; // A boolean flag used to validate the username entered by the user.

    //The constructor takes an AddInfoActivity as a parameter.
    // This allows the fragment to communicate with the activity.
    public UsernameFragment(AddInfoActivity parent) {
        this.parent = parent;
    }

    // This method is responsible for inflating the fragment's layout and configuring its views.
    //Initializing the userNames list by fetching usernames from the Firebase database.
    //Disabling the "Next" button in the parent activity by default.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_username, container, false);
        username = view.findViewById(R.id.et_username);
        parent.headerText.setText("Choose a username");
        userNames = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(userSnapshots -> {
            for (DocumentSnapshot userSnapshot : userSnapshots) {
                User user = userSnapshot.toObject(User.class);
                userNames.add(user.getUsername());
            }
        });
        parent.nextButton.setClickable(false);

        //Adding a TextWatcher to the EditText widget to listen for text changes.
        //Validating the entered username based on the following conditions:
        //The username should contain only word characters (letters, digits, or underscores).
        //The username should be between 3 and 15 characters in length.
        //Checking whether the entered username already exists in the userNames list.
        //If the entered username is invalid or already taken,
        // it displays a message and keeps the "Next" button disabled.
        // Otherwise, it enables the "Next" button.
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                valid = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("username", s.toString());
                if (!s.toString().matches("^\\w+$") || s.length() < 3 || s.length() > 15) {
                    valid = false;
                }
                for (String name : userNames) {
                    if (name.equals(s.toString())) {
                        valid = false;
                        Toast.makeText(requireActivity(), "username taken or invalid!!", Toast.LENGTH_SHORT).show();

                        break;
                    }
                }
                if (!valid) {
                    username.setTextColor(getResources().getColor(R.color.like));
                    parent.nextButton.setClickable(false);
                } else {
                    username.setTextColor(getResources().getColor(R.color.inverted));
                    parent.nextButton.setClickable(true);
                }
            }
        });
        return view;
    }
}