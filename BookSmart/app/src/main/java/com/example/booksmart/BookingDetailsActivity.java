package com.example.booksmart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.booksmart.adapters.BookingsAdapter;
import com.example.booksmart.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class BookingDetailsActivity extends AppCompatActivity {

    private RecyclerView bookingsRecyclerView;
    private BookingsAdapter bookingsAdapter;
    private List<Booking> bookingsList;
    private BottomNavigationView bottomNav;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private DatabaseReference bookingsRef;
    private TextView noBookingsText;
    private ValueEventListener bookingsListener;
    private DatabaseReference userBookingsRef;
    private BroadcastReceiver logoutReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        bookingsRef = database.getReference("bookings");

        // Initialize Views
        noBookingsText = findViewById(R.id.noBookingsText);
        bottomNav = findViewById(R.id.bottomNav);

        // Initialize RecyclerView
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingsList = new ArrayList<>();
        bookingsAdapter = new BookingsAdapter(this, bookingsList);
        bookingsRecyclerView.setAdapter(bookingsAdapter);

        // Load bookings
        loadUserBookings();
        setupBottomNavigation();

        // Register logout receiver
        logoutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                removeBookingsListener();
                finish();
            }
        };
        registerReceiver(logoutReceiver, new IntentFilter("com.example.booksmart.LOGOUT"));
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, BookingPageActivity.class));
                return true;
            } else if (itemId == R.id.nav_bookings) {
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
        bottomNav.setSelectedItemId(R.id.nav_bookings);
    }

    private void loadUserBookings() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        
        bookingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookingsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && 
                        booking.getUserId().equals(userId) && 
                        !"cancelled".equalsIgnoreCase(booking.getStatus())) {
                        bookingsList.add(booking);
                    }
                }
                
                // Update UI based on bookings list
                if (bookingsList.isEmpty()) {
                    noBookingsText.setVisibility(View.VISIBLE);
                    bookingsRecyclerView.setVisibility(View.GONE);
                } else {
                    noBookingsText.setVisibility(View.GONE);
                    bookingsRecyclerView.setVisibility(View.VISIBLE);
                }
                
                bookingsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookingDetailsActivity.this,
                        "Error loading bookings: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };
        
        // Listen to all bookings and filter in the ValueEventListener
        bookingsRef.addValueEventListener(bookingsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeBookingsListener();
        try {
            if (logoutReceiver != null) {
                unregisterReceiver(logoutReceiver);
                logoutReceiver = null;
            }
        } catch (IllegalArgumentException e) {
            // Receiver was already unregistered
        }
    }

    private void removeBookingsListener() {
        if (bookingsListener != null && bookingsRef != null) {
            bookingsRef.removeEventListener(bookingsListener);
            bookingsListener = null;
        }
    }
}
