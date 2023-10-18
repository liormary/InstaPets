package com.example.instapets.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.instapets.R;
import com.example.instapets.activities.AddInfoActivity;

/**
 * this class represents the screen of adding info after sign up
 */

public class DetailsFragment extends Fragment {
    EditText name, bio;
    AddInfoActivity parent;

    //The DetailsFragment constructor takes an AddInfoActivity as a parameter and stores it
    // in the parent variable.
    // This allows communication between the fragment and the activity.
    public DetailsFragment(AddInfoActivity parent) {
        this.parent = parent;
    }

    //
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        name = view.findViewById(R.id.et_name);
        bio = view.findViewById(R.id.et_bio);
        parent.headerText.setText("Add details");

        //TextWatcher listeners for both the name and bio EditText fields.
        // These listeners detect changes in the text and update the parent.data map with
        // the respective user details, "name" and "bio."
        //The afterTextChanged method is used to capture the updated text (name or bio)
        // entered by the user and store it in the parent.data map with the corresponding key ("name" or "bio").
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("name", s.toString());
            }
        });

        bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                parent.data.put("bio", s.toString());
            }
        });
        return view;
    }
}