package com.example.safehaven;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

public class ManageDisasterGuides extends AppCompatActivity {

    private Spinner spinnerType;
    private Button btnUploadImage, btnUploadPdf, btnSaveGuide, btnBack;
    private TextView tvImage, tvPdf;

    private Uri imageUri, pdfUri;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    private final int PICK_IMAGE = 101;
    private final int PICK_PDF = 102;

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
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnUploadPdf = findViewById(R.id.btnUploadPdf);
        btnSaveGuide = findViewById(R.id.btnSaveGuide);
        tvImage = findViewById(R.id.tvImage);
        tvPdf = findViewById(R.id.tvPdf);

        // Spinner setup
        String[] types = {"Floods", "Landslides"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("DisasterGuides");
        storageRef = FirebaseStorage.getInstance().getReference("DisasterGuides");

        // Load selected guide
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadGuideFromFirebase(spinnerType.getSelectedItem().toString().toLowerCase());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnUploadImage.setOnClickListener(v -> pickFile("image/*", PICK_IMAGE));
        btnUploadPdf.setOnClickListener(v -> pickFile("application/pdf", PICK_PDF));
        btnSaveGuide.setOnClickListener(v -> saveGuide());
    }

    private void pickFile(String type, int requestCode){
        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri fileUri = data.getData();
            if(requestCode == PICK_IMAGE){
                imageUri = fileUri;
                tvImage.setText("Selected: " + fileUri.getLastPathSegment());
            } else if(requestCode == PICK_PDF){
                pdfUri = fileUri;
                tvPdf.setText("Selected: " + fileUri.getLastPathSegment());
            }
        }
    }

    private void saveGuide() {
        String type = spinnerType.getSelectedItem().toString().toLowerCase();

        if(imageUri == null && pdfUri == null){
            Toast.makeText(this, "Add at least one file", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadFilesAndSave(type, imageUri, pdfUri);
    }

    private void uploadFilesAndSave(String type, Uri imageUri, Uri pdfUri) {
        final String node = type;

        if(imageUri != null){
            StorageReference imageRef = storageRef.child(node + "/image.jpg");
            imageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imgUrl = uri.toString();
                        saveToFirebase(node, imgUrl, null);
                    })
            );
        }

        if(pdfUri != null){
            StorageReference pdfRef = storageRef.child(node + "/guide.pdf");
            pdfRef.putFile(pdfUri).addOnSuccessListener(taskSnapshot ->
                    pdfRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String pdfUrl = uri.toString();
                        saveToFirebase(node, null, pdfUrl);
                    })
            );
        }
    }

    private void saveToFirebase(String type, String imageUrl, String pdfUrl){
        String selectedType = spinnerType.getSelectedItem().toString().toLowerCase();
        DatabaseReference typeRef = databaseRef.child(selectedType);

        typeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DisasterGuide guide = snapshot.exists() ? snapshot.getValue(DisasterGuide.class) : new DisasterGuide();

                if(imageUrl != null) guide.imageUrl = imageUrl;
                if(pdfUrl != null) guide.pdfUrl = pdfUrl;

                typeRef.setValue(guide).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(ManageDisasterGuides.this, "Guide updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ManageDisasterGuides.this, "Failed to update guide", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDisasterGuides.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGuideFromFirebase(String type){
        databaseRef.child(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    DisasterGuide guide = snapshot.getValue(DisasterGuide.class);
                    tvImage.setText(guide.imageUrl != null ? "Image exists" : "No image selected");
                    tvPdf.setText(guide.pdfUrl != null ? "PDF exists" : "No file selected");
                } else {
                    tvImage.setText("No image selected");
                    tvPdf.setText("No file selected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageDisasterGuides.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
