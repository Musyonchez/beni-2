package com.usiu.cafeteria;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.usiu.cafeteria.auth.LoginActivity;
import com.usiu.cafeteria.repository.FirestoreRepository;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID   = "cafeteria_notifications";
    private static final String CHANNEL_NAME = "Cafeteria Notifications";

    /** Called when a new FCM token is generated. Save it to the user's Firestore doc. */
    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirestoreRepository.getInstance().updateFcmToken(user.getUid(), token);
        }
    }

    /** Called when a notification arrives while the app is in the foreground. */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() == null) return;
        String title = remoteMessage.getNotification().getTitle();
        String body  = remoteMessage.getNotification().getBody();
        showNotification(title != null ? title : "", body != null ? body : "");
    }

    private void showNotification(String title, String body) {
        ensureChannel();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_nav_orders)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager mgr =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mgr != null) {
            mgr.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void ensureChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager mgr =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mgr != null) mgr.createNotificationChannel(channel);
    }
}
