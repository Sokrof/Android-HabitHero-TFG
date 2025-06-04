package com.dgo.habitherov4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        return missions.size();
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
        private TextView progressTextView;
        private TextView expTextView;
        
        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.mission_icon);
            titleTextView = itemView.findViewById(R.id.mission_title);
            descriptionTextView = itemView.findViewById(R.id.mission_description);
            categoryTextView = itemView.findViewById(R.id.mission_category);
            progressTextView = itemView.findViewById(R.id.mission_progress);
            expTextView = itemView.findViewById(R.id.mission_exp);
            
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
            progressTextView.setText(mission.getProgressText());
            expTextView.setText(mission.getExpReward() + ":00");
            
            // Configurar icono según el tipo
            switch (mission.getIconType()) {
                case "study":
                    iconImageView.setImageResource(R.drawable.ic_study);
                    break;
                case "health":
                    iconImageView.setImageResource(R.drawable.ic_health);
                    break;
                case "food":
                    iconImageView.setImageResource(R.drawable.ic_food);
                    break;
                default:
                    iconImageView.setImageResource(R.drawable.ic_default_mission);
                    break;
            }
            
            // Cambiar apariencia si está completado
            if (mission.isCompleted()) {
                itemView.setAlpha(0.7f);
                progressTextView.setText("Realizado");
            } else {
                itemView.setAlpha(1.0f);
            }
        }
    }
}