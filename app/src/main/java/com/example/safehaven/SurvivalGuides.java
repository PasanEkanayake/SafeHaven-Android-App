package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SurvivalGuides extends AppCompatActivity {

    private LinearLayout guideFloods, guideLandslides;

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

        guideFloods = findViewById(R.id.guideFloods);

        guideFloods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SurvivalGuides.this, GuideFloods.class);
                startActivity(intent);
            }
        });

        guideLandslides = findViewById(R.id.guideLandslides);

        guideLandslides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SurvivalGuides.this, GuideLandslide.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set selected item for the current activity
        bottomNavigationView.setSelectedItemId(R.id.nav_guides);

        // Set listener for item selection
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_guides) {
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
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