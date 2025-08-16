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

        String language = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getString("language", "en");

        TextView welcomeMessage = findViewById(R.id.welcomeMessage);

        switch (language) {
            case "si":
                welcomeMessage.setText("SafeHaven වෙත සාදරයෙන් පිළිගනිමු!");
                break;
            case "ta":
                welcomeMessage.setText("SafeHaven-க்கு வரவேற்கிறோம்!");
                break;
            default:
                welcomeMessage.setText("Welcome to SafeHaven!");
                break;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

// Set selected item depending on the current activity
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Change this per activity

        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    if (!(this instanceof Home)) {
                        startActivity(new Intent(getApplicationContext(), Home.class));
                        overridePendingTransition(0, 0);
                    }
                    return true;
                case R.id.nav_guides:
                    if (!(this instanceof SurvivalGuides)) {
                        startActivity(new Intent(getApplicationContext(), SurvivalGuides.class));
                        overridePendingTransition(0, 0);
                    }
                    return true;
                case R.id.nav_recover:
                    if (!(this instanceof Recover)) {
                        startActivity(new Intent(getApplicationContext(), Recover.class));
                        overridePendingTransition(0, 0);
                    }
                    return true;
                case R.id.nav_contacts:
                    if (!(this instanceof Contacts)) {
                        startActivity(new Intent(getApplicationContext(), Contacts.class));
                        overridePendingTransition(0, 0);
                    }
                    return true;
            }
            return false;
        });

    }
}