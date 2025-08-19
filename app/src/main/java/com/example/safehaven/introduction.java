package com.example.safehaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class introduction extends AppCompatActivity {

    private Button finishButton;
    private Button backButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_introduction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(introduction.this, UserRegister.class));
            finish();
        });

        sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);
        finishButton = findViewById(R.id.finishButton);

        finishButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("introFinished", true);
            editor.apply();

            // Go to Home
            startActivity(new Intent(introduction.this, Home.class));
            finish();
        });

    }
}