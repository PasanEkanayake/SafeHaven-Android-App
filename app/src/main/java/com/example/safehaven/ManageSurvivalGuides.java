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

public class ManageSurvivalGuides extends AppCompatActivity {

    private Spinner spinnerDisasterType;
    private Button btnUploadBefore, btnUploadAfter, btnSaveGuide, btnBack;
    private TextView tvBeforePdf, tvAfterPdf;
    private EditText etYoutubeLink;

    private Uri beforePdfUri, afterPdfUri;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    private final int PICK_BEFORE_PDF = 101;
    private final int PICK_AFTER_PDF = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_survival_guides);

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

        spinnerDisasterType = findViewById(R.id.spinnerDisasterType);
        btnUploadBefore = findViewById(R.id.btnUploadBefore);
        btnUploadAfter = findViewById(R.id.btnUploadAfter);
        btnSaveGuide = findViewById(R.id.btnSaveGuide);
        tvBeforePdf = findViewById(R.id.tvBeforePdf);
        tvAfterPdf = findViewById(R.id.tvAfterPdf);
        etYoutubeLink = findViewById(R.id.etYoutubeLink);

        databaseRef = FirebaseDatabase.getInstance().getReference("SurvivalGuide");
        storageRef = FirebaseStorage.getInstance().getReference("SurvivalGuide");

        // Disaster types
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Floods", "Landslides"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDisasterType.setAdapter(adapter);

        // Load guide when type changes
        spinnerDisasterType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                loadGuideFromFirebase(spinnerDisasterType.getSelectedItem().toString().toLowerCase());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnUploadBefore.setOnClickListener(v -> pickPdf(PICK_BEFORE_PDF));
        btnUploadAfter.setOnClickListener(v -> pickPdf(PICK_AFTER_PDF));
        btnSaveGuide.setOnClickListener(v -> saveGuide());
    }

    private void pickPdf(int requestCode) {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri pdfUri = data.getData();
            if(requestCode == PICK_BEFORE_PDF){
                beforePdfUri = pdfUri;
                tvBeforePdf.setText("Selected: " + pdfUri.getLastPathSegment());
            } else if(requestCode == PICK_AFTER_PDF){
                afterPdfUri = pdfUri;
                tvAfterPdf.setText("Selected: " + pdfUri.getLastPathSegment());
            }
        }
    }

    private void saveGuide() {
        String youtubeLink = etYoutubeLink.getText().toString().trim();
        String type = spinnerDisasterType.getSelectedItem().toString().toLowerCase();

        if(beforePdfUri == null && afterPdfUri == null && youtubeLink.isEmpty()){
            Toast.makeText(this, "Add at least one field", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadFilesAndSave(type, beforePdfUri, afterPdfUri, youtubeLink);
    }

    private void uploadFilesAndSave(String type, Uri beforeUri, Uri afterUri, String youtubeLink) {
        final String[] beforeUrlHolder = {null};
        final String[] afterUrlHolder = {null};

        if(beforeUri != null){
            StorageReference beforeRef = storageRef.child(type + "/before.pdf");
            beforeRef.putFile(beforeUri).addOnSuccessListener(taskSnapshot ->
                    beforeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        beforeUrlHolder[0] = uri.toString();
                        saveToFirebase(type, beforeUrlHolder[0], afterUrlHolder[0], youtubeLink);
                    })
            );
        }

        if(afterUri != null){
            StorageReference afterRef = storageRef.child(type + "/after.pdf");
            afterRef.putFile(afterUri).addOnSuccessListener(taskSnapshot ->
                    afterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        afterUrlHolder[0] = uri.toString();
                        saveToFirebase(type, beforeUrlHolder[0], afterUrlHolder[0], youtubeLink);
                    })
            );
        }

        if(beforeUri == null && afterUri == null){
            saveToFirebase(type, null, null, youtubeLink);
        }
    }

    private void saveToFirebase(String type, String beforeUrl, String afterUrl, String youtubeLink){
        DatabaseReference typeRef = databaseRef.child(type);

        typeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String finalBefore = beforeUrl;
                String finalAfter = afterUrl;
                String finalYoutube = youtubeLink;

                if(snapshot.exists()){
                    SurvivalGuide guide = snapshot.getValue(SurvivalGuide.class);
                    if(finalBefore == null) finalBefore = guide.beforePdfUrl;
                    if(finalAfter == null) finalAfter = guide.afterPdfUrl;
                    if(finalYoutube.isEmpty()) finalYoutube = guide.youtubeLink;
                }

                SurvivalGuide updatedGuide = new SurvivalGuide(finalBefore, finalAfter, finalYoutube);
                typeRef.setValue(updatedGuide)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ManageSurvivalGuides.this, "Guide saved!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ManageSurvivalGuides.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageSurvivalGuides.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGuideFromFirebase(String type){
        DatabaseReference typeRef = databaseRef.child(type);
        typeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    SurvivalGuide guide = snapshot.getValue(SurvivalGuide.class);
                    tvBeforePdf.setText(guide.beforePdfUrl != null ? "Uploaded: before.pdf" : "No file selected");
                    tvAfterPdf.setText(guide.afterPdfUrl != null ? "Uploaded: after.pdf" : "No file selected");
                    etYoutubeLink.setText(guide.youtubeLink != null ? guide.youtubeLink : "");
                } else {
                    tvBeforePdf.setText("No file selected");
                    tvAfterPdf.setText("No file selected");
                    etYoutubeLink.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageSurvivalGuides.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
