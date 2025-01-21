package com.example.booksmart.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Model class representing a space booking.
 * Contains booking details including space type, timing, and status.
 */
public class Booking {
    private String bookingId;
    private String userId;
    private String spaceType;
    private String userEmail;
    private String startTime;
    private String endTime;
    private String status;

    /**
     * Required empty constructor for Firebase deserialization
     */
    public Booking() {
    }

    /**
     * Creates a new booking with specified parameters
     * 
     * @param userId    User who created the booking
     * @param spaceType Type of space booked
     * @param userEmail User's email
     * @param startTime Booking start time
     * @param endTime   Booking end time
     */
    public Booking(String userId, String spaceType, String userEmail, String startTime, String endTime) {
        this.userId = userId;
        this.spaceType = spaceType;
        this.userEmail = userEmail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "pending";
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(String spaceType) {
        this.spaceType = spaceType;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Converts the start time string to milliseconds
     * 
     * @return Start time in milliseconds, or 0 if parsing fails
     */
    public long getStartTimeMillis() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = sdf.parse(startTime);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}