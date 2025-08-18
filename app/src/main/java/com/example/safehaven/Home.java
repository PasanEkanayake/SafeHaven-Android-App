package com.example.safehaven;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home extends AppCompatActivity {

    private Button contactsButton, moreNewsButton;
    private ImageView settingsButton;
    private LinearLayout survivalguides, disasterTypes, postDisasterButton, locationsButton;
    private LinearLayout newsCard1, newsCard2;
    private ImageView newsImage1, newsImage2;
    private TextView newsTitle1, newsTitle2, newsBody1, newsBody2;

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

        // === Initialize Views ===
        contactsButton = findViewById(R.id.contactsButton);
        settingsButton = findViewById(R.id.settings);
        survivalguides = findViewById(R.id.survivalguides);
        disasterTypes = findViewById(R.id.disasterTypes);
        postDisasterButton = findViewById(R.id.postDisasterButton);
        locationsButton = findViewById(R.id.locationsButton);
        moreNewsButton = findViewById(R.id.moreNewsButton);

        newsCard1 = findViewById(R.id.newsCard1);
        newsCard2 = findViewById(R.id.newsCard2);
        newsImage1 = findViewById(R.id.newsImage1);
        newsImage2 = findViewById(R.id.newsImage2);
        newsTitle1 = findViewById(R.id.newsTitle1);
        newsTitle2 = findViewById(R.id.newsTitle2);
        newsBody1 = findViewById(R.id.newsBody1);
        newsBody2 = findViewById(R.id.newsBody2);

        // === Language Greeting ===
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

        // === Bottom Navigation ===
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
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

        // === Button Clicks ===
        contactsButton.setOnClickListener(v -> startActivity(new Intent(Home.this, Contacts.class)));
        settingsButton.setOnClickListener(v -> startActivity(new Intent(Home.this, Settings.class)));
        survivalguides.setOnClickListener(v -> startActivity(new Intent(Home.this, SurvivalGuides.class)));
        disasterTypes.setOnClickListener(v -> startActivity(new Intent(Home.this, DisasterTypes.class)));
        postDisasterButton.setOnClickListener(v -> startActivity(new Intent(Home.this, Recover.class)));
        locationsButton.setOnClickListener(v -> startActivity(new Intent(Home.this, Locations.class)));
        moreNewsButton.setOnClickListener(v -> startActivity(new Intent(Home.this, ViewNewsArticles.class)));

        // === Load Latest News from Firebase ===
        loadLatestNews();
    }

    // Load 2 latest news
    private void loadLatestNews() {
        DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("LatestNews");

        newsRef.limitToLast(2).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int index = 0;
                for (DataSnapshot newsSnapshot : snapshot.getChildren()) {
                    String imageUrl = newsSnapshot.child("imageUrl").getValue(String.class);
                    String title = newsSnapshot.child("title").getValue(String.class);
                    String body = newsSnapshot.child("newsBody").getValue(String.class); // still used for preview
                    String video = newsSnapshot.child("videoLink").getValue(String.class);
                    String newsBodyUrl = newsSnapshot.child("newsBodyUrl").getValue(String.class); // ✅ new field

                    if (index == 0) {
                        setNewsCard(newsImage1, newsTitle1, newsBody1, imageUrl, title, body);
                        newsCard1.setOnClickListener(v -> showNewsPopup(title, imageUrl, video, newsBodyUrl));
                    } else if (index == 1) {
                        setNewsCard(newsImage2, newsTitle2, newsBody2, imageUrl, title, body);
                        newsCard2.setOnClickListener(v -> showNewsPopup(title, imageUrl, video, newsBodyUrl));
                    }
                    index++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    // Set preview card
    private void setNewsCard(ImageView img, TextView titleTv, TextView bodyTv,
                             String imgUrl, String title, String body) {
        titleTv.setText(title);
        bodyTv.setText(truncateText(body, 80)); // show only first 80 chars
        Glide.with(this).load(imgUrl).into(img);
    }

    // Truncate preview text
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "...";
        }
        return text;
    }

    // Popup for full article
    // Popup for full article
    private void showNewsPopup(String title, String imageUrl, String videoLink, String newsBodyUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View popupView = LayoutInflater.from(this).inflate(R.layout.news_popup, null);

        TextView popupTitle = popupView.findViewById(R.id.newsTitle);
        ImageView popupImg = popupView.findViewById(R.id.newsImage);
        ImageView popupBodyImage = popupView.findViewById(R.id.newsBodyImage);
        TextView popupVideo = popupView.findViewById(R.id.newsVideoLink);

        popupTitle.setText(title);
        Glide.with(this).load(imageUrl).into(popupImg);        // main news thumbnail
        Glide.with(this).load(newsBodyUrl).into(popupBodyImage); // full article image

        if (videoLink != null && !videoLink.isEmpty()) {
            popupVideo.setText("Video: " + videoLink);
            popupVideo.setVisibility(View.VISIBLE);
        } else {
            popupVideo.setVisibility(View.GONE);
        }

        builder.setView(popupView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
