package com.example.booksmart;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.booksmart.models.Booking;
import com.example.booksmart.models.BookingTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.text.ParseException;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;
import android.text.InputType;
import java.util.ArrayList;
import java.util.List;

import com.example.booksmart.notifications.NotificationHelper;

/**
 * Activity for confirming and finalizing space bookings.
 * Handles date/time selection and booking submission.
 */
public class BookingConfirmationActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText dateInput, startTimeInput, endTimeInput;
    private MaterialButton confirmBookingButton, saveTemplateButton;
    private Calendar selectedDate, startTime, endTime;

    // Firebase components
    private FirebaseAuth firebaseAuth;
    private DatabaseReference bookingsRef, templatesRef;
    private String selectedSpace;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");
        templatesRef = FirebaseDatabase.getInstance().getReference("templates");

        // Initialize calendars first
        selectedDate = Calendar.getInstance();
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();

        // Initialize UI elements
        initializeViews();

        // Get intent data
        Intent intent = getIntent();
        selectedSpace = intent.getStringExtra("spaceType");

        // Check if coming from template
        if (intent.getBooleanExtra("fromTemplate", false)) {
            loadTemplateFromIntent(intent);
        }

        setupInputListeners();
        setupBottomNavigation();
    }

    private void initializeViews() {
        dateInput = findViewById(R.id.dateInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        confirmBookingButton = findViewById(R.id.confirmBookingButton);
        saveTemplateButton = findViewById(R.id.saveTemplateButton);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupInputListeners() {
        dateInput.setOnClickListener(v -> showDatePicker());
        startTimeInput.setOnClickListener(v -> showTimePicker(true));
        endTimeInput.setOnClickListener(v -> showTimePicker(false));
        confirmBookingButton.setOnClickListener(v -> confirmBooking());
        saveTemplateButton.setOnClickListener(v -> showSaveTemplateDialog());
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

    private void confirmBooking() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validateBookingTimes()) {
            return;
        }

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking availability...");
        progressDialog.show();

        // Check for conflicts
        checkBookingConflicts(startTime, endTime, selectedSpace, (hasConflict, conflictTime) -> {
            progressDialog.dismiss();

            if (hasConflict) {
                showConflictDialog(conflictTime);
            } else {
                // Proceed with booking
                createBooking(currentUser);
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
                    navigateToBookingPage();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createBooking(FirebaseUser currentUser) {
        String bookingId = bookingsRef.push().getKey();
        if (bookingId != null) {
            // Combine date with time
            Calendar bookingStart = (Calendar) selectedDate.clone();
            bookingStart.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
            bookingStart.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));

            Calendar bookingEnd = (Calendar) selectedDate.clone();
            bookingEnd.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
            bookingEnd.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            Booking booking = new Booking(
                    currentUser.getUid(),
                    selectedSpace,
                    currentUser.getEmail(),
                    sdf.format(bookingStart.getTime()),
                    sdf.format(bookingEnd.getTime()));
            booking.setBookingId(bookingId);
            booking.setStatus("confirmed");

            bookingsRef.child(bookingId).setValue(booking)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Booking confirmed successfully",
                                Toast.LENGTH_SHORT).show();
                        navigateToBookingDetails(booking);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save booking: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

            // Schedule notification
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.scheduleBookingReminder(booking);
        }
    }

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

    private void navigateToBookingPage() {
        Intent intent = new Intent(this, BookingPageActivity.class);
        intent.putExtra("spaceType", selectedSpace);
        startActivity(intent);
        finish();
    }

    private void navigateToBookingDetails(Booking booking) {
        Intent intent = new Intent(this, BookingDetailsActivity.class);
        intent.putExtra("bookingId", booking.getBookingId());
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home); // Set the current tab to home

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BookingPageActivity.class));
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(this, BookingDetailsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void checkBookingConflicts(Calendar startTime, Calendar endTime, String spaceType,
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
                            if (existingBooking != null && !"cancelled".equals(existingBooking.getStatus())) {
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

    private void showSaveTemplateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save as Template");

        // Add an EditText for template name
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter template name");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String templateName = input.getText().toString().trim();
            if (!templateName.isEmpty()) {
                saveBookingTemplate(templateName);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void saveBookingTemplate(String templateName) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
            return;

        // Generate key directly under templates node
        String templateId = templatesRef.push().getKey();
        if (templateId == null)
            return;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        BookingTemplate template = new BookingTemplate(
                currentUser.getUid(),
                templateName,
                selectedSpace,
                sdf.format(startTime.getTime()),
                sdf.format(endTime.getTime()));
        template.setTemplateId(templateId);

        // Save directly under templates node
        templatesRef.child(templateId).setValue(template)
                .addOnSuccessListener(
                        aVoid -> Toast.makeText(this, "Template saved successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save template: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    private void showTemplateOptions() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null)
            return;

        templatesRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<BookingTemplate> templates = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            BookingTemplate template = snapshot.getValue(BookingTemplate.class);
                            if (template != null) {
                                templates.add(template);
                            }
                        }

                        if (templates.isEmpty()) {
                            Toast.makeText(BookingConfirmationActivity.this,
                                    "No templates found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String[] templateNames = templates.stream()
                                .map(BookingTemplate::getTemplateName)
                                .toArray(String[]::new);

                        new AlertDialog.Builder(BookingConfirmationActivity.this)
                                .setTitle("Load Template")
                                .setItems(templateNames, (dialog, which) -> {
                                    BookingTemplate selected = templates.get(which);
                                    loadTemplateData(selected);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookingConfirmationActivity.this,
                                "Error loading templates", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTemplateData(BookingTemplate template) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            // Set start time
            Date startDate = timeFormat.parse(template.getStartTime());
            if (startDate != null) {
                startTime.set(Calendar.HOUR_OF_DAY, startDate.getHours());
                startTime.set(Calendar.MINUTE, startDate.getMinutes());
                startTimeInput.setText(template.getStartTime());
            }

            // Set end time
            Date endDate = timeFormat.parse(template.getEndTime());
            if (endDate != null) {
                endTime.set(Calendar.HOUR_OF_DAY, endDate.getHours());
                endTime.set(Calendar.MINUTE, endDate.getMinutes());
                endTimeInput.setText(template.getEndTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading template times", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTemplateFromIntent(Intent intent) {
        String startTimeStr = intent.getStringExtra("startTime");
        String endTimeStr = intent.getStringExtra("endTime");

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            // Set today's date
            selectedDate = Calendar.getInstance();
            dateInput.setText(dateFormat.format(selectedDate.getTime()));

            // Set start time
            startTime = Calendar.getInstance();
            Date parsedStartTime = sdf.parse(startTimeStr);
            if (parsedStartTime != null) {
                // Copy today's date components to startTime
                startTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
                startTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
                startTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
                // Set the time components
                startTime.set(Calendar.HOUR_OF_DAY, parsedStartTime.getHours());
                startTime.set(Calendar.MINUTE, parsedStartTime.getMinutes());
            }
            startTimeInput.setText(startTimeStr);

            // Set end time
            endTime = Calendar.getInstance();
            Date parsedEndTime = sdf.parse(endTimeStr);
            if (parsedEndTime != null) {
                // Copy today's date components to endTime
                endTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
                endTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
                endTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
                // Set the time components
                endTime.set(Calendar.HOUR_OF_DAY, parsedEndTime.getHours());
                endTime.set(Calendar.MINUTE, parsedEndTime.getMinutes());
            }
            endTimeInput.setText(endTimeStr);

            // Set selected space
            selectedSpace = intent.getStringExtra("spaceType");

        } catch (ParseException e) {
            Toast.makeText(this, "Error loading template times", Toast.LENGTH_SHORT).show();
        }
    }

    // Interface for conflict check callback
    interface OnConflictCheckListener {
        void onConflictCheckComplete(boolean hasConflict, String conflictTime);
    }
}