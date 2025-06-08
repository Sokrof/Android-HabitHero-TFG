package com.dgo.habitherov4.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dgo.habitherov4.R;
import com.dgo.habitherov4.models.InventoryReward;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private List<InventoryReward> inventoryItems;
    private OnInventoryItemClickListener listener;

    public interface OnInventoryItemClickListener {
        void onInventoryItemClick(InventoryReward item);
    }

    public InventoryAdapter(List<InventoryReward> inventoryItems, OnInventoryItemClickListener listener) {
        this.inventoryItems = inventoryItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryReward item = inventoryItems.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        
        // Formatear fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateText = "Obtenido: " + sdf.format(item.getObtainedAt().toDate());
        holder.dateTextView.setText(dateText);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInventoryItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dateTextView;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.inventory_item_title);
            descriptionTextView = itemView.findViewById(R.id.inventory_item_description);
            dateTextView = itemView.findViewById(R.id.inventory_item_date);
        }
    }
}