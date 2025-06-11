package com.dgo.habitherov4.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.dgo.habitherov4.EditMissionActivity;
import com.dgo.habitherov4.InventoryActivity;
import com.dgo.habitherov4.R;
import com.dgo.habitherov4.adapters.MissionsAdapter;
import com.dgo.habitherov4.databinding.FragmentHomeBinding;
import com.dgo.habitherov4.models.InventoryReward;
import com.dgo.habitherov4.models.Mission;
import com.dgo.habitherov4.models.Reward;
import com.dgo.habitherov4.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.os.Handler;
import android.os.Looper;

public class HomeFragment extends Fragment implements MissionsAdapter.OnMissionClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private MissionsAdapter missionsAdapter;
    private int enemyCurrentHealth = 3;
    private int enemyMaxHealth = 3;
    private int chestCount = 0;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar Firebase y SharedPreferences
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sharedPreferences = requireContext().getSharedPreferences("HabitHero_" + currentUserId, Context.MODE_PRIVATE);

        setupRecyclerView();
        observeViewModel();
        setupCharacterAnimation();
        setupEnemyAttack();
        setupChestClick();
        updateEnemyHealthDisplay();
        loadChestCount();
        setupBagClick();
        loadBagCount();

        return root;
    }

    private void setupChestClick() {
        binding.chestContainer.setOnClickListener(v -> {
            if (chestCount <= 0) {
                Toast.makeText(getContext(), "No tienes cofres disponibles", Toast.LENGTH_SHORT).show();
                return;
            }
            showChestConfirmationDialog();
        });
    }

    private void showChestConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Abrir Cofre")
                .setMessage("¿Deseas abrir un cofre para recibir una recompensa aleatoria?\n\nCofres disponibles: " + chestCount)
                .setPositiveButton("Abrir", (dialog, which) -> {
                    openChest();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void openChest() {
        // Obtener recompensas aleatorias de Firestore
        db.collection("users").document(currentUserId)
                .collection("rewards")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reward> availableRewards = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Reward reward = document.toObject(Reward.class);
                        reward.setId(document.getId());
                        availableRewards.add(reward);
                    }

                    if (availableRewards.isEmpty()) {
                        Toast.makeText(getContext(), "No hay recompensas disponibles. Crea algunas primero.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Seleccionar recompensa aleatoria
                    Random random = new Random();
                    Reward randomReward = availableRewards.get(random.nextInt(availableRewards.size()));

                    // Reducir contador de cofres
                    chestCount--;
                    saveChestCount();
                    updateChestDisplay();

                    // Agregar recompensa al inventario
                    addRewardToInventory(randomReward);

                    // Mostrar recompensa obtenida
                    showRewardDialog(randomReward);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al obtener recompensas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addRewardToInventory(Reward reward) {
        InventoryReward inventoryReward = new InventoryReward(reward.getTitle(), reward.getDescription());
        
        Log.d("HomeFragment", "Guardando recompensa en inventario: " + reward.getTitle());
        Log.d("HomeFragment", "Usuario ID: " + currentUserId);
        Log.d("HomeFragment", "Recompensa used: " + inventoryReward.isUsed());
        
        db.collection("users").document(currentUserId)
                .collection("inventory")
                .add(inventoryReward)
                .addOnSuccessListener(documentReference -> {
                    Log.d("HomeFragment", "Recompensa guardada exitosamente con ID: " + documentReference.getId());
                    loadBagCount(); // Actualizar contador de la mochila
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error al guardar en inventario: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al guardar en inventario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBagClick() {
        binding.bagContainer.setOnClickListener(v -> {
            showBagConfirmationDialog();
        });
    }

    private void showBagConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Abrir Inventario")
                .setMessage("¿Deseas abrir tu inventario para ver y usar tus recompensas?")
                .setPositiveButton("Abrir", (dialog, which) -> {
                    Intent intent = new Intent(getContext(), InventoryActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void loadBagCount() {
        db.collection("users").document(currentUserId)
                .collection("inventory")
                .whereEqualTo("used", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int itemCount = queryDocumentSnapshots.size();
                    updateBagDisplay(itemCount);
                })
                .addOnFailureListener(e -> {
                    updateBagDisplay(0);
                });
    }

    private void updateBagDisplay(int itemCount) {
        binding.bagCounter.setText("Items: " + itemCount);
    }

    private void showRewardDialog(Reward reward) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("¡Recompensa Obtenida!")
                .setMessage("Has recibido:\n\n" + reward.getTitle() + "\n\n" + reward.getDescription())
                .setPositiveButton("¡Genial!", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void loadChestCount() {
        chestCount = sharedPreferences.getInt("chest_count", 0);
        updateChestDisplay();
    }

    private void saveChestCount() {
        sharedPreferences.edit().putInt("chest_count", chestCount).apply();
    }

    private void updateChestDisplay() {
        binding.chestCounter.setText("Cofres: " + chestCount);
        // El contador siempre será visible ahora
    }

    private void setupEnemyAttack() {
        binding.enemyGif.setOnClickListener(v -> {
            showAttackConfirmationDialog();
        });
    }

    private void showAttackConfirmationDialog() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        
        User currentUser = homeViewModel.getCurrentUser().getValue();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Error: No se pudo cargar la información del usuario", Toast.LENGTH_SHORT).show();
            return;
        }
    
        if (currentUser.getCurrentMana() <= 0) {
            Toast.makeText(getContext(), "No tienes suficiente MP para atacar", Toast.LENGTH_SHORT).show();
            return;
        }
    
        if (enemyCurrentHealth <= 0) {
            Toast.makeText(getContext(), "El enemigo ya está derrotado", Toast.LENGTH_SHORT).show();
            return;
        }
    
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Atacar Enemigo")
                    .setMessage("¿Deseas atacar al enemigo?\n\nCosto: 1 MP\nDaño: 1 HP")
                    .setPositiveButton("Atacar", (dialog, which) -> {
                        if (isAdded()) {
                            attackEnemy();
                        }
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        } catch (Exception e) {
            Log.e("HomeFragment", "Error showing attack dialog", e);
        }
    }

    private void attackEnemy() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        
        User currentUser = homeViewModel.getCurrentUser().getValue();
        if (currentUser == null || currentUser.getCurrentMana() <= 0) {
            return;
        }
    
        try {
            // Consumir 1 MP del jugador
            homeViewModel.consumeMana(1);
            
            // Reducir 1 HP del enemigo
            enemyCurrentHealth = Math.max(0, enemyCurrentHealth - 1);
            updateEnemyHealthDisplay();
            
            // Mostrar mensaje de ataque
            Toast.makeText(getContext(), 
                "¡Atacaste al enemigo! -1 MP, Enemigo: " + enemyCurrentHealth + "/" + enemyMaxHealth + " HP", 
                Toast.LENGTH_SHORT).show();
            
            // Verificar si el enemigo fue derrotado
            if (enemyCurrentHealth <= 0) {
                // Agregar cofre por derrotar enemigo
                chestCount++;
                saveChestCount();
                updateChestDisplay();
                
                // Usar un Handler para mostrar el segundo Toast con delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "¡Enemigo derrotado! +2 EXP +1 Cofre", Toast.LENGTH_LONG).show();
                    }
                }, 500);
                
                // Dar recompensa por derrotar al enemigo
                homeViewModel.addExperience(2);
                
                // Resetear enemigo con delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded()) {
                        resetEnemy();
                    }
                }, 1000);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error during attack", e);
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Error durante el ataque", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetEnemy() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        
        enemyCurrentHealth = enemyMaxHealth;
        updateEnemyHealthDisplay();
        Toast.makeText(getContext(), "Un nuevo enemigo ha aparecido", Toast.LENGTH_SHORT).show();
    }

    private void updateEnemyHealthDisplay() {
        if (!isAdded() || binding == null) {
            return;
        }
        
        try {
            String healthText = enemyCurrentHealth + "/" + enemyMaxHealth;
            binding.enemyHealthCounter.setText(healthText);
            
            // Cambiar color del texto según la vida
            if (enemyCurrentHealth <= 0) {
                binding.enemyHealthCounter.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (enemyCurrentHealth == 1) {
                binding.enemyHealthCounter.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                binding.enemyHealthCounter.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "Error updating enemy health display", e);
        }
    }

    private void setupCharacterAnimation() {
        // Cargar el GIF animado del personaje usando Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.personaje)
                .into(binding.characterGif);
        
        // Cargar el GIF animado del enemigo usando Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.enemigo)
                .into(binding.enemyGif);
    }

    private void setupRecyclerView() {
        binding.missionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        missionsAdapter = new MissionsAdapter(new ArrayList<>(), this);
        binding.missionsRecyclerView.setAdapter(missionsAdapter);
    }

    private void observeViewModel() {
        homeViewModel.getMissions().observe(getViewLifecycleOwner(), missions -> {
            if (missions != null) {
                // Filtrar misiones para mostrar solo las NO completadas en el Home
                List<Mission> incompleteMissions = new ArrayList<>();
                for (Mission mission : missions) {
                    if (!mission.isCompleted()) {
                        incompleteMissions.add(mission);
                    }
                }
                missionsAdapter.updateMissions(incompleteMissions);
            }
        });
    

        // OBSERVAR CAMBIOS EN EL USUARIO PARA ACTUALIZAR BARRAS
    homeViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
        if (user != null) {
            Log.d("HomeFragment", "=== USER DATA CHANGED ===");
            Log.d("HomeFragment", "User stats received - HP: " + user.getCurrentHp() + "/" + user.getMaxHp() +
                    ", MP: " + user.getCurrentMana() + "/" + user.getMaxMana() +
                    ", EXP: " + user.getCurrentExp() + "/" + user.getMaxExp());
            updateAllStatBars(user);
        } else {
            Log.w("HomeFragment", "User is null - setting default bars");
            // Si no hay usuario, mostrar barras por defecto
            updateHpBar(5);    // HP completa
            updateManaBar(1);  // MP a 1
            updateExpBar(0);   // EXP a 0
        }
    });

    homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        if (isLoading != null) {
            Log.d("HomeFragment", "Loading state: " + isLoading);
        }
    });
}

    private void updateAllStatBars(User user) {
        Log.d("HomeFragment", "=== UPDATING ALL STAT BARS ===");
        
        int hpLevel = user.getHpBarLevel();
        int manaLevel = user.getManaBarLevel();
        int expLevel = user.getExpBarLevel();
        
        Log.d("HomeFragment", "Calculated bar levels - HP: " + hpLevel + ", MP: " + manaLevel + ", EXP: " + expLevel);
        
        updateHpBar(hpLevel);
        updateManaBar(manaLevel);
        updateExpBar(expLevel);
        updatePlayerLevel(user.getLevel()); // Agregar esta línea
        
        Log.d("HomeFragment", "=== STAT BARS UPDATE COMPLETE ===");
    }

    private void updatePlayerLevel(int level) {
        binding.playerLevelCounter.setText("Nivel: " + level);
        Log.d("HomeFragment", "✓ Player level updated to: " + level);
    }

    private void updateManaBar(int manaLevel) {
        // Asegurar que el nivel esté en el rango válido
        int level = Math.max(0, Math.min(5, manaLevel));
        String drawableName = "mp_" + level;
        
        Log.d("HomeFragment", "Updating mana bar to level: " + level + " (drawable: " + drawableName + ")");
        
        try {
            int resourceId = getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
            if (resourceId != 0) {
                binding.manaBar.setImageResource(resourceId);
                Log.d("HomeFragment", "✓ Mana bar successfully updated to: " + drawableName);
            } else {
                Log.e("HomeFragment", "✗ Mana resource not found: " + drawableName + " - using fallback mp_0");
                binding.manaBar.setImageResource(R.drawable.mp_0);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "✗ Error updating mana bar", e);
            binding.manaBar.setImageResource(R.drawable.mp_0);
        }
    }

    private void updateHpBar(int hpLevel) {
        // Asegurar que el nivel esté en el rango válido
        int level = Math.max(0, Math.min(5, hpLevel));
        String drawableName = "hp_" + level;
        
        Log.d("HomeFragment", "Updating HP bar to level: " + level + " (drawable: " + drawableName + ")");
        
        try {
            int resourceId = getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
            if (resourceId != 0) {
                binding.healthBar.setImageResource(resourceId);
                Log.d("HomeFragment", "✓ HP bar successfully updated to: " + drawableName);
            } else {
                Log.e("HomeFragment", "✗ HP resource not found: " + drawableName + " - using fallback hp_5");
                binding.healthBar.setImageResource(R.drawable.hp_5);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "✗ Error updating HP bar", e);
            binding.healthBar.setImageResource(R.drawable.hp_5);
        }
    }

    private void updateExpBar(int expLevel) {
        // Asegurar que el nivel esté en el rango válido
        int level = Math.max(0, Math.min(5, expLevel));
        String drawableName = "exp_" + level;
        
        Log.d("HomeFragment", "Updating EXP bar to level: " + level + " (drawable: " + drawableName + ")");
        
        try {
            int resourceId = getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
            if (resourceId != 0) {
                binding.experienceBar.setImageResource(resourceId);
                Log.d("HomeFragment", "✓ EXP bar successfully updated to: " + drawableName);
            } else {
                Log.e("HomeFragment", "✗ EXP resource not found: " + drawableName + " - using fallback exp_0");
                binding.experienceBar.setImageResource(R.drawable.exp_0);
            }
        } catch (Exception e) {
            Log.e("HomeFragment", "✗ Error updating EXP bar", e);
            binding.experienceBar.setImageResource(R.drawable.exp_0);
        }
    }

    @Override
    public void onMissionClick(Mission mission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(mission.getTitle())
                .setMessage("¿Qué deseas hacer con esta misión?")
                .setPositiveButton("Completar", (dialog, which) -> {
                    if (!mission.isCompleted()) {
                        int manaReward = mission.getManaReward();
                        Log.d("HomeFragment", "Completing mission: " + mission.getTitle() + " for +" + manaReward + " MP");
                        homeViewModel.completeMission(mission.getId());
                        Toast.makeText(getContext(), 
                            "¡Misión completada! +" + manaReward + " MP", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Esta misión ya está completada", 
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Editar", (dialog, which) -> {
                    onEditMissionClick(mission);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onEditMissionClick(Mission mission) {
        Intent intent = new Intent(getActivity(), EditMissionActivity.class);
        intent.putExtra("mission_id", mission.getId());
        intent.putExtra("mission_title", mission.getTitle());
        intent.putExtra("mission_description", mission.getDescription());
        intent.putExtra("mission_category", mission.getCategory());
        intent.putExtra("mission_difficulty", mission.getDifficulty());
        intent.putExtra("mission_deadline", mission.getDeadlineTimestamp());
        startActivity(intent);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}