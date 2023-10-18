package com.example.instapets.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.instapets.R;
import com.example.instapets.adapters.ProfileAdapter;
import com.example.instapets.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * this class represents the search screen
 */
public class SearchFragment extends Fragment {
    SearchView searchbar;
    RecyclerView recyclerViewProfiles;
    TextView messageText; // displays a message when there are no search results to show.
    ProfileAdapter profileAdapter;
    List<User> profiles, allProfiles; //Two lists, profiles that fit to the search and allProfiles.

    ImageView closeButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        recyclerViewProfiles = view.findViewById(R.id.recyclerview_profiles);
        searchbar = view.findViewById(R.id.et_search);
        messageText = view.findViewById(R.id.txt_message);
        closeButton = view.findViewById(R.id.btn_close);
        profiles = new ArrayList<>();
        allProfiles = new ArrayList<>();
        profileAdapter = new ProfileAdapter(requireContext(), profiles, "PROFILE");
        recyclerViewProfiles.setHasFixedSize(true);
        recyclerViewProfiles.setAdapter(profileAdapter);

        //The readProfiles() method is called to fetch all available profiles.
        readProfiles();

        //A listener is added to the searchbar to filter profiles as the user types in the search query.
        searchbar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
        return view;
    }

    //This method filters profiles based on the search query.
    // It compares the search query to the names and usernames of users and updates the profiles
    // list accordingly.
    // It also handles the visibility of the "No results" message.
    private void filter(String text) {
        profiles.clear();
        messageText.setVisibility(View.GONE);
        if (text.length() == 0) return;
        for (User user : allProfiles) {
            if (user.getName().toLowerCase().contains(text.toLowerCase()) || user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                profiles.add(user);
            }
        }
        if (profiles.isEmpty()) {
            messageText.setVisibility(View.VISIBLE);
        }
        profileAdapter.filterList(profiles);
    }

    //This method reads all user profiles from the Firebase database and populates the
    // allProfiles list with this data.
    private void readProfiles() {
        CollectionReference userReference = FirebaseFirestore.getInstance().collection("Users");
        userReference.get().addOnSuccessListener(usersSnapshots -> {
            allProfiles.clear();
            for (DocumentSnapshot userSnapshot : usersSnapshots) {
                User user = userSnapshot.toObject(User.class);
                allProfiles.add(user);
            }
        });
    }
}