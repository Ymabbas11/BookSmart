package com.example.booksmart.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.booksmart.BookingDetailsActivity;
import com.example.booksmart.R;
import com.example.booksmart.models.Booking;

/**
 * Helper class for managing booking notifications.
 * Handles notification channel creation and scheduling of booking reminders.
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "BookingReminders";
    private static final String CHANNEL_NAME = "Booking Reminders";
    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Creates notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for upcoming bookings");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Schedules a reminder notification for an upcoming booking
     * 
     * @param booking The booking to create a reminder for
     */
    public void scheduleBookingReminder(Booking booking) {
        try {
            // Schedule notification 30 minutes before booking
            long bookingTime = booking.getStartTimeMillis();
            long reminderTime = bookingTime - (30 * 60 * 1000); // 30 minutes before

            Intent intent = new Intent(context, BookingNotificationReceiver.class);
            intent.putExtra("bookingId", booking.getBookingId());
            intent.putExtra("spaceType", booking.getSpaceType());
            intent.putExtra("startTime", booking.getStartTime());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    booking.getBookingId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}