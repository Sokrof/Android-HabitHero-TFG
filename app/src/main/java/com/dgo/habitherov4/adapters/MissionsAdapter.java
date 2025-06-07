package com.dgo.habitherov4.adapters;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.dgo.habitherov4.R;
import com.dgo.habitherov4.models.Mission;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MissionsAdapter extends RecyclerView.Adapter<MissionsAdapter.MissionViewHolder> {
    
    private List<Mission> missions;
    private OnMissionClickListener listener;
    
    public interface OnMissionClickListener {
        void onMissionClick(Mission mission);
        void onEditMissionClick(Mission mission);
    }
    
    public MissionsAdapter(List<Mission> missions, OnMissionClickListener listener) {
        this.missions = missions;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mission, parent, false);
        return new MissionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        Mission mission = missions.get(position);
        holder.bind(mission);
    }
    
    @Override
    public void onViewRecycled(@NonNull MissionViewHolder holder) {
        super.onViewRecycled(holder);
        holder.stopCountdown();
    }
    
    @Override
    public int getItemCount() {
        return missions != null ? missions.size() : 0;
    }
    
    public void updateMissions(List<Mission> newMissions) {
        this.missions = newMissions;
        notifyDataSetChanged();
    }
    
    class MissionViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconImageView;
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView categoryTextView;
        private TextView expTextView;
        private LinearLayout cardContainer;
        private Handler handler;
        private Runnable countdownRunnable;
        
        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.mission_icon);
            titleTextView = itemView.findViewById(R.id.mission_title);
            descriptionTextView = itemView.findViewById(R.id.mission_description);
            categoryTextView = itemView.findViewById(R.id.mission_category);
            expTextView = itemView.findViewById(R.id.mission_exp);
            cardContainer = itemView.findViewById(R.id.mission_card_container);
            handler = new Handler(Looper.getMainLooper());
            
            // Click en toda la card para mostrar el alert dialog
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onMissionClick(missions.get(getAdapterPosition()));
                }
            });
        }
        
        public void bind(Mission mission) {
            titleTextView.setText(mission.getTitle());
            descriptionTextView.setText(mission.getDescription());
            categoryTextView.setText(mission.getCategory());
            
            // Detener contador anterior si existe
            if (countdownRunnable != null) {
                handler.removeCallbacks(countdownRunnable);
            }
            
            // Verificar si la misión está completada
            if (mission.isCompleted()) {
                expTextView.setText("COMPLETADO");
                expTextView.setTextColor(Color.GREEN);
                expTextView.setBackgroundResource(R.drawable.timer_background);
            } else {
                // Iniciar contador de tiempo
                startCountdown(mission);
            }
            
            // Configurar background según dificultad
            String difficulty = mission.getDifficulty();
            if (difficulty != null) {
                switch (difficulty.toLowerCase()) {
                    case "easy":
                    case "fácil":
                    case "facil":
                        cardContainer.setBackgroundResource(R.drawable.bronce);
                        break;
                    case "normal":
                    case "medio":
                        cardContainer.setBackgroundResource(R.drawable.plata);
                        break;
                    case "hard":
                    case "difícil":
                    case "dificil":
                        cardContainer.setBackgroundResource(R.drawable.oro);
                        break;
                    default:
                        cardContainer.setBackgroundResource(R.drawable.mission_card_background);
                        break;
                }
            }
            
            // Configurar icono según la categoría o tipo de misión
            if (mission.isDailyMission()) {
                iconImageView.setImageResource(R.drawable.ic_reloj);
            } else {
                switch (mission.getCategory().toLowerCase()) {
                    case "académico":
                    case "academico":
                        iconImageView.setImageResource(R.drawable.ic_academico);
                        break;
                    case "salud":
                        iconImageView.setImageResource(R.drawable.ic_salud);
                        break;
                    case "economía":
                    case "economia":
                    case "finanzas":
                        iconImageView.setImageResource(R.drawable.ic_finanzas);
                        break;
                    case "aventura":
                        iconImageView.setImageResource(R.drawable.ic_aventura);
                        break;
                    case "diaria":
                        iconImageView.setImageResource(R.drawable.ic_reloj);
                        break;
                    default:
                        iconImageView.setImageResource(R.drawable.ic_default_mission);
                        break;
                }
            }
            
            // Cambiar apariencia si está completado
            if (mission.isCompleted()) {
                itemView.setAlpha(0.7f);
            } else {
                itemView.setAlpha(1.0f);
            }
        }
        
        private void startCountdown(Mission mission) {
            countdownRunnable = new Runnable() {
                @Override
                public void run() {
                    // Verificar si la misión se completó durante el contador
                    if (mission.isCompleted()) {
                        expTextView.setText("COMPLETADO");
                        expTextView.setTextColor(Color.GREEN);
                        expTextView.setBackgroundResource(R.drawable.timer_background);
                        return;
                    }
                    
                    long currentTime = System.currentTimeMillis();
                    long timeRemaining = mission.getDeadlineTimestamp() - currentTime;
                    
                    if (timeRemaining <= 0) {
                        // Tiempo agotado - cancelar misión
                        mission.setExpired(true);
                        expTextView.setText("EXPIRADO");
                        expTextView.setTextColor(Color.RED);
                        expTextView.setBackgroundResource(R.drawable.timer_background);
                        return;
                    }
                    
                    // Convertir tiempo restante a formato legible
                    String timeText = formatTimeRemaining(timeRemaining);
                    expTextView.setText(timeText);
                    expTextView.setBackgroundResource(R.drawable.timer_background);
                    
                    // Cambiar color según el tiempo restante
                    if (timeRemaining < TimeUnit.HOURS.toMillis(1)) {
                        // Menos de 1 hora - rojo
                        expTextView.setTextColor(Color.RED);
                    } else {
                        // Más de 1 hora - amarillo
                        expTextView.setTextColor(Color.YELLOW);
                    }
                    
                    // Programar siguiente actualización en 1 segundo
                    handler.postDelayed(this, 1000);
                }
            };
            
            // Iniciar el contador
            handler.post(countdownRunnable);
        }
        
        private String formatTimeRemaining(long timeInMillis) {
            long days = TimeUnit.MILLISECONDS.toDays(timeInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60;
            
            if (days > 0) {
                return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
            } else if (hours > 0) {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%02d:%02d", minutes, seconds);
            }
        }
        
        public void stopCountdown() {
            if (countdownRunnable != null) {
                handler.removeCallbacks(countdownRunnable);
            }
        }
    }
}