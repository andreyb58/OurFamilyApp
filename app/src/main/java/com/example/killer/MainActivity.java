package com.example.killer;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.killer.auth.AuthManager;
import com.example.killer.fragments.CalendarFragment;
import com.example.killer.fragments.ChatFragment;
import com.example.killer.fragments.FamilyFragment;
import com.example.killer.fragments.ProfileFragment;
import com.example.killer.fragments.TasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authManager = AuthManager.getInstance(this);

        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Запуск с Календаря (первый в меню)
        loadFragment(new CalendarFragment());
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if      (id == R.id.nav_calendar) fragment = new CalendarFragment();
            else if (id == R.id.nav_tasks)    fragment = new TasksFragment();
            else if (id == R.id.nav_chat)     fragment = new ChatFragment();
            else if (id == R.id.nav_family)   fragment = new FamilyFragment();
            else if (id == R.id.nav_profile)  fragment = new ProfileFragment();

            if (fragment != null) { loadFragment(fragment); return true; }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
