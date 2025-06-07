package com.dgo.habitherov4.ui.home;

import android.content.Intent;
import android.os.Bundle;
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
            }
        });
        
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Aquí podrías mostrar/ocultar un loading indicator
        });
    }

    @Override
    public void onMissionClick(Mission mission) {
        // Mostrar AlertDialog con opciones "Completar" y "Editar"
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(mission.getTitle())
                .setMessage("¿Qué deseas hacer con esta misión?")
                .setPositiveButton("Completar", (dialog, which) -> {
                    if (!mission.isCompleted()) {
                        homeViewModel.completeMission(mission.getId());
                        Toast.makeText(getContext(), "¡Misión completada! +" + mission.getExpReward() + " EXP", 
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