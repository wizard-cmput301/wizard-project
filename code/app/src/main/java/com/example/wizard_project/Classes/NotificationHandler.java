package com.example.wizard_project.Classes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.example.wizard_project.MainActivity;
import com.example.wizard_project.R;

import java.util.Random;

/**
 * NotificationHandler Handles the creation of System notifications
 *  createNotificationChannel - Sets up the app with the system to be able to send notifcations
 *  sendNotification - Handles the display of notifications
 */
public class NotificationHandler {

    private static final String CHANNEL_ID = "high_priority_channel";

    private final Context context;


    public NotificationHandler(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = "Default Channel";
            String channelDescription = "Notifications that pop up on screen";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    /**
     * Displays a system Notification with message, title and app Icon
     *
     * @param title A callback that will be executed once the user is available.
     * @param message A callback that will be executed once the user is available.
     */
    public void sendNotification(String title, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.event_wizard_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Use a unique ID for each notification
            int notificationId = new Random().nextInt(100000); // Generate a random ID
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
