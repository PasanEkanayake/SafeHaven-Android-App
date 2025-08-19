package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ManageLatestNews extends AppCompatActivity {

    private EditText editImageUrl, editNewsTitle, editNewsBody, editVideoLink, editNewsBodyUrl;
    private Button btnSaveNews;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_latest_news);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(ManageLatestNews.this, AdminPanel.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

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

        // Initialize Views
        editImageUrl = findViewById(R.id.imageUrl);
        editNewsTitle = findViewById(R.id.editNewsTitle);
        editNewsBody = findViewById(R.id.editNewsBody);
        editNewsBodyUrl = findViewById(R.id.newsBodyUrl);
        editVideoLink = findViewById(R.id.editVideoLink);
        btnSaveNews = findViewById(R.id.btnSaveNews);
        progressBar = findViewById(R.id.progressBar);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("LatestNews");

        btnSaveNews.setOnClickListener(v -> uploadNews());
    }

    private void uploadNews() {
        String imageUrl = editImageUrl.getText().toString().trim();
        String title = editNewsTitle.getText().toString().trim();
        String newsBody = editNewsBody.getText().toString().trim();
        String newsBodyUrl = editNewsBodyUrl.getText().toString().trim();
        String videoLink = editVideoLink.getText().toString().trim();

        if (title.isEmpty() || newsBody.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Title, Body and Image URL are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveNews.setEnabled(false);

        String newsId = databaseReference.push().getKey();

        Map<String, Object> newsMap = new HashMap<>();
        newsMap.put("title", title);
        newsMap.put("newsBody", newsBody);
        newsMap.put("newsBodyUrl", newsBodyUrl);
        newsMap.put("videoLink", videoLink);
        newsMap.put("imageUrl", imageUrl);

        if (newsId != null) {
            databaseReference.child(newsId).setValue(newsMap)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        btnSaveNews.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(ManageLatestNews.this, "News uploaded successfully!", Toast.LENGTH_SHORT).show();
                            clearFields();
                        } else {
                            Toast.makeText(ManageLatestNews.this, "Failed to upload news: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void clearFields() {
        editImageUrl.setText("");
        editNewsTitle.setText("");
        editNewsBody.setText("");
        editNewsBodyUrl.setText("");
        editVideoLink.setText("");
    }
}