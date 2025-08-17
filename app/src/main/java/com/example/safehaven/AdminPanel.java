package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminPanel extends AppCompatActivity {

    private Button btnManageSurvival, btnManageDisaster, btnManageNews, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_panel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        btnManageSurvival = findViewById(R.id.btnManageSurvival);
        btnManageDisaster = findViewById(R.id.btnManageDisaster);
        btnManageNews = findViewById(R.id.btnManageNews);
        logoutButton = findViewById(R.id.logoutButton);

        // Button click listeners
        btnManageSurvival.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPanel.this, ManageSurvivalGuides.class);
                startActivity(intent);
            }
        });

        btnManageDisaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPanel.this, ManageDisasterGuides.class);
                startActivity(intent);
            }
        });

        btnManageNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminPanel.this, ManageLatestNews.class);
                startActivity(intent);
            }
        });

    }
}