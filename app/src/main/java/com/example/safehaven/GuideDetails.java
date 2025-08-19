package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class GuideDetails extends AppCompatActivity {

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_details);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuideDetails.this, Recover.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.settings).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Settings.class));
            overridePendingTransition(0, 0);
        });

        TextView titleTextView = findViewById(R.id.detailTitle);
        ImageView mainImageView = findViewById(R.id.detailMainImage);
        ImageView bodyImageView = findViewById(R.id.detailBodyImage);

        String title = getIntent().getStringExtra("title");
        int mainImage = getIntent().getIntExtra("mainImage", 0);
        String bodyImageUrl = getIntent().getStringExtra("bodyImageUrl");

        titleTextView.setText(title);
        if (mainImage != 0) {
            mainImageView.setImageResource(mainImage);
        }

        if (bodyImageUrl != null && !bodyImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(bodyImageUrl)
                    .into(bodyImageView);
        }
    }
}
