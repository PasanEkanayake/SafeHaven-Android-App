package com.example.safehaven;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Recover extends AppCompatActivity {

    private LinearLayout guideFloods, guideLandslides, guideEarthquakes, guideFires, guideHurricanes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set selected item for the current activity
        bottomNavigationView.setSelectedItemId(R.id.nav_recover);

        // Set listener for item selection
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

        guideFloods.setOnClickListener(v ->
                showArticle("Flood Safety Guide", R.drawable.img_postguides_floods, R.drawable.img_postguides_guide_floods)
        );

        guideLandslides.setOnClickListener(v ->
                showArticle("Landslide Safety Guide", R.drawable.img_postguides_landslides, R.drawable.img_postguides_guide_landslides)
        );

        guideEarthquakes.setOnClickListener(v ->
                showArticle("Earthquake Safety Guide", R.drawable.img_postguides_earthquakes, R.drawable.img_postguides_guide_earthquakes)
        );

        guideFires.setOnClickListener(v ->
                showArticle("Fire Safety Guide", R.drawable.img_postguides_wildfires, R.drawable.img_postguides_guide_wildfires)
        );

        guideHurricanes.setOnClickListener(v ->
                showArticle("Hurricane Safety Guide", R.drawable.img_postguides_tsunamis, R.drawable.img_postguides_guide_tsunamis)
        );
    }

    private void showArticle(String title, int mainImage, int bodyImage) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_article);

        TextView articleTitle = dialog.findViewById(R.id.articleTitle);
        ImageView articleMainImage = dialog.findViewById(R.id.articleMainImage);
        ImageView articleBodyImage = dialog.findViewById(R.id.articleBodyImage);

        articleTitle.setText(title);
        articleMainImage.setImageResource(mainImage);
        articleBodyImage.setImageResource(bodyImage);

        // Expand dialog to match parent
        dialog.getWindow().setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
        );

        dialog.show();
    }
}