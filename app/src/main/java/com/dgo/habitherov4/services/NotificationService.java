package com.dgo.habitherov4.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dgo.habitherov4.R;
import com.dgo.habitherov4.models.Mission;

public class NotificationService {
    private static final String CHANNEL_ID = "mission_notifications";
    
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Misiones";
            String description = "Notificaciones cuando las misiones están por expirar";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public static void showMissionExpirationNotification(Context context, Mission mission) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_misiones)
                .setContentTitle("¡Misión por expirar!")
                .setContentText("La misión '" + mission.getTitle() + "' expira pronto")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(mission.getId().hashCode(), builder.build());
    }
}