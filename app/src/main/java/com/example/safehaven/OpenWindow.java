package com.example.safehaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OpenWindow extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_open_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);
        boolean isRegistered = sharedPreferences.getBoolean("isRegistered", false);
        boolean introFinished = sharedPreferences.getBoolean("introFinished", false);
        boolean isAdminLogged = sharedPreferences.getBoolean("isAdmin", false);

        new Handler().postDelayed(() -> {
            Intent intent;
            if (isRegistered && introFinished) {
                intent = new Intent(OpenWindow.this, Home.class);
            }
            else if(isAdminLogged) {
                intent = new Intent(OpenWindow.this, Home.class);
            }else {
                intent = new Intent(OpenWindow.this, SelectLanguage.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
