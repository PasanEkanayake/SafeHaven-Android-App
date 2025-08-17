package com.example.safehaven;

import android.content.Context;
import android.content.Intent;
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

public class UserRegister extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private Spinner districtSpinner;
    private Button backButton, nextButton, registerButton, loginButton;

    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);

        // If user already finished intro, skip registration
        boolean introFinished = sharedPreferences.getBoolean("introFinished", false);
        if (introFinished) {
            startActivity(new Intent(UserRegister.this, Home.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_user_register);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        districtSpinner = findViewById(R.id.districtSpinner);
        backButton = findViewById(R.id.backButton);
        nextButton = findViewById(R.id.nextButton);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Districts
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

        backButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isRegistered", false);
            editor.apply();
            startActivity(new Intent(UserRegister.this, introduction.class));
            finish();
        });

        // Login button redirects to login page
        loginButton.setOnClickListener(v -> {
            startActivity(new Intent(UserRegister.this, UserLogin.class));
        });

        // Register button
        registerButton.setOnClickListener(v -> {
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

            // Check if email already exists
            databaseReference.orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(UserRegister.this, "Email already registered! Please login.", Toast.LENGTH_LONG).show();
                            } else {
                                String userId = databaseReference.push().getKey();
                                User user = new User(name, email, phone, district);

                                if (userId != null) {
                                    databaseReference.child(userId).setValue(user)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    sharedPreferences.edit().putBoolean("isRegistered", true).apply();
                                                    sharedPreferences.edit().putString("userId", userId).apply();
                                                    Toast.makeText(UserRegister.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(UserRegister.this, introduction.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(UserRegister.this, "Failed to save data!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(UserRegister.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(UserRegister.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
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
