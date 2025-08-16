package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SurvivalGuides extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_survival_guides);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // highlight home by default

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    // Already on home, do nothing or restart
                    return true;
                case R.id.nav_guides:
                    startActivity(new Intent(getApplicationContext(), SurvivalGuides.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_recover:
                    startActivity(new Intent(getApplicationContext(), Recover.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_contacts:
                    startActivity(new Intent(getApplicationContext(), Contacts.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });

    }
}