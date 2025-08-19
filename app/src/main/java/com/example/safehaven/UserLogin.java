package com.example.safehaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserLogin extends AppCompatActivity {

    private EditText emailOrUsernameInput, phoneOrPasswordInput;
    private Button loginButton;
    private TextView registerRedirectButton;

    private DatabaseReference userRef, adminRef;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        emailOrUsernameInput = findViewById(R.id.loginEmailInput);
        phoneOrPasswordInput = findViewById(R.id.loginPhoneInput);
        loginButton = findViewById(R.id.loginButton);
        registerRedirectButton = findViewById(R.id.registerRedirectButton);

        // SharedPreferences for session management
        sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);

        // Firebase references
        userRef = FirebaseDatabase.getInstance().getReference("users");
        adminRef = FirebaseDatabase.getInstance().getReference("adminUsers");

        // Redirect to registration page
        registerRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(UserLogin.this, UserRegister.class));
        });

        // Login button click
        loginButton.setOnClickListener(v -> {
            String inputOne = emailOrUsernameInput.getText().toString().trim();
            String inputTwo = phoneOrPasswordInput.getText().toString().trim();

            if (inputOne.isEmpty() || inputTwo.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- First check adminUsers
            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String dbUsername = snapshot.child("username").getValue(String.class);
                        String dbPassword = snapshot.child("password").getValue(String.class);

                        if (inputOne.equals(dbUsername) && inputTwo.equals(dbPassword)) {
                            // Admin login success
                            sharedPreferences.edit()
                                    .putBoolean("isRegistered", true)
                                    .putBoolean("isAdmin", true)
                                    .putString("userId", "admin") // fixed ID since only one admin
                                    .apply();

                            Toast.makeText(UserLogin.this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserLogin.this, Home.class));
                            finish();
                            return; // Stop here if admin logged in
                        }
                    }

                    // --- If not admin, check regular users by email + phone ---
                    userRef.orderByChild("email").equalTo(inputOne)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        boolean foundUser = false;
                                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                            String dbPhone = userSnapshot.child("phone").getValue(String.class);
                                            if (inputTwo.equals(dbPhone)) {
                                                // Regular user login success
                                                String userId = userSnapshot.getKey();

                                                sharedPreferences.edit()
                                                        .putBoolean("isRegistered", true)
                                                        .putBoolean("isAdmin", false)
                                                        .putString("userId", userId)
                                                        .apply();

                                                Toast.makeText(UserLogin.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(UserLogin.this, Home.class));
                                                finish();
                                                foundUser = true;
                                                break;
                                            }
                                        }
                                        if (!foundUser) {
                                            Toast.makeText(UserLogin.this, "Incorrect phone number", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(UserLogin.this, "No account found with this email", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Toast.makeText(UserLogin.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(UserLogin.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
