package com.example.booksmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import androidx.appcompat.app.AppCompatActivity;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextView bookingInfoText;
    private MaterialButton editBookingButton, cancelBookingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        bookingInfoText = findViewById(R.id.bookingInfoText);
        editBookingButton = findViewById(R.id.editBookingButton);
        cancelBookingButton = findViewById(R.id.cancelBookingButton);

        // Receive data from MainActivity
        Intent intent = getIntent();
        String room = intent.getStringExtra("room");
        String userName = intent.getStringExtra("userName");
        String startTime = intent.getStringExtra("startTime");
        String endTime = intent.getStringExtra("endTime");

        // Display the received booking information
        bookingInfoText.setText(String.format("Room: %s\nUser: %s\nStart: %s\nEnd: %s", room, userName, startTime, endTime));

        // Handle Edit Button Click
        editBookingButton.setOnClickListener(v -> {
            // Code to edit the booking (can navigate back to MainActivity or open edit dialog)
            Toast.makeText(this, "Edit booking feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Handle Cancel Button Click
        cancelBookingButton.setOnClickListener(v -> {
            // Code to cancel the booking (reset or remove the booking data)
            Toast.makeText(this, "Booking cancelled successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Return to the previous screen
        });
    }
}
