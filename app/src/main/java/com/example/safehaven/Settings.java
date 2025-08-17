package com.example.safehaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Settings extends AppCompatActivity {

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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

        logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Redirect to Home
            Intent intent = new Intent(Settings.this, SelectLanguage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        LinearLayout editProfileButton = findViewById(R.id.editProfileButton);

        editProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(Settings.this, EditProfile.class));
        });

        LinearLayout changeLanguageButton = findViewById(R.id.changeLanguageButton);

        changeLanguageButton.setOnClickListener(v -> {
            // List of languages
            String[] languages = {"English", "Sinhala", "Tamil"};

            // Get current selected language
            SharedPreferences sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);
            String currentLang = sharedPreferences.getString("language", "English");

            TextView currentLanguage = findViewById(R.id.currentLanguage);
            currentLanguage.setText(currentLang);

            int checkedItem = 0;
            for (int i = 0; i < languages.length; i++) {
                if (languages[i].equals(currentLang)) {
                    checkedItem = i;
                    break;
                }
            }

            new androidx.appcompat.app.AlertDialog.Builder(Settings.this)
                    .setTitle("Select Preferred Language")
                    .setSingleChoiceItems(languages, checkedItem, null)
                    .setPositiveButton("OK", (dialog, whichButton) -> {
                        int selectedPosition = ((androidx.appcompat.app.AlertDialog) dialog).getListView().getCheckedItemPosition();
                        String selectedLanguage = languages[selectedPosition];

                        // Save selected language in SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("language", selectedLanguage);
                        editor.apply();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        SharedPreferences sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        if (isAdmin) {
            // Show admin buttons
            findViewById(R.id.adminPanelButton).setVisibility(View.VISIBLE);
        }

    }
}