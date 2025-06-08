package com.dgo.habitherov4;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dgo.habitherov4.adapters.InventoryAdapter;
import com.dgo.habitherov4.models.InventoryReward;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnInventoryItemClickListener {

    private RecyclerView inventoryRecyclerView;
    private InventoryAdapter inventoryAdapter;
    private List<InventoryReward> inventoryItems;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inventario");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Configurar RecyclerView
        inventoryRecyclerView = findViewById(R.id.inventory_recycler_view);
        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        inventoryItems = new ArrayList<>();
        inventoryAdapter = new InventoryAdapter(inventoryItems, this);
        inventoryRecyclerView.setAdapter(inventoryAdapter);

        // Cargar inventario
        loadInventory();
    }

    private void loadInventory() {
        Log.d("InventoryActivity", "Cargando inventario para usuario: " + currentUserId);
        
        db.collection("users").document(currentUserId)
                .collection("inventory")
                .whereEqualTo("used", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("InventoryActivity", "Documentos encontrados: " + queryDocumentSnapshots.size());
                    inventoryItems.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        InventoryReward item = document.toObject(InventoryReward.class);
                        item.setId(document.getId());
                        inventoryItems.add(item);
                        Log.d("InventoryActivity", "Recompensa cargada: " + item.getTitle());
                    }
                    inventoryAdapter.notifyDataSetChanged();
                    Log.d("InventoryActivity", "Total items en inventario: " + inventoryItems.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("InventoryActivity", "Error al cargar inventario: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar inventario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onInventoryItemClick(InventoryReward item) {
        showUseItemDialog(item);
    }

    private void showUseItemDialog(InventoryReward item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Usar Recompensa")
                .setMessage("¿Deseas usar esta recompensa?\n\n" + item.getTitle() + "\n\n" + item.getDescription())
                .setPositiveButton("Usar", (dialog, which) -> {
                    useItem(item);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void useItem(InventoryReward item) {
        // Marcar como usado en Firestore
        db.collection("users").document(currentUserId)
                .collection("inventory")
                .document(item.getId())
                .update("used", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "¡Recompensa usada!", Toast.LENGTH_SHORT).show();
                    loadInventory(); // Recargar inventario
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al usar recompensa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}