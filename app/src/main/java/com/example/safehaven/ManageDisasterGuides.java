package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ManageDisasterGuides extends AppCompatActivity {

    private Spinner spinnerType;
    private EditText editImageUrl, editDescription;

    private ImageView btnBack;
    private Button btnSaveGuide;

    private DatabaseReference databaseReference;

    private String selectedDisaster = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_disaster_guides);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageDisasterGuides.this, AdminPanel.class);
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

        spinnerType = findViewById(R.id.spinnerType);
        editImageUrl = findViewById(R.id.imageUrl);
        editDescription = findViewById(R.id.editNewsBody);
        btnSaveGuide = findViewById(R.id.btnSaveGuide);

        // Spinner setup
        String[] disasterTypes = {"Floods", "Landslides"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, disasterTypes);
        spinnerType.setAdapter(adapter);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("DisasterGuides");

        // Spinner listener
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedDisaster = disasterTypes[position];
                loadData(selectedDisaster);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // Button click
        btnSaveGuide.setOnClickListener(v -> saveOrUpdateGuide());
    }

    private void loadData(String disasterType) {
        databaseReference.child(disasterType)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                            String description = snapshot.child("description").getValue(String.class);

                            editImageUrl.setText(imageUrl != null ? imageUrl : "");
                            editDescription.setText(description != null ? description : "");
                        } else {
                            editImageUrl.setText("");
                            editDescription.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ManageDisasterGuides.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveOrUpdateGuide() {
        String imageUrl = editImageUrl.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        if (selectedDisaster.isEmpty()) {
            Toast.makeText(this, "Select a disaster type!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUrl.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Both fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> guideData = new HashMap<>();
        guideData.put("imageUrl", imageUrl);
        guideData.put("description", description);

        databaseReference.child(selectedDisaster).setValue(guideData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Guide updated for " + selectedDisaster, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
