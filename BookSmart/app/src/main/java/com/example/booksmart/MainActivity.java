package com.example.booksmart;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;

/**
 * Entry point activity for the application.
 * Handles initial authentication check and navigation.
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        
        // Find the Get Started button
        MaterialButton getStartedButton = findViewById(R.id.getStartedButton);
        
        // Set click listener
        getStartedButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BookingPageActivity.class));
        });
    }
}