package com.example.booksmart;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.ImageButton;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.booksmart.adapters.TemplatesAdapter;
import com.example.booksmart.models.BookingTemplate;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * Activity for managing space bookings.
 * Handles the main booking interface and space selection.
 */
public class BookingPageActivity extends AppCompatActivity {

    // UI Components
    private MaterialButton submitButton;
    private Calendar startTime, endTime;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private String selectedSpace;
    private BottomNavigationView bottomNav;
    private RecyclerView templatesRecyclerView;
    private TemplatesAdapter templatesAdapter;
    private List<BookingTemplate> templatesList;
    private TextView noTemplatesText;
    private DatabaseReference templatesRef;
    private MaterialButton savedTemplatesButton;
    private BottomSheetDialog templatesBottomSheet;
    private BroadcastReceiver logoutReceiver;
    private ValueEventListener templatesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_page);
        
        // Initialize Firebase services
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        templatesRef = FirebaseDatabase.getInstance().getReference("templates");

        // Initialize lists and adapters
        templatesList = new ArrayList<>();
        templatesAdapter = new TemplatesAdapter(this, templatesList);

        // Initialize views
        savedTemplatesButton = findViewById(R.id.savedTemplatesButton);
        
        // Verify user authentication
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLoginActivity();
            return;
        }

        setupSpaceSelection();
        setupBottomNavigation();
        setupTemplatesBottomSheet();
        loadUserTemplates();
        setupTooltips();
        registerLogoutReceiver();
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
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

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void navigateToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupSpaceSelection() {
        // Find all quick book buttons
        MaterialButton quickBookConference = findViewById(R.id.quickBookConference);
        MaterialButton quickBookMeeting = findViewById(R.id.quickBookMeeting);
        MaterialButton quickBookEvent = findViewById(R.id.quickBookEvent);
        MaterialButton quickBookStudy = findViewById(R.id.quickBookStudy);
        MaterialButton quickBookRecreational = findViewById(R.id.quickBookRecreational);
        MaterialButton quickBookWorkshop = findViewById(R.id.quickBookWorkshop);

        // Set up click listeners for quick book buttons
        quickBookConference.setOnClickListener(v -> {
            selectedSpace = "Conference Room";
            navigateToBookingConfirmation();
        });

        quickBookMeeting.setOnClickListener(v -> {
            selectedSpace = "Meeting Room";
            navigateToBookingConfirmation();
        });

        quickBookEvent.setOnClickListener(v -> {
            selectedSpace = "Event Hall";
            navigateToBookingConfirmation();
        });

        quickBookStudy.setOnClickListener(v -> {
            selectedSpace = "Study Room";
            navigateToBookingConfirmation();
        });

        quickBookRecreational.setOnClickListener(v -> {
            selectedSpace = "Recreational Space";
            navigateToBookingConfirmation();
        });

        quickBookWorkshop.setOnClickListener(v -> {
            selectedSpace = "Workshop Space";
            navigateToBookingConfirmation();
        });
    }

    private void navigateToBookingConfirmation() {
        Intent intent = new Intent(this, BookingConfirmationActivity.class);
        intent.putExtra("spaceType", selectedSpace);
        startActivity(intent);
    }

    private void setupTemplatesBottomSheet() {
        templatesBottomSheet = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_templates, null);
        templatesBottomSheet.setContentView(bottomSheetView);

        RecyclerView templatesRecyclerView = bottomSheetView.findViewById(R.id.templatesRecyclerView);
        noTemplatesText = bottomSheetView.findViewById(R.id.noTemplatesText);

        templatesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        templatesRecyclerView.setAdapter(templatesAdapter);

        savedTemplatesButton.setOnClickListener(v -> templatesBottomSheet.show());
    }

    private void loadUserTemplates() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        // Remove any existing listener first
        if (templatesListener != null) {
            templatesRef.removeEventListener(templatesListener);
        }

        templatesListener = templatesRef.orderByChild("userId").equalTo(currentUser.getUid())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Only process if activity is not finishing
                    if (!isFinishing()) {
                        templatesList.clear();
                        for (DataSnapshot templateSnapshot : snapshot.getChildren()) {
                            BookingTemplate template = templateSnapshot.getValue(BookingTemplate.class);
                            if (template != null) {
                                templatesList.add(template);
                            }
                        }
                        
                        if (templatesList.isEmpty()) {
                            noTemplatesText.setVisibility(View.VISIBLE);
                        } else {
                            noTemplatesText.setVisibility(View.GONE);
                        }
                        
                        templatesAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!isFinishing()) {
                        Toast.makeText(BookingPageActivity.this,
                            "Error loading templates: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void setupTooltips() {
        // Conference Room
        ImageButton conferenceInfoButton = findViewById(R.id.conferenceInfoButton);
        conferenceInfoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Conference Room Features")
                .setMessage("• Full HD projector and screen\n" +
                           "• Professional sound system\n" +
                           "• Video conferencing equipment\n" +
                           "• Adjustable lighting\n" +
                           "• Complimentary water service\n" +
                           "• Available 8 AM - 8 PM")
                .setPositiveButton("Got it", null)
                .show();
        });

        // Meeting Room
        ImageButton meetingInfoButton = findViewById(R.id.meetingInfoButton);
        meetingInfoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Meeting Room Features")
                .setMessage("• Interactive whiteboard\n" +
                           "• Built-in charging stations\n" +
                           "• Privacy blinds\n" +
                           "• Ergonomic seating\n" +
                           "• Coffee and tea service\n" +
                           "• Available 24/7")
                .setPositiveButton("Got it", null)
                .show();
        });

        // Event Hall
        ImageButton eventInfoButton = findViewById(R.id.eventInfoButton);
        eventInfoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Event Hall Features")
                .setMessage("• Stage with lighting\n" +
                           "• Professional PA system\n" +
                           "• Catering preparation area\n" +
                           "• Flexible seating arrangements\n" +
                           "• Coat check facility\n" +
                           "• Available weekends & evenings")
                .setPositiveButton("Got it", null)
                .show();
        });

        // Study Room
        ImageButton studyInfoButton = findViewById(R.id.studyInfoButton);
        studyInfoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Study Room Features")
                .setMessage("• High-speed Wi-Fi\n" +
                           "• Power outlets at every seat\n" +
                           "• Soundproof walls\n" +
                           "• Adjustable desk lamps\n" +
                           "• Print & scan station\n" +
                           "• 24/7 access for students")
                .setPositiveButton("Got it", null)
                .show();
        });

        // Recreational Space
        ImageButton recreationalInfoButton = findViewById(R.id.recreationalInfoButton);
        recreationalInfoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Recreational Space Features")
                .setMessage("• Gaming consoles (PS5, Xbox)\n" +
                           "• Pool and foosball tables\n" +
                           "• Lounge seating area\n" +
                           "• Snack and drink machines\n" +
                           "• Board game collection\n" +
                           "• Available 10 AM - 10 PM")
                .setPositiveButton("Got it", null)
                .show();
        });

        // Workshop Space
        ImageButton workshopInfoButton = findViewById(R.id.workshopInfoButton);
        workshopInfoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Workshop Space Features")
                .setMessage("• Professional tools and equipment\n" +
                           "• Safety gear provided\n" +
                           "• Ventilation system\n" +
                           "• Storage lockers\n" +
                           "• First aid station\n" +
                           "• Available 9 AM - 6 PM")
                .setPositiveButton("Got it", null)
                .show();
        });
    }

    private void registerLogoutReceiver() {
        logoutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.booksmart.LOGOUT".equals(intent.getAction())) {
                    // Remove listeners before finishing
                    if (templatesListener != null) {
                        templatesRef.removeEventListener(templatesListener);
                        templatesListener = null;
                    }
                    // Dismiss bottom sheet if showing
                    if (templatesBottomSheet != null && templatesBottomSheet.isShowing()) {
                        templatesBottomSheet.dismiss();
                    }
                    finish();
                }
            }
        };
        registerReceiver(logoutReceiver, new IntentFilter("com.example.booksmart.LOGOUT"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listeners
        if (templatesListener != null) {
            templatesRef.removeEventListener(templatesListener);
            templatesListener = null;
        }
        // Unregister receiver
        if (logoutReceiver != null) {
            unregisterReceiver(logoutReceiver);
            logoutReceiver = null;
        }
    }
}
