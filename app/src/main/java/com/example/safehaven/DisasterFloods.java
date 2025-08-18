package com.example.safehaven;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.safehaven.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DisasterFloods extends AppCompatActivity {
    private static final String TAG = "FloodDetailActivity";

    private ImageView disasterImage;
    private TextView disasterDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disaster_floods);

        disasterImage = findViewById(R.id.disasterImage);
        disasterDescription = findViewById(R.id.disasterDescription);

        DatabaseReference floodsRef = FirebaseDatabase.getInstance()
                .getReference("DisasterGuides")
                .child("Floods");

        floodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String imageUrl = null;
                    String description = null;

                    if (snapshot.exists()) {
                        if (snapshot.child("ImageUrl").getValue() != null) {
                            imageUrl = snapshot.child("ImageUrl").getValue(String.class);
                        } else if (snapshot.child("imageUrl").getValue() != null) {
                            imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        }

                        if (snapshot.child("description").getValue() != null) {
                            description = snapshot.child("description").getValue(String.class);
                        } else if (snapshot.child("Description").getValue() != null) {
                            description = snapshot.child("Description").getValue(String.class);
                        }
                    }

                    if (!TextUtils.isEmpty(description)) {
                        disasterDescription.setText(description);
                    } else {
                        disasterDescription.setText("No description available.");
                    }

                    if (!TextUtils.isEmpty(imageUrl)) {
                        String finalUrl = convertGoogleDriveUrlIfNeeded(imageUrl);
                        Glide.with(DisasterFloods.this)
                                .load(finalUrl)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .into(disasterImage);
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
