package com.dgo.habitherov4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dgo.habitherov4.R;
import com.dgo.habitherov4.models.Mission;
import java.util.List;

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
        private LinearLayout cardContainer; // Agregar esta línea
        
        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.mission_icon);
            titleTextView = itemView.findViewById(R.id.mission_title);
            descriptionTextView = itemView.findViewById(R.id.mission_description);
            categoryTextView = itemView.findViewById(R.id.mission_category);
            expTextView = itemView.findViewById(R.id.mission_exp);
            cardContainer = itemView.findViewById(R.id.mission_card_container); // Agregar esta línea
            
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
            expTextView.setText(mission.getExpReward() + ":00");
            
            // Configurar background según dificultad
            String difficulty = mission.getDifficulty();
            if (difficulty != null) {
                switch (difficulty.toLowerCase()) {
                    case "easy":
                    case "fácil":
                    case "facil":
                        cardContainer.setBackgroundResource(R.drawable.mission_card_background_easy);
                        break;
                    case "normal":
                    case "medio":
                        cardContainer.setBackgroundResource(R.drawable.mission_card_background_normal);
                        break;
                    case "hard":
                    case "difícil":
                    case "dificil":
                        cardContainer.setBackgroundResource(R.drawable.mission_card_background_hard);
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
    }
}