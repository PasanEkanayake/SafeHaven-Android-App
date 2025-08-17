package com.example.safehaven;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ManageSurvivalGuides extends AppCompatActivity {

    private Button btnUploadBefore, btnUploadAfter, btnSaveGuide;
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

        btnUploadBefore = findViewById(R.id.btnUploadBefore);
        btnUploadAfter = findViewById(R.id.btnUploadAfter);
        btnSaveGuide = findViewById(R.id.btnSaveGuide);
        tvBeforePdf = findViewById(R.id.tvBeforePdf);
        tvAfterPdf = findViewById(R.id.tvAfterPdf);
        etYoutubeLink = findViewById(R.id.etYoutubeLink);

        databaseRef = FirebaseDatabase.getInstance().getReference("SurvivalGuide");
        storageRef = FirebaseStorage.getInstance().getReference("SurvivalGuide");

        // Load existing guide
        loadGuideFromFirebase();

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

        if(beforePdfUri == null && afterPdfUri == null && youtubeLink.isEmpty()){
            Toast.makeText(this, "Add at least one field", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadFilesAndSave(beforePdfUri, afterPdfUri, youtubeLink);
    }

    private void uploadFilesAndSave(Uri beforeUri, Uri afterUri, String youtubeLink) {
        // Upload Before PDF
        if(beforeUri != null){
            StorageReference beforeRef = storageRef.child("before.pdf");
            beforeRef.putFile(beforeUri).addOnSuccessListener(taskSnapshot ->
                    beforeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String beforeUrl = uri.toString();
                        saveToFirebase(beforeUrl, null, youtubeLink);
                    })
            );
        }

        // Upload After PDF
        if(afterUri != null){
            StorageReference afterRef = storageRef.child("after.pdf");
            afterRef.putFile(afterUri).addOnSuccessListener(taskSnapshot ->
                    afterRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String afterUrl = uri.toString();
                        saveToFirebase(null, afterUrl, youtubeLink);
                    })
            );
        }

        // If only YouTube link is updated
        if(beforeUri == null && afterUri == null){
            saveToFirebase(null, null, youtubeLink);
        }
    }

    private void saveToFirebase(String beforeUrl, String afterUrl, String youtubeLink){
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String finalBefore = beforeUrl;
                String finalAfter = afterUrl;
                String finalYoutube = youtubeLink;

                // If data exists, keep existing PDFs if new not uploaded
                if(snapshot.exists()){
                    SurvivalGuide guide = snapshot.getValue(SurvivalGuide.class);
                    if(finalBefore == null) finalBefore = guide.beforePdfUrl;
                    if(finalAfter == null) finalAfter = guide.afterPdfUrl;
                    if(finalYoutube.isEmpty()) finalYoutube = guide.youtubeLink;
                }

                SurvivalGuide updatedGuide = new SurvivalGuide(finalBefore, finalAfter, finalYoutube);
                databaseRef.setValue(updatedGuide)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ManageSurvivalGuides.this, "Guide saved!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ManageSurvivalGuides.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageSurvivalGuides.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGuideFromFirebase(){
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    SurvivalGuide guide = snapshot.getValue(SurvivalGuide.class);
                    if(guide.beforePdfUrl != null) tvBeforePdf.setText("Uploaded: before.pdf");
                    if(guide.afterPdfUrl != null) tvAfterPdf.setText("Uploaded: after.pdf");
                    if(guide.youtubeLink != null) etYoutubeLink.setText(guide.youtubeLink);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageSurvivalGuides.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}