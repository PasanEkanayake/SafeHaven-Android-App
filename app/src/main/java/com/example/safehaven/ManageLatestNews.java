package com.example.safehaven;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ManageLatestNews extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView newsImageView;
    private Button btnSelectImage, btnSaveNews;
    private EditText editNewsBody, editVideoLink;
    private ProgressBar progressBar;

    private Uri imageUri;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_latest_news);

        newsImageView = findViewById(R.id.newsImageView);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveNews = findViewById(R.id.btnSaveNews);
        editNewsBody = findViewById(R.id.editNewsBody);
        editVideoLink = findViewById(R.id.editVideoLink);
        progressBar = findViewById(R.id.progressBar);

        storageRef = FirebaseStorage.getInstance().getReference("LatestNews");
        databaseRef = FirebaseDatabase.getInstance().getReference("LatestNews");

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        btnSaveNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNews();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            newsImageView.setImageURI(imageUri);
        }
    }

    private void saveNews() {
        final String newsBody = editNewsBody.getText().toString().trim();
        final String videoLink = editVideoLink.getText().toString().trim();

        if (newsBody.isEmpty()) {
            editNewsBody.setError("News body required");
            editNewsBody.requestFocus();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();

                                String key = databaseRef.push().getKey();
                                Map<String, Object> newsData = new HashMap<>();
                                newsData.put("imageUrl", imageUrl);
                                newsData.put("newsBody", newsBody);
                                newsData.put("videoLink", videoLink.isEmpty() ? null : videoLink);

                                if (key != null) {
                                    databaseRef.child(key).setValue(newsData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(ManageLatestNews.this, "News uploaded successfully", Toast.LENGTH_SHORT).show();
                                                    editNewsBody.setText("");
                                                    editVideoLink.setText("");
                                                    newsImageView.setImageResource(android.R.color.darker_gray);
                                                    imageUri = null;
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(ManageLatestNews.this, "Failed to upload news", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageLatestNews.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                });
    }
}
