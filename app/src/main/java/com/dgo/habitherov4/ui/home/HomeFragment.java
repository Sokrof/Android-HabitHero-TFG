package com.dgo.habitherov4.ui.home;

import android.content.Intent;
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
import com.dgo.habitherov4.R;
import com.dgo.habitherov4.adapters.MissionsAdapter;
import com.dgo.habitherov4.databinding.FragmentHomeBinding;
import com.dgo.habitherov4.models.Mission;
import com.dgo.habitherov4.models.User;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements MissionsAdapter.OnMissionClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private MissionsAdapter missionsAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        observeViewModel();
        setupCharacterAnimation();

        return root;
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
                missionsAdapter.updateMissions(missions);
                Log.d("HomeFragment", "Missions updated: " + missions.size());
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
        
        Log.d("HomeFragment", "=== STAT BARS UPDATE COMPLETE ===");
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
                            "¡Misión completada! +" + manaReward + " MP, +1 EXP", 
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