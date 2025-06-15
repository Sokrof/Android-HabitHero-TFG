package com.dgo.habitherov4.ui.recompensas;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgo.habitherov4.R;
import com.dgo.habitherov4.adapters.RewardsAdapter;
import com.dgo.habitherov4.databinding.FragmentNotificationsBinding;
import com.dgo.habitherov4.models.Reward;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecompensasFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private FirebaseFirestore db;
    private String currentUserId;
    private List<Reward> rewardsList;
    private List<Reward> filteredRewardsList;
    private RewardsAdapter rewardsAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Inicializar listas y adapter
        rewardsList = new ArrayList<>();
        filteredRewardsList = new ArrayList<>();
        rewardsAdapter = new RewardsAdapter(filteredRewardsList);

        // Configurar RecyclerView
        RecyclerView recyclerView = binding.recyclerRewards;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(rewardsAdapter);

        // Configurar listener para clics en recompensas
        rewardsAdapter.setOnRewardClickListener(this::showRewardOptionsDialog);

        // Configurar FAB para mostrar diálogo de añadir recompensa
        binding.addRewardButton.setOnClickListener(v -> showAddRewardDialog());

        // Configurar búsqueda
        binding.searchRewards.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRewards(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Cargar recompensas existentes
        loadRewards();

        return root;
    }

    private void filterRewards(String query) {
        filteredRewardsList.clear();
        if (query.isEmpty()) {
            filteredRewardsList.addAll(rewardsList);
        } else {
            for (Reward reward : rewardsList) {
                if (reward.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        reward.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredRewardsList.add(reward);
                }
            }
        }
        rewardsAdapter.notifyDataSetChanged();
    }

    // Alerta para la card de recompensas
    private void showRewardOptionsDialog(Reward reward, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(reward.getTitle())
                .setMessage("¿Qué deseas hacer con esta recompensa?")
                .setPositiveButton("Editar", (dialog, which) -> showEditRewardDialog(reward, position))
                .setNegativeButton("Eliminar", (dialog, which) -> showDeleteConfirmationDialog(reward, position))
                .setNeutralButton("Cancelar", null)
                .show();
    }


    // Dialogo de confirmación para eliminar
    private void showDeleteConfirmationDialog(Reward reward, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Eliminar Recompensa")
                .setMessage("¿Estás seguro de que deseas eliminar \"" + reward.getTitle() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteReward(reward, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showAddRewardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_reward, null);
        builder.setView(dialogView);

        // Referencias a los campos del diálogo
        TextInputEditText titleInput = dialogView.findViewById(R.id.edit_reward_title);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.edit_reward_description);
        Button cancelBtn = dialogView.findViewById(R.id.btn_cancel);
        Button saveBtn = dialogView.findViewById(R.id.btn_save);

        AlertDialog dialog = builder.create();

        // Configurar botón cancelar
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // Configurar botón guardar
        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                titleInput.setError("El título es obligatorio");
                return;
            }

            // Guardar la recompensa en Firestore
            saveReward(title, description);

            dialog.dismiss();
        });

        dialog.show();
    }

    // Alerta para EDITAr recompensas
    private void showEditRewardDialog(Reward reward, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_reward, null);
        builder.setView(dialogView);

        // Referencias a los campos del diálogo
        TextInputEditText titleInput = dialogView.findViewById(R.id.edit_reward_title);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.edit_reward_description);
        Button cancelBtn = dialogView.findViewById(R.id.btn_cancel);
        Button saveBtn = dialogView.findViewById(R.id.btn_save);

        // Prellenar con los datos actuales
        titleInput.setText(reward.getTitle());
        descriptionInput.setText(reward.getDescription());

        AlertDialog dialog = builder.create();

        // Configurar botón cancelar
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // Configurar botón guardar
        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                titleInput.setError("El título es obligatorio");
                return;
            }

            // Actualizar la recompensa en Firestore
            updateReward(reward, title, description, position);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveReward(String title, String description) {
        Reward reward = new Reward(title, description);

        db.collection("users")
                .document(currentUserId)
                .collection("rewards")
                .add(reward)
                .addOnSuccessListener(documentReference -> {
                    reward.setId(documentReference.getId());
                    rewardsList.add(0, reward);
                    filterRewards(binding.searchRewards.getText().toString());
                    Toast.makeText(getContext(), "Recompensa creada exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al crear recompensa", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateReward(Reward reward, String newTitle, String newDescription, int position) {
        reward.setTitle(newTitle);
        reward.setDescription(newDescription);

        db.collection("users")
                .document(currentUserId)
                .collection("rewards")
                .document(reward.getId())
                .set(reward)
                .addOnSuccessListener(aVoid -> {
                    // Actualizar en la lista local
                    int originalPosition = rewardsList.indexOf(reward);
                    if (originalPosition != -1) {
                        rewardsList.set(originalPosition, reward);
                    }
                    filterRewards(binding.searchRewards.getText().toString());
                    Toast.makeText(getContext(), "Recompensa actualizada exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al actualizar recompensa", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteReward(Reward reward, int position) {
        db.collection("users")
                .document(currentUserId)
                .collection("rewards")
                .document(reward.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    rewardsList.remove(reward);
                    filterRewards(binding.searchRewards.getText().toString());
                    Toast.makeText(getContext(), "Recompensa eliminada exitosamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al eliminar recompensa", Toast.LENGTH_SHORT).show();
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
                    filterRewards(binding.searchRewards.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar recompensas", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}