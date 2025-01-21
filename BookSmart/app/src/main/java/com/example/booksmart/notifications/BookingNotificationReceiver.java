package com.example.booksmart.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.example.booksmart.BookingDetailsActivity;
import com.example.booksmart.R;

/**
 * BroadcastReceiver for handling booking reminder notifications.
 * Receives scheduled notification intents and displays them to the user.
 */
public class BookingNotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "BookingReminders";

    /**
     * Handles the received notification intent and creates the notification
     * 
     * @param context Application context
     * @param intent  Intent containing booking details
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String bookingId = intent.getStringExtra("bookingId");
        String spaceType = intent.getStringExtra("spaceType");
        String startTime = intent.getStringExtra("startTime");

        // Create intent for notification tap action
        Intent notificationIntent = new Intent(context, BookingDetailsActivity.class);
        notificationIntent.putExtra("bookingId", bookingId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Upcoming Booking Reminder")
                .setContentText("Your " + spaceType + " booking starts at " + startTime)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(bookingId.hashCode(), builder.build());
    }
}