package com.example.safehaven;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NewsDetail extends AppCompatActivity {
    private static final String TAG = "NewsDetailActivity";

    private ImageView imageFull, bodyImage, btnBack;
    private TextView titleTv, videoLinkTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

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

        imageFull = findViewById(R.id.imageFull);
        bodyImage = findViewById(R.id.bodyImage);
        titleTv = findViewById(R.id.titleTv);
        videoLinkTv = findViewById(R.id.videoLinkTv);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        String title = intent.getStringExtra(NewsViewAdapter.EXTRA_TITLE);
        String imageUrl = intent.getStringExtra(NewsViewAdapter.EXTRA_IMAGE);
        String newsBody = intent.getStringExtra(NewsViewAdapter.EXTRA_NEWS_BODY);
        String bodyImageUrl = intent.getStringExtra(NewsViewAdapter.EXTRA_BODY_IMAGE);
        String videoLink = intent.getStringExtra(NewsViewAdapter.EXTRA_VIDEO);

        Log.d(TAG, "received extras: title=" + title + " imageUrl=" + imageUrl + " bodyImageUrl=" + bodyImageUrl + " videoLink=" + videoLink);

        titleTv.setText(title != null ? title : "");

        if (!TextUtils.isEmpty(imageUrl)) {
            imageFull.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(imageFull);
        } else {
            imageFull.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bodyImageUrl)) {
            bodyImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(bodyImageUrl).into(bodyImage);
        } else {
            bodyImage.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(videoLink)) {
            videoLinkTv.setText(videoLink);
            videoLinkTv.setOnClickListener(v -> {
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(videoLink));
                    startActivity(i);
                } catch (Exception e) {
                    Log.e(TAG, "cannot open video link", e);
                }
            });
        } else {
            videoLinkTv.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());
    }
}
