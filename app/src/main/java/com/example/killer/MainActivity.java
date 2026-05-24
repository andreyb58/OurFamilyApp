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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthManager authManager;

    // Кэш фрагментов — чтобы не пересоздавать при переключении вкладок
    private final Map<Integer, Fragment> fragmentCache = new HashMap<>();

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

        loadFragment(R.id.nav_calendar);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            loadFragment(item.getItemId());
            return true;
        });
    }

    private Fragment getOrCreateFragment(int menuItemId) {
        if (!fragmentCache.containsKey(menuItemId)) {
            Fragment f;
            if      (menuItemId == R.id.nav_calendar) f = new CalendarFragment();
            else if (menuItemId == R.id.nav_tasks)    f = new TasksFragment();
            else if (menuItemId == R.id.nav_chat)     f = new ChatFragment();
            else if (menuItemId == R.id.nav_family)   f = new FamilyFragment();
            else                                       f = new ProfileFragment();
            fragmentCache.put(menuItemId, f);
        }
        return fragmentCache.get(menuItemId);
    }

    private void loadFragment(int menuItemId) {
        Fragment fragment = getOrCreateFragment(menuItemId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
