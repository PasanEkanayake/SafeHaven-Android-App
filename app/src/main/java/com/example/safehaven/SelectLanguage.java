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

public class SelectLanguage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_language);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button sinhalaButton = findViewById(R.id.sinhalaButton);
        Button englishButton = findViewById(R.id.englishButton);
        Button tamilButton = findViewById(R.id.tamilButton);

        View.OnClickListener languageClickListener = v -> {
            String selectedLanguage = "";
            if (v.getId() == R.id.sinhalaButton) selectedLanguage = "Sinhala";
            else if (v.getId() == R.id.englishButton) selectedLanguage = "English";
            else if (v.getId() == R.id.tamilButton) selectedLanguage = "Tamil";

            // Save selected language in SharedPreferences
            getSharedPreferences("SafeHavenPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("language", selectedLanguage)
                    .apply();

            // Go to Next Page
            Intent intent = new Intent(SelectLanguage.this, UserRegister.class);
            startActivity(intent);
            finish(); // close LanguageSelection so user can't go back
        };

        sinhalaButton.setOnClickListener(languageClickListener);
        englishButton.setOnClickListener(languageClickListener);
        tamilButton.setOnClickListener(languageClickListener);
    }
}