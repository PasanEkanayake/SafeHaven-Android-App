package com.example.safehaven;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ManageSurvivalGuides extends AppCompatActivity {

    private Spinner spinnerDisasterType;
    private EditText beforeGuide, duringGuide, etYoutubeLink;
    private Button btnSaveGuide;
    private ImageView btnBack;
    private DatabaseReference databaseRef;

    private String selectedDisaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_survival_guides);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageSurvivalGuides.this, AdminPanel.class);
                startActivity(intent);
            }
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

        // Initialize Views
        spinnerDisasterType = findViewById(R.id.spinnerDisasterType);
        beforeGuide = findViewById(R.id.beforeGuide);
        duringGuide = findViewById(R.id.duringGuide);
        etYoutubeLink = findViewById(R.id.etYoutubeLink);
        btnSaveGuide = findViewById(R.id.btnSaveGuide);

        // Firebase Reference
        databaseRef = FirebaseDatabase.getInstance().getReference("SurvivalGuides");

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Floods", "Landslides"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisasterType.setAdapter(adapter);

        // On selecting disaster type
        spinnerDisasterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDisaster = parent.getItemAtPosition(position).toString();
                loadGuideData(selectedDisaster);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Save/Update Button
        btnSaveGuide.setOnClickListener(v -> saveOrUpdateGuide());
    }

    private void loadGuideData(String disasterType) {
        databaseRef.child(disasterType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String before = snapshot.child("before").getValue(String.class);
                    String during = snapshot.child("during").getValue(String.class);
                    String videoLink = snapshot.child("videoLink").getValue(String.class);

                    beforeGuide.setText(before != null ? before : "");
                    duringGuide.setText(during != null ? during : "");
                    etYoutubeLink.setText(videoLink != null ? videoLink : "");
                } else {
                    // No data, clear fields
                    beforeGuide.setText("");
                    duringGuide.setText("");
                    etYoutubeLink.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageSurvivalGuides.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrUpdateGuide() {
        String before = beforeGuide.getText().toString().trim();
        String during = duringGuide.getText().toString().trim();
        String videoLink = etYoutubeLink.getText().toString().trim();

        if (TextUtils.isEmpty(selectedDisaster)) {
            Toast.makeText(this, "Please select a disaster type", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> guideData = new HashMap<>();
        guideData.put("before", before);
        guideData.put("during", during);
        guideData.put("videoLink", videoLink);

        databaseRef.child(selectedDisaster).setValue(guideData)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ManageSurvivalGuides.this, "Guide saved/updated!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(ManageSurvivalGuides.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
