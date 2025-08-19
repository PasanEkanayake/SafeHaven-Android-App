package com.example.safehaven;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

    private static final String TAG = "GuideLandslide";

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

        // Back button action
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(GuideLandslide.this, SurvivalGuides.class);
            startActivity(intent);
        });

        // Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference("SurvivalGuides").child("Landslides");

        // YouTube player setup
        getLifecycle().addObserver(youtubePlayerView);
        youtubePlayerView.setEnableAutomaticInitialization(false);

        // Load video link from Firebase, then initialize player
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
                    Log.d(TAG, "No videoLink found in Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Firebase videoLink read cancelled: " + error.getMessage());
            }
        });

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
                String value = snapshot.getValue(String.class);
                String title = type.equals("before") ? "Before a Landslide" : "During a Landslide";
                Log.d(TAG, "showGuidePopup: type=" + type + " rawValue=" + value);

                if (value == null || value.trim().isEmpty()) {
                    Toast.makeText(GuideLandslide.this, "No data found for: " + type, Toast.LENGTH_SHORT).show();
                    new AlertDialog.Builder(GuideLandslide.this)
                            .setTitle(title)
                            .setMessage("No data available.")
                            .setPositiveButton("OK", (d, w) -> d.dismiss())
                            .show();
                    return;
                }

                String trimmed = value.trim();

                boolean looksLikeUrl = (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains("drive.google.com"));

                if (!looksLikeUrl) {
                    new AlertDialog.Builder(GuideLandslide.this)
                            .setTitle(title)
                            .setMessage(trimmed)
                            .setPositiveButton("OK", (d, w) -> d.dismiss())
                            .show();
                    return;
                }

                String finalUrl = convertGoogleDriveUrlIfNeeded(trimmed);
                Log.d(TAG, "showGuidePopup: finalUrl=" + finalUrl);

                ScrollView scrollView = new ScrollView(GuideLandslide.this);
                LinearLayout container = new LinearLayout(GuideLandslide.this);
                container.setOrientation(LinearLayout.VERTICAL);
                int pad = dpToPx(8);
                container.setPadding(pad, pad, pad, pad);

                ImageView imageView = new ImageView(GuideLandslide.this);
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                container.addView(imageView, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                scrollView.addView(container);

                AlertDialog dialog = new AlertDialog.Builder(GuideLandslide.this)
                        .setTitle(title)
                        .setView(scrollView)
                        .setPositiveButton("OK", (d, w) -> d.dismiss())
                        .create();

                dialog.show();

                if (dialog.getWindow() != null) {
                    int margin = dpToPx(16);
                    int width = getResources().getDisplayMetrics().widthPixels - margin * 2;
                    dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                }

                Glide.with(GuideLandslide.this)
                        .load(finalUrl)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.w(TAG, "Glide load failed for url: " + finalUrl, e);
                                runOnUiThread(() -> {
                                    Toast.makeText(GuideLandslide.this, "Failed to load image. Check URL or permissions.", Toast.LENGTH_LONG).show();
                                    // show the raw text as fallback
                                    new AlertDialog.Builder(GuideLandslide.this)
                                            .setTitle(title)
                                            .setMessage(trimmed)
                                            .setPositiveButton("OK", (d, w) -> d.dismiss())
                                            .show();
                                    try {
                                        if (dialog.isShowing()) dialog.dismiss();
                                    } catch (Exception ex) {
                                        // ignore
                                    }
                                });
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d(TAG, "Glide load succeeded for url: " + finalUrl);
                                return false;
                            }
                        })
                        .into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Firebase read cancelled for '" + type + "': " + error.getMessage());
                Toast.makeText(GuideLandslide.this, "Firebase read failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
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
        if (youtubePlayerView != null) {
            youtubePlayerView.release();
        }
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

    // convert dp to px for padding calculations
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
