package com.dgo.habitherov4.ui.dashboard;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        // Modificar el listener del botón de añadir
        FloatingActionButton addButton = binding.addMissionButton;
        addButton.setOnClickListener(v -> {
        // Mostrar diálogo para añadir una nueva misión
        showAddMissionDialog();
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

    private void showAddMissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_mission, null);
        builder.setView(dialogView);
    
        // Referencias a los campos del diálogo
        TextInputEditText titleInput = dialogView.findViewById(R.id.edit_mission_title);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.edit_mission_description);
        
        // Referencias a los componentes
        ChipGroup categoryChipGroup = dialogView.findViewById(R.id.category_chip_group);
        ChipGroup difficultyChipGroup = dialogView.findViewById(R.id.difficulty_chip_group);
        Spinner missionTypeSpinner = dialogView.findViewById(R.id.mission_type_spinner);
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        
        // Configurar spinner de tipo de misión
        ArrayAdapter<CharSequence> typeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Principal", "Secundaria", "Diaria"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        missionTypeSpinner.setAdapter(typeAdapter);
        
        // Configurar TimePicker
        timePicker.setIs24HourView(true);
        timePicker.setHour(1); // Valor por defecto: 1 hora
        timePicker.setMinute(0); // Valor por defecto: 0 minutos
        
        // Seleccionar el primer chip por defecto
        if (categoryChipGroup.getChildCount() > 0) {
            ((Chip) categoryChipGroup.getChildAt(0)).setChecked(true);
        }
        if (difficultyChipGroup.getChildCount() > 0) {
            ((Chip) difficultyChipGroup.getChildAt(0)).setChecked(true);
        }
    
        // Configurar botones del diálogo
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            // Validar campos
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            
            // Obtener categoría seleccionada
            String category = "";
            int checkedChipId = categoryChipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID) {
                Chip selectedChip = dialogView.findViewById(checkedChipId);
                category = selectedChip.getText().toString();
            }
            
            // Obtener dificultad seleccionada
            String difficulty = "";
            int checkedDifficultyId = difficultyChipGroup.getCheckedChipId();
            if (checkedDifficultyId != View.NO_ID) {
                Chip selectedDifficultyChip = dialogView.findViewById(checkedDifficultyId);
                String difficultyText = selectedDifficultyChip.getText().toString();
                // Extraer solo la palabra de dificultad (sin el XP)
                if (difficultyText.contains("Fácil")) {
                    difficulty = "Fácil";
                } else if (difficultyText.contains("Medio")) {
                    difficulty = "Medio";
                } else if (difficultyText.contains("Difícil")) {
                    difficulty = "Difícil";
                }
            }
            
            // Obtener tipo de misión seleccionado
            String missionType = missionTypeSpinner.getSelectedItem().toString();
            
            // Obtener tiempo del TimePicker (convertir a minutos totales)
            int hours = timePicker.getHour();
            int minutes = timePicker.getMinute();
            int totalMinutes = (hours * 60) + minutes;

            if (title.isEmpty() || description.isEmpty() || category.isEmpty() || difficulty.isEmpty()) {
                Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (totalMinutes == 0) {
                Toast.makeText(getContext(), "Por favor selecciona un tiempo válido", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determinar el iconType basado en la categoría
            String iconType = "default";
            if (category.equals("Economía")) {
                iconType = "finanzas";
            } else if (category.equals("Salud")) {
                iconType = "salud";
            } else if (category.equals("Académico")) {
                iconType = "academico";
            }

            Mission newMission = new Mission(
                    UUID.randomUUID().toString(),
                    title,
                    description,
                    missionType,
                    false,
                    0,
                    1, // maxProgress siempre es 1 para seguimiento simple
                    0, // La experiencia se asignará automáticamente
                    iconType,
                    difficulty
            );
            
            // Asignar la dificultad (esto automáticamente asignará la experiencia)
            newMission.setDifficulty(difficulty);
            
            // Aquí podrías guardar también el tiempo si lo necesitas en el modelo Mission
            // Por ejemplo: newMission.setTimeLimit(totalMinutes);

            // Guardar la misión usando el ViewModel
            dashboardViewModel.addMission(newMission);
            Toast.makeText(getContext(), "Misión añadida correctamente", Toast.LENGTH_SHORT).show();
        });
    
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
    
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}