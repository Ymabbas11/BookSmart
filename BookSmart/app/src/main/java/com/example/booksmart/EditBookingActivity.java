package com.example.booksmart;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.textfield.TextInputEditText;
import com.example.booksmart.models.Booking;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for editing existing bookings.
 * Allows users to modify booking times and dates.
 */
public class EditBookingActivity extends AppCompatActivity {
    // UI Components
    private TextInputEditText dateInput, startTimeInput, endTimeInput;
    private MaterialButton confirmEditButton;
    private Calendar selectedDate, startTime, endTime;

    // Firebase components
    private FirebaseAuth firebaseAuth;
    private DatabaseReference bookingsRef;
    private String bookingId;
    private Booking currentBooking;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");

        // Get booking ID from intent
        bookingId = getIntent().getStringExtra("bookingId");

        initializeViews();
        setupCalendars();
        loadCurrentBooking();
        setupInputListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        dateInput = findViewById(R.id.dateInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        confirmEditButton = findViewById(R.id.confirmBookingButton);
        confirmEditButton.setText("Update Booking");
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupCalendars() {
        selectedDate = Calendar.getInstance();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
    }

    private void setupInputListeners() {
        dateInput.setOnClickListener(v -> showDatePicker());
        startTimeInput.setOnClickListener(v -> showTimePicker(true));
        endTimeInput.setOnClickListener(v -> showTimePicker(false));
        confirmEditButton.setOnClickListener(v -> confirmEdit());
    }

    private void loadCurrentBooking() {
        if (bookingId == null) {
            Toast.makeText(this, "Error: Booking ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bookingsRef.child(bookingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentBooking = snapshot.getValue(Booking.class);
                if (currentBooking != null) {
                    updateUIWithBookingData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditBookingActivity.this,
                        "Error loading booking: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void confirmEdit() {
        if (!validateBookingTimes())
            return;

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking availability...");
        progressDialog.show();

        // Check for conflicts
        checkBookingConflicts(startTime, endTime, currentBooking.getSpaceType(), bookingId,
                (hasConflict, conflictTime) -> {
                    progressDialog.dismiss();

                    if (hasConflict) {
                        showConflictDialog(conflictTime);
                    } else {
                        updateBooking();
                    }
                });
    }

    private void showConflictDialog(String conflictTime) {
        new AlertDialog.Builder(this)
                .setTitle("Booking Conflict")
                .setMessage("This space is already booked during " + conflictTime +
                        "\n\nWould you like to:")
                .setPositiveButton("Choose Another Time", (dialog, which) -> {
                    // Reset time inputs
                    startTimeInput.setText("");
                    endTimeInput.setText("");
                })
                .setNeutralButton("Try Another Space", (dialog, which) -> {
                    // Go back to space selection
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateBooking() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        bookingsRef.child(bookingId)
                .child("startTime").setValue(sdf.format(startTime.getTime()))
                .addOnSuccessListener(aVoid -> {
                    bookingsRef.child(bookingId)
                            .child("endTime").setValue(sdf.format(endTime.getTime()))
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Booking updated successfully",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update booking: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker() {
        DatePickerDialog dateDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    startTime.set(year, month, dayOfMonth);
                    endTime.set(year, month, dayOfMonth);
                    updateDateText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dateDialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = isStartTime ? startTime : endTime;

        TimePickerDialog timeDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeText(isStartTime);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timeDialog.show();
    }

    private void updateDateText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dateInput.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void updateTimeText(boolean isStartTime) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (isStartTime) {
            startTimeInput.setText(timeFormat.format(startTime.getTime()));
        } else {
            endTimeInput.setText(timeFormat.format(endTime.getTime()));
        }
    }

    /**
     * Validates the selected booking times
     * 
     * @return true if times are valid, false otherwise
     */
    private boolean validateBookingTimes() {
        if (startTime.after(endTime)) {
            Toast.makeText(this, "Start time must be before end time",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        Calendar now = Calendar.getInstance();
        if (startTime.getTimeInMillis() < now.getTimeInMillis()) {
            Toast.makeText(this, "Cannot book time in the past",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateUIWithBookingData() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            // Parse the start and end times
            Date startDate = inputFormat.parse(currentBooking.getStartTime());
            Date endDate = inputFormat.parse(currentBooking.getEndTime());

            if (startDate != null && endDate != null) {
                // Set the calendar instances
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(endDate);

                startTime.setTime(startDate);
                endTime.setTime(endDate);
                selectedDate.setTime(startDate);

                // Update the UI
                dateInput.setText(dateFormat.format(startDate));
                startTimeInput.setText(timeFormat.format(startDate));
                endTimeInput.setText(timeFormat.format(endDate));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading booking times", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_bookings); // Set current tab to bookings

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BookingPageActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(this, BookingDetailsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    // Add after the existing methods
    interface OnConflictCheckListener {
        void onConflictCheckComplete(boolean hasConflict, String conflictTime);
    }

    /**
     * Checks for booking conflicts with existing bookings
     */
    private void checkBookingConflicts(Calendar startTime, Calendar endTime, String spaceType, String currentBookingId,
            OnConflictCheckListener listener) {
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");

        bookingsRef.orderByChild("spaceType").equalTo(spaceType)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean hasConflict = false;
                        String conflictTime = "";

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Booking existingBooking = snapshot.getValue(Booking.class);
                            // Skip checking against the current booking being edited
                            if (existingBooking != null &&
                                    !existingBooking.getBookingId().equals(currentBookingId) &&
                                    !"cancelled".equals(existingBooking.getStatus())) {
                                try {
                                    Date existingStart = sdf.parse(existingBooking.getStartTime());
                                    Date existingEnd = sdf.parse(existingBooking.getEndTime());

                                    if (existingStart != null && existingEnd != null) {
                                        // Check for overlap
                                        if (!(endTime.getTime().before(existingStart) ||
                                                startTime.getTime().after(existingEnd))) {
                                            hasConflict = true;
                                            conflictTime = String.format("%s - %s",
                                                    existingBooking.getStartTime(),
                                                    existingBooking.getEndTime());
                                            break;
                                        }
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        listener.onConflictCheckComplete(hasConflict, conflictTime);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onConflictCheckComplete(false, "");
                    }
                });
    }
}