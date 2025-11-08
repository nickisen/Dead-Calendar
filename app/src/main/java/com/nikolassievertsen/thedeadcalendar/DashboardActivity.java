package com.nikolassievertsen.thedeadcalendar;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private Bundle fragmentDataBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ----- 1. Daten aus der MainActivity abrufen und bündeln -----
        Intent intent = getIntent();
        fragmentDataBundle = intent.getExtras(); // Holt alle Extras auf einmal

        // ----- 2. Bottom Navigation einrichten -----
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_timer) {
                selectedFragment = new TimerFragment();
            } else if (itemId == R.id.navigation_calendar) {
                selectedFragment = new CalendarFragment();
            }

            if (selectedFragment != null) {
                // Daten-Bundle an das Fragment übergeben
                selectedFragment.setArguments(fragmentDataBundle);
                // Fragment laden
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // ----- 3. Standard-Fragment (Timer) beim Start laden -----
        if (savedInstanceState == null) {
            Fragment initialFragment = new TimerFragment();
            initialFragment.setArguments(fragmentDataBundle);
            loadFragment(initialFragment);
            // Sicherstellen, dass der richtige Tab ausgewählt ist
            bottomNav.setSelectedItemId(R.id.navigation_timer);
        }
    }

    /**
     * Tauscht das aktuelle Fragment im FrameLayout aus.
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}