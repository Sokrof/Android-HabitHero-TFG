package com.dgo.habitherov4.ui.dashboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgo.habitherov4.R;
import com.dgo.habitherov4.adapters.MissionsAdapter;
import com.dgo.habitherov4.databinding.FragmentDashboardBinding;
import com.dgo.habitherov4.models.Mission;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment implements MissionsAdapter.OnMissionClickListener {

    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private MissionsAdapter missionsAdapter;
    private List<Mission> allMissions;
    private List<Mission> filteredMissions;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar RecyclerView
        RecyclerView recyclerView = binding.missionsRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Inicializar listas
        allMissions = new ArrayList<>();
        filteredMissions = new ArrayList<>();
        
        // Configurar adaptador
        missionsAdapter = new MissionsAdapter(filteredMissions, this);
        recyclerView.setAdapter(missionsAdapter);
        
        // Configurar búsqueda
        EditText searchEditText = binding.searchMissions;
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMissions(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Configurar botón de añadir
        FloatingActionButton addButton = binding.addMissionButton;
        addButton.setOnClickListener(v -> {
            // Aquí se abriría un diálogo para añadir una nueva misión
            Toast.makeText(getContext(), "Añadir nueva misión", Toast.LENGTH_SHORT).show();
        });
        
        // Observar cambios en las misiones
        dashboardViewModel.getMissions().observe(getViewLifecycleOwner(), missions -> {
            allMissions = missions;
            filteredMissions.clear();
            filteredMissions.addAll(missions);
            missionsAdapter.updateMissions(filteredMissions);
            updateMissionCounters();
        });

        return root;
    }
    
    private void filterMissions(String query) {
        filteredMissions.clear();
        if (query.isEmpty()) {
            filteredMissions.addAll(allMissions);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Mission mission : allMissions) {
                if (mission.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                    mission.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                    mission.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    filteredMissions.add(mission);
                }
            }
        }
        missionsAdapter.updateMissions(filteredMissions);
    }
    
    private void updateMissionCounters() {
        // Actualizar contador de misiones activas
        binding.activeMissionsCount.setText(String.valueOf(allMissions.size()));
        
        // Aquí se actualizaría el contador de tiempo de misiones diarias
        // y el estado de completado
        int completedCount = 0;
        for (Mission mission : allMissions) {
            if (mission.isCompleted()) {
                completedCount++;
            }
        }
        
        if (completedCount == allMissions.size() && !allMissions.isEmpty()) {
            binding.dailyMissionsStatus.setText("Has completado tus misiones diarias!");
        } else {
            binding.dailyMissionsStatus.setText("Tienes misiones pendientes");
        }
    }

    @Override
    public void onMissionClick(Mission mission) {
        // Aquí se manejaría el clic en una misión
        Toast.makeText(getContext(), "Misión seleccionada: " + mission.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}