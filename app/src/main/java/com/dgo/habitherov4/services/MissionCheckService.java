package com.dgo.habitherov4.services;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.dgo.habitherov4.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MissionCheckService extends JobIntentService {
    private static final int JOB_ID = 1000;
    private static final String CHANNEL_ID = "mission_notifications";

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MissionCheckService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        checkExpiredMissions();
    }

    private void checkExpiredMissions() {
        // Consultar Firebase por misiones próximas a expirar
        FirebaseFirestore.getInstance()
                .collection("mission_alarms")
                .whereLessThan("deadlineTimestamp", System.currentTimeMillis() + (24 * 60 * 60 * 1000)) // 24 horas antes
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Enviar notificación
                        String missionTitle = document.getString("title");
                        String missionId = document.getString("missionId");
                        
                        if (missionTitle != null && missionId != null) {
                            showMissionExpirationNotification(missionTitle, missionId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Manejar error
                    android.util.Log.e("MissionCheckService", "Error checking expired missions", e);
                });
    }
    
    private void showMissionExpirationNotification(String missionTitle, String missionId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_misiones)
                .setContentTitle("¡Misión por expirar!")
                .setContentText("La misión '" + missionTitle + "' expira pronto")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        
        // Verificar permisos de notificación antes de mostrar
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(missionId.hashCode(), builder.build());
        }
    }
}