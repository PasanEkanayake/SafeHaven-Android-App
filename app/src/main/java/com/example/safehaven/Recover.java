package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Recover extends AppCompatActivity {

    private LinearLayout guideFloods, guideLandslides, guideEarthquakes, guideFires, guideHurricanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_recover);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_recover) {
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_guides) {
                startActivity(new Intent(getApplicationContext(), SurvivalGuides.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_contacts) {
                startActivity(new Intent(getApplicationContext(), Contacts.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        guideFloods = findViewById(R.id.guideFloods);
        guideLandslides = findViewById(R.id.guideLandslides);
        guideEarthquakes = findViewById(R.id.guideEarthquakes);
        guideFires = findViewById(R.id.guideFires);
        guideHurricanes = findViewById(R.id.guideHurricanes);

        // Floods
        guideFloods.setOnClickListener(v -> openGuide(
                "Flood Aftermath Survival Guide: What to Do When the Waters Recede?",
                R.drawable.img_postguides_floods,
                "https://drive.google.com/uc?export=download&id=1F0SGvZWNkNVxNlk843L1oToCvbCW5NWK"
        ));

        // Landslides
        guideLandslides.setOnClickListener(v -> openGuide(
                "Landslide Aftermath Survival Guide: Steps to Take When the Ground Stops Moving",
                R.drawable.img_postguides_landslides,
                "https://drive.google.com/uc?export=download&id=1FJKvcoGBMokPE0SgwOWF-LZ6Eiqp8h4Z"
        ));

        guideEarthquakes.setOnClickListener(v -> openGuide(
                "Earthquake Aftermath Survival Guide: Steps to Take When the Shaking Stops",
                R.drawable.img_postguides_earthquakes,
                "https://drive.google.com/uc?export=download&id=1s9UVIPv-6v-ZAAQkGfNzLWzsSShydOR3"
        ));

        // Fires
        guideFires.setOnClickListener(v -> openGuide(
                "Wildfire Aftermath Survival Guide: Steps to Take When the Flames Have Passed",
                R.drawable.img_postguides_wildfires,
                "https://drive.google.com/uc?export=download&id=1cVgKCGoV7m3Z-E23YDN8W1-_Bvi09Vn-"
        ));

        // Tsunamis
        guideHurricanes.setOnClickListener(v -> openGuide(
                "Tsunami Aftermath Survival Guide: Steps to Take When the Waves Recede",
                R.drawable.img_postguides_tsunamis,
                "https://drive.google.com/uc?export=download&id=1jaN3wMknX5WYjCd3t_cZBztu7E8VNLz5"
        ));
    }

    private void openGuide(String title, int mainImage, String bodyImageUrl) {
        Intent intent = new Intent(this, GuideDetails.class);
        intent.putExtra("title", title);
        intent.putExtra("mainImage", mainImage);
        intent.putExtra("bodyImageUrl", bodyImageUrl);
        startActivity(intent);
    }
}
