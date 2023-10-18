package com.example.instapets.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.instapets.R;
import com.example.instapets.fragments.HomeFragment;
import com.example.instapets.fragments.ProfileFragment;
import com.example.instapets.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * this class represents the activity of navigation bar
 */

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    FirebaseUser user;
    String currentPage;

    // Overrides the behavior of the back button.
    // If the current page is Home, the default behavior is used, allowing the user to navigate back.
    // If the current page is not Home, the Home page is selected.
    @Override
    public void onBackPressed() {
        if (currentPage.equals("HOME")) {
            super.onBackPressed();
        } else {
            currentPage = "HOME";
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    //A helper method to replace the current fragment in the container with the specified fragment.
    void startFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).commit();
    }

    //This method is called when the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        user = FirebaseAuth.getInstance().getCurrentUser();

        String page = getIntent().getStringExtra("page");
        if (page != null) {
            if (page.equals("PROFILE")) {
                currentPage = "PROFILE";
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            }
        } else {
            currentPage = "HOME";
        }

        //bottom navigation changes fragment
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    currentPage = "HOME";
                    break;
                case R.id.nav_search:
                    currentPage = "SEARCH";
                    break;
                case R.id.nav_profile:
                    currentPage = "PROFILE";
                    break;
            }
            startFragment(currentPage);
            return true;
        });
        startFragment(currentPage);
    }

    // A method for creating and displaying the specified fragment based on the provided string
    public void startFragment(String fragment) {
        Fragment frag = null;
        switch (fragment) {
            case "PROFILE":
                frag = new ProfileFragment();
                currentPage = "PROFILE";
                break;
            case "SEARCH":
                frag = new SearchFragment();
                currentPage = "SEARCH";
                break;
            case "HOME":
                frag = new HomeFragment();
                currentPage = "HOME";
                break;
        }
        if (frag != null) startFragment(frag);
    }
}