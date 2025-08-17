package com.example.safehaven;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
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

    private EditText emailInput, phoneInput;
    private Button loginButton;
    private TextView registerRedirectButton;

    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Initialize UI
        emailInput = findViewById(R.id.loginEmailInput);
        phoneInput = findViewById(R.id.loginPhoneInput);
        loginButton = findViewById(R.id.loginButton);
        registerRedirectButton = findViewById(R.id.registerRedirectButton);

        // SharedPreferences for session management
        sharedPreferences = getSharedPreferences("SafeHavenPrefs", Context.MODE_PRIVATE);

        // Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Redirect to registration page
        registerRedirectButton.setOnClickListener(v -> {
            startActivity(new Intent(UserLogin.this, UserRegister.class));
        });

        // Login button click
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
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

            // Check credentials in Firebase
            databaseReference.orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                boolean found = false;
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    String dbPhone = userSnapshot.child("phone").getValue(String.class);
                                    if (phone.equals(dbPhone)) {
                                        // Successful login
                                        String userId = userSnapshot.getKey();

                                        // Save login state + userId in SharedPreferences
                                        sharedPreferences.edit()
                                                .putBoolean("isRegistered", true)
                                                .putString("userId", userId)
                                                .apply();

                                        Toast.makeText(UserLogin.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(UserLogin.this, Home.class));
                                        finish();
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
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
        });
    }
}
