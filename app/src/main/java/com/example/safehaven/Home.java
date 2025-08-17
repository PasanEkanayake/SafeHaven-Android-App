package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String language = getSharedPreferences("SafeHavenPrefs", MODE_PRIVATE)
                .getString("language", "English");

        TextView welcomeMessage = findViewById(R.id.welcomeMessage);

        switch (language) {
            case "Sinhala":
                welcomeMessage.setText("SafeHaven වෙත සාදරයෙන් පිළිගනිමු!");
                break;
            case "Tamil":
                welcomeMessage.setText("SafeHaven-க்கு வரவேற்கிறோம்!");
                break;
            default:
                welcomeMessage.setText("Welcome to SafeHaven!");
                break;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set selected item for the current activity
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Set listener for item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_guides) {
                startActivity(new Intent(getApplicationContext(), SurvivalGuides.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_recover) {
                startActivity(new Intent(getApplicationContext(), Recover.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_contacts) {
                startActivity(new Intent(getApplicationContext(), Contacts.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        findViewById(R.id.settings).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Settings.class));
            overridePendingTransition(0, 0);
        });

    }
}