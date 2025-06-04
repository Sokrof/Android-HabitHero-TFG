package com.dgo.habitherov4.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

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
        setupObservers();
        setupClickListeners();

        return root;
    }
    
    private void setupRecyclerView() {
        missionsAdapter = new MissionsAdapter(new ArrayList<>(), this);
        binding.missionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.missionsRecyclerView.setAdapter(missionsAdapter);
    }
    
    private void setupObservers() {
        homeViewModel.getCurrentUser().observe(getViewLifecycleOwner(), this::updateUserUI);
        homeViewModel.getMissions().observe(getViewLifecycleOwner(), missions -> {
            if (missions != null) {
                missionsAdapter.updateMissions(missions);
            }
        });
        
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Aquí podrías mostrar/ocultar un loading indicator
        });
    }
    
    private void updateUserUI(User user) {
        if (user != null) {
            binding.userName.setText(user.getName());
            binding.userLevel.setText("Nivel: " + user.getLevel());
            binding.expProgress.setProgress(user.getExpPercentage());
        }
    }
    
    private void setupClickListeners() {
        binding.btnRewards.setOnClickListener(v -> {
            // Navegar a recompensas
            Toast.makeText(getContext(), "Recompensas próximamente", Toast.LENGTH_SHORT).show();
        });
        
        binding.btnMissions.setOnClickListener(v -> {
            // Ya estamos en misiones
        });
    }

    @Override
    public void onMissionClick(Mission mission) {
        if (!mission.isCompleted()) {
            homeViewModel.completeMission(mission.getId());
            Toast.makeText(getContext(), "¡Misión completada! +" + mission.getExpReward() + " EXP", 
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}