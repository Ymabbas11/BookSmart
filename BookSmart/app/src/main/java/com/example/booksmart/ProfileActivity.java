package com.example.booksmart;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activity for managing user profile information.
 * Handles user data display, editing, and logout functionality.
 */
public class ProfileActivity extends AppCompatActivity {
    // UI Components
    private TextView profileName, profileEmail;
    private MaterialButton editProfileButton, logoutButton;
    private BottomNavigationView bottomNav;
    
    // Firebase components
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        if (currentUser == null) {
            navigateToLoginActivity();
            return;
        }

        initializeViews();
        setupBottomNavigation();
        loadUserDataFromDatabase();
        setupButtonListeners();
    }

    private void initializeViews() {
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        editProfileButton = findViewById(R.id.editProfileButton);
        logoutButton = findViewById(R.id.logoutButton);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BookingPageActivity.class));
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(this, BookingDetailsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }

    private void loadUserDataFromDatabase() {
        usersRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    profileName.setText(name != null ? name : "Name not set");
                    profileEmail.setText(email != null ? email : currentUser.getEmail());
                } else {
                    // Fallback to Firebase Auth data if database entry doesn't exist
                    profileName.setText(
                            currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Name not set");
                    profileEmail.setText(currentUser.getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this,
                        "Failed to load user data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonListeners() {
        editProfileButton.setOnClickListener(v -> showEditNameDialog());
        logoutButton.setOnClickListener(v -> {
            navigateToLoginActivity();
        });
    }

    private void navigateToLoginActivity() {
        // First, remove all listeners and cleanup
        if (currentUser != null) {
            // Send broadcast to close other activities
            Intent broadcastIntent = new Intent("com.example.booksmart.LOGOUT");
            sendBroadcast(broadcastIntent);
            
            // Wait briefly for broadcast to be received
            new Handler().postDelayed(() -> {
                // Sign out
                firebaseAuth.signOut();
                
                // Navigate to login with clear flags
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 100); // Small delay to allow broadcast processing
        }
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(profileName.getText().toString());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!TextUtils.isEmpty(newName)) {
                updateUserName(newName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserName(String newName) {
        // Update Firebase Auth Profile
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(aVoid -> {
                        // Update Realtime Database
                        usersRef.child(user.getUid()).child("name").setValue(newName)
                                .addOnSuccessListener(aVoid1 -> {
                                    profileName.setText(newName);
                                    Toast.makeText(ProfileActivity.this,
                                            "Name updated successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
                                        "Failed to update database: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this,
                            "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
        }
    }
}
