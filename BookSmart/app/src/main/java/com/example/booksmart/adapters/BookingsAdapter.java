package com.example.booksmart.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booksmart.R;
import com.example.booksmart.models.Booking;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import com.example.booksmart.EditBookingActivity;

/**
 * RecyclerView adapter for displaying booking items.
 * Handles booking item display and interactions.
 */
public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingViewHolder> {
    private List<Booking> bookings;
    private Context context;
    private SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public BookingsAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        
        // Set space type (with null check)
        holder.spaceTypeText.setText(booking.getSpaceType() != null ? booking.getSpaceType() : "N/A");
        
        // Set and style status (with null check)
        String status = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "PENDING";
        holder.statusText.setText(status);
        GradientDrawable statusBackground = new GradientDrawable();
        statusBackground.setCornerRadius(25);
        statusBackground.setColor(getStatusColor(status));
        holder.statusText.setBackground(statusBackground);

        // Handle date and time formatting
        String startTime = booking.getStartTime();
        String endTime = booking.getEndTime();
        
        if (startTime != null && endTime != null) {
            try {
                Date startDate = inputFormat.parse(startTime);
                Date endDate = inputFormat.parse(endTime);

                if (startDate != null && endDate != null) {
                    holder.dateText.setText(dateFormat.format(startDate));
                    String timeRange = String.format("%s - %s",
                            timeFormat.format(startDate),
                            timeFormat.format(endDate));
                    holder.timeText.setText(timeRange);
                } else {
                    setDefaultDateTimeText(holder);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                setDefaultDateTimeText(holder);
            }
        } else {
            setDefaultDateTimeText(holder);
        }

        // Show/hide cancel button based on status
        if ("confirmed".equalsIgnoreCase(status)) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> cancelBooking(booking));
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditBookingActivity.class);
            intent.putExtra("bookingId", booking.getBookingId());
            context.startActivity(intent);
        });
    }

    private void setDefaultDateTimeText(BookingViewHolder holder) {
        holder.dateText.setText("Date not set");
        holder.timeText.setText("Time not set");
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                return Color.parseColor("#4CAF50"); // Green
            case "pending":
                return Color.parseColor("#FFA000"); // Amber
            case "cancelled":
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.parseColor("#757575"); // Grey
        }
    }

    private void cancelBooking(Booking booking) {
        new AlertDialog.Builder(context)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    DatabaseReference bookingRef = FirebaseDatabase.getInstance()
                            .getReference("bookings")
                            .child(booking.getBookingId());

                    bookingRef.child("status").setValue("cancelled")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Booking cancelled successfully", 
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to cancel booking: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView spaceTypeText, statusText, dateText, timeText;
        MaterialButton cancelButton, editButton;

        BookingViewHolder(View itemView) {
            super(itemView);
            spaceTypeText = itemView.findViewById(R.id.spaceTypeText);
            statusText = itemView.findViewById(R.id.statusText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            cancelButton = itemView.findViewById(R.id.cancelButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
} 