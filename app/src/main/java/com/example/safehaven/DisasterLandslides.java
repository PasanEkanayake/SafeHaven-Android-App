package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisasterLandslides extends AppCompatActivity {
    private static final String TAG = "DisasterLandslides";

    private ImageView disasterImage; // existing top image in layout
    private ImageView disasterImageFromDescription; // replacement for the previous TextView
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disaster_landslides);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisasterLandslides.this, DisasterTypes.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        // Navigation handling
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_guides) {
                startActivity(new Intent(getApplicationContext(), SurvivalGuides.class));
                overridePendingTransition(0, 0);
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

        // find views
        disasterImage = findViewById(R.id.disasterImage);
        disasterImageFromDescription = findViewById(R.id.disasterImageFromDescription);

        DatabaseReference landslidesRef = FirebaseDatabase.getInstance()
                .getReference("DisasterGuides")
                .child("Landslides");

        landslidesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String imageUrl = null;
                    String description = null;

                    if (snapshot.exists()) {
                        // support both "ImageUrl" and "imageUrl"
                        if (snapshot.child("ImageUrl").getValue() != null) {
                            imageUrl = snapshot.child("ImageUrl").getValue(String.class);
                        } else if (snapshot.child("imageUrl").getValue() != null) {
                            imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        }

                        // description field (may contain an image URL)
                        if (snapshot.child("description").getValue() != null) {
                            description = snapshot.child("description").getValue(String.class);
                        } else if (snapshot.child("Description").getValue() != null) {
                            description = snapshot.child("Description").getValue(String.class);
                        }
                    }

                    // Load top image if imageUrl exists (keeps previous behavior)
                    if (!TextUtils.isEmpty(imageUrl)) {
                        String finalUrl = convertGoogleDriveUrlIfNeeded(imageUrl);
                        Glide.with(DisasterLandslides.this)
                                .load(finalUrl)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(disasterImage);
                    } else {
                        // optional: hide or set placeholder
                        // disasterImage.setVisibility(View.GONE);
                    }

                    // If description contains a URL (http/https or Google Drive) -> load into new ImageView
                    if (!TextUtils.isEmpty(description) &&
                            (description.startsWith("http://") || description.startsWith("https://") || description.contains("drive.google.com"))) {
                        String finalDescUrl = convertGoogleDriveUrlIfNeeded(description);
                        Glide.with(DisasterLandslides.this)
                                .load(finalDescUrl)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(disasterImageFromDescription);
                    } else {
                        // description isn't a URL: choose desired behavior (hide, placeholder, or do nothing)
                        Log.d(TAG, "Description field is empty or does not contain a valid URL.");
                        // Example: hide the view
                        // disasterImageFromDescription.setVisibility(View.GONE);
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error parsing snapshot", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    private String convertGoogleDriveUrlIfNeeded(String url) {
        if (url == null) return null;

        if (url.contains("drive.google.com/uc") || url.contains("googleusercontent.com")) {
            return url;
        }

        try {
            if (url.contains("drive.google.com") && url.contains("/file/d/")) {
                int start = url.indexOf("/file/d/") + "/file/d/".length();
                int end = url.indexOf("/", start);
                if (end == -1) end = url.length();
                String fileId = url.substring(start, end);
                if (!fileId.isEmpty()) {
                    return "https://drive.google.com/uc?export=download&id=" + fileId;
                }
            }

            if (url.contains("drive.google.com") && url.contains("open?id=")) {
                int start = url.indexOf("open?id=") + "open?id=".length();
                int end = url.indexOf("&", start);
                if (end == -1) end = url.length();
                String fileId = url.substring(start, end);
                if (!fileId.isEmpty()) {
                    return "https://drive.google.com/uc?export=download&id=" + fileId;
                }
            }

            return url;
        } catch (Exception e) {
            Log.w(TAG, "Failed to convert Drive URL, returning original", e);
            return url;
        }
    }
}