package com.dgo.habitherov4;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgo.habitherov4.adapters.RewardsAdapter;
import com.dgo.habitherov4.models.Reward;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RewardsActivity extends AppCompatActivity {
    
    private RecyclerView rewardsRecyclerView;
    private RewardsAdapter rewardsAdapter;
    private List<Reward> rewardsList;
    private FloatingActionButton fabAddReward;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Inicializar vistas
        rewardsRecyclerView = findViewById(R.id.rewards_recycler_view);
        fabAddReward = findViewById(R.id.fab_add_reward);

        // Configurar RecyclerView
        rewardsList = new ArrayList<>();
        rewardsAdapter = new RewardsAdapter(rewardsList);
        rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rewardsRecyclerView.setAdapter(rewardsAdapter);

        // Configurar FAB
        fabAddReward.setOnClickListener(v -> showAddRewardDialog());

        // Cargar recompensas
        loadRewards();
    }

    private void showAddRewardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reward, null);
        builder.setView(dialogView);

        EditText titleEditText = dialogView.findViewById(R.id.edit_reward_title);
        EditText descriptionEditText = dialogView.findViewById(R.id.edit_reward_description);

        builder.setTitle("Crear Nueva Recompensa")
                .setPositiveButton("Crear", (dialog, which) -> {
                    String title = titleEditText.getText().toString().trim();
                    String description = descriptionEditText.getText().toString().trim();

                    if (!title.isEmpty() && !description.isEmpty()) {
                        createReward(title, description);
                    } else {
                        Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void createReward(String title, String description) {
        Reward reward = new Reward(title, description);
        
        db.collection("users")
                .document(currentUserId)
                .collection("rewards")
                .add(reward)
                .addOnSuccessListener(documentReference -> {
                    reward.setId(documentReference.getId());
                    rewardsList.add(0, reward);
                    rewardsAdapter.notifyItemInserted(0);
                    Toast.makeText(this, "Recompensa creada exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al crear recompensa", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadRewards() {
        db.collection("users")
                .document(currentUserId)
                .collection("rewards")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    rewardsList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Reward reward = document.toObject(Reward.class);
                        reward.setId(document.getId());
                        rewardsList.add(reward);
                    }
                    rewardsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar recompensas", Toast.LENGTH_SHORT).show();
                });
    }
}