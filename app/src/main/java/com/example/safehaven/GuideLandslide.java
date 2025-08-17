package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class GuideLandslide extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private YouTubePlayerView youtubePlayerView;
    private FrameLayout youtubeContainer;
    private String videoId = "";
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_landslide);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        youtubeContainer = findViewById(R.id.youtube_container);
        youtubePlayerView = findViewById(R.id.youtubePlayerView);
        Button btnBeforeLandslide = findViewById(R.id.btnBeforeLandslide);
        Button btnDuringLandslide = findViewById(R.id.btnDuringLandslide);
        Button btnAfterLandslide = findViewById(R.id.btnAfterLandslide);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Back button


        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuideLandslide.this, SurvivalGuides.class);
                startActivity(intent);
            }
        });

        // Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference("SurvivalGuides").child("Landslides");

        getLifecycle().addObserver(youtubePlayerView);

        youtubePlayerView.setEnableAutomaticInitialization(false);

        // Load video link from Firebase, then initialize player with options
        databaseRef.child("videoLink").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String videoUrl = snapshot.getValue(String.class);
                    if (videoUrl != null) {
                        if (videoUrl.contains("v=")) {
                            videoId = videoUrl.substring(videoUrl.indexOf("v=") + 2);
                            int amp = videoId.indexOf('&');
                            if (amp != -1) videoId = videoId.substring(0, amp);
                        } else if (videoUrl.contains("youtu.be/")) {
                            videoId = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
                            int amp = videoId.indexOf('?');
                            if (amp != -1) videoId = videoId.substring(0, amp);
                        }
                    }

                    IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder()
                            .controls(1)
                            .fullscreen(1)
                            .build();

                    youtubePlayerView.initialize(new AbstractYouTubePlayerListener() {
                        @Override
                        public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                            if (videoId != null && !videoId.isEmpty()) {
                                youTubePlayer.loadVideo(videoId, 0f);
                            }
                        }
                    }, iFramePlayerOptions);

                    youtubePlayerView.addFullscreenListener(new FullscreenListener() {
                        @Override
                        public void onEnterFullscreen(@NonNull View fullscreenView, @NonNull Function0<Unit> exitFullscreen) {
                            youtubeContainer.setVisibility(View.GONE);
                            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();

                            decorView.addView(fullscreenView, new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));

                        }

                        @Override
                        public void onExitFullscreen() {
                            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();

                            int childCount = decorView.getChildCount();
                            if (childCount > 0) {
                                for (int i = childCount - 1; i >= 0; i--) {
                                    View child = decorView.getChildAt(i);
                                    ViewGroup.LayoutParams lp = child.getLayoutParams();
                                    if (lp != null &&
                                            lp.width == ViewGroup.LayoutParams.MATCH_PARENT &&
                                            lp.height == ViewGroup.LayoutParams.MATCH_PARENT &&
                                            child != findViewById(android.R.id.content)) {
                                        decorView.removeView(child);
                                        break;
                                    }
                                }
                            }

                            youtubeContainer.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    // no video link
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Button popups
        btnBeforeLandslide.setOnClickListener(v -> showGuidePopup("before"));
        btnDuringLandslide.setOnClickListener(v -> showGuidePopup("during"));
        btnAfterLandslide.setOnClickListener(v -> startActivity(new Intent(GuideLandslide.this, Recover.class)));

        // Bottom navigation
        setupBottomNavigation(bottomNavigationView);

        // Settings button
        findViewById(R.id.settings).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Settings.class));
            overridePendingTransition(0, 0);
        });
    }

    private void showGuidePopup(String type) {
        databaseRef.child(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String text = snapshot.getValue(String.class);
                String title = type.equals("before") ? "Before a Landslide" : "During a Landslide";
                new AlertDialog.Builder(GuideLandslide.this)
                        .setTitle(title)
                        .setMessage(text != null ? text : "No data available.")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_guides) {
                startActivity(new Intent(getApplicationContext(), SurvivalGuides.class));
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
            } else if (itemId == R.id.nav_recover) {
                startActivity(new Intent(getApplicationContext(), Recover.class));
            } else if (itemId == R.id.nav_contacts) {
                startActivity(new Intent(getApplicationContext(), Contacts.class));
            } else return false;

            overridePendingTransition(0, 0);
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // release the player to avoid leaks
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
    }
}