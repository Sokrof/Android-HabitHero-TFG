package com.dgo.habitherov4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dgo.habitherov4.R;
import com.dgo.habitherov4.models.Reward;

import java.util.List;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.RewardViewHolder> {

    private List<Reward> rewards;
    private OnRewardClickListener onRewardClickListener;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward, int position);
    }

    public RewardsAdapter(List<Reward> rewards) {
        this.rewards = rewards;
    }

    public void setOnRewardClickListener(OnRewardClickListener listener) {
        this.onRewardClickListener = listener;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewards.get(position);
        holder.titleTextView.setText(reward.getTitle());
        holder.descriptionTextView.setText(reward.getDescription());
        
        // Cambiar apariencia si está reclamada
        if (reward.isClaimed()) {
            holder.itemView.setAlpha(0.6f);
            holder.titleTextView.setTextColor(holder.itemView.getContext().getColor(android.R.color.darker_gray));
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.titleTextView.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
        }

        // Configurar clic en el item
        holder.itemView.setOnClickListener(v -> {
            if (onRewardClickListener != null) {
                onRewardClickListener.onRewardClick(reward, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.reward_title);
            descriptionTextView = itemView.findViewById(R.id.reward_description);
        }
    }
}