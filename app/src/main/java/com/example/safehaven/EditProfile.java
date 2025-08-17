package com.example.safehaven;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfile extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private Spinner districtSpinner;
    private Button backButton, saveButton;

    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", null); // store userId after registration

        if (userId == null) {
            Toast.makeText(this, "No user found! Please register first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        districtSpinner = findViewById(R.id.districtSpinner);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // districts
        String[] districts = {
                "Colombo", "Gampaha", "Kalutara", "Kandy", "Matale", "Nuwara Eliya",
                "Galle", "Matara", "Hambantota", "Jaffna", "Kilinochchi", "Mannar",
                "Vavuniya", "Mullaitivu", "Batticaloa", "Ampara", "Trincomalee",
                "Kurunegala", "Puttalam", "Anuradhapura", "Polonnaruwa",
                "Badulla", "Monaragala", "Ratnapura", "Kegalle"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, districts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(adapter);

        // Load user data
        loadUserData();

        backButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        nameInput.setText(user.name);
                        emailInput.setText(user.email);
                        phoneInput.setText(user.phone);

                        int position = ((ArrayAdapter) districtSpinner.getAdapter()).getPosition(user.district);
                        districtSpinner.setSelection(position);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditProfile.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String district = districtSpinner.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("^07\\d{8}$")) {
            Toast.makeText(this, "Enter valid Sri Lankan mobile number (07XXXXXXXX)", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User(name, email, phone, district);
        databaseReference.setValue(updatedUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfile.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfile.this, "Failed to update profile!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static class User {
        public String name, email, phone, district;
        public User() {}
        public User(String name, String email, String phone, String district) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.district = district;
        }
    }
}
