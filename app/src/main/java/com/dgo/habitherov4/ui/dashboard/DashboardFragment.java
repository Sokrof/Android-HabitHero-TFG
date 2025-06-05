package com.dgo.habitherov4.ui.dashboard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        Button selectDeadlineBtn = dialogView.findViewById(R.id.btn_select_deadline);
        TextView selectedDeadlineText = dialogView.findViewById(R.id.tv_selected_deadline);
        
        // Variable para almacenar deadline personalizado
        final long[] customDeadline = {0};
        
        // Configurar spinner de tipo de misión
        ArrayAdapter<CharSequence> typeAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Principal", "Secundaria", "Diaria"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        missionTypeSpinner.setAdapter(typeAdapter);
        
        // Configurar selector de fecha límite específica
        selectDeadlineBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            
            // DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // TimePickerDialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                        getContext(),
                        (timeView, hourOfDay, minute) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.SECOND, 0);
                            
                            customDeadline[0] = calendar.getTimeInMillis();
                            
                            // Formatear y mostrar la fecha seleccionada
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            selectedDeadlineText.setText("Fecha límite: " + sdf.format(calendar.getTime()));
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            
            // No permitir fechas pasadas
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
        
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
                if (difficultyText.contains("Fácil")) {
                    difficulty = "Fácil";
                } else if (difficultyText.contains("Medio")) {
                    difficulty = "Medio";
                } else if (difficultyText.contains("Difícil")) {
                    difficulty = "Difícil";
                }
            }
            
            // Obtener tipo de misión
            String missionType = missionTypeSpinner.getSelectedItem().toString();

            if (title.isEmpty() || description.isEmpty() || category.isEmpty() || 
                difficulty.isEmpty()) {
                Toast.makeText(getContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
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
                    1, // maxProgress siempre es 1
                    0, // La experiencia se asignará automáticamente
                    iconType,
                    difficulty
            );
            
            // Usar deadline personalizado si se seleccionó, sino calcular automáticamente
            if (customDeadline[0] > 0) {
                newMission.setDeadlineTimestamp(customDeadline[0]);
            } else {
                // Establecer valores predeterminados para el tiempo
                newMission.setTimeAmount(1);
                newMission.setTimeUnit("días");
                newMission.calculateDeadline();
            }
            
            // Asignar la dificultad
            newMission.setDifficulty(difficulty);
            
            // Programar alarma/notificación en Firebase
            scheduleFirebaseAlarm(newMission);

            // Guardar la misión
            dashboardViewModel.addMission(newMission);
            Toast.makeText(getContext(), "Misión añadida correctamente", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para programar alarma en Firebase
    private void scheduleFirebaseAlarm(Mission mission) {
        // Aquí implementarías la lógica para programar una alarma/notificación
        // usando Firebase Cloud Functions o Firebase Cloud Messaging
        
        // Ejemplo básico:
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> alarmData = new HashMap<>();
        alarmData.put("missionId", mission.getId());
        alarmData.put("userId", getCurrentUserId()); // Implementar este método
        alarmData.put("deadlineTimestamp", mission.getDeadlineTimestamp());
        alarmData.put("title", mission.getTitle());
        alarmData.put("isActive", true);
        
        db.collection("mission_alarms")
            .document(mission.getId())
            .set(alarmData)
            .addOnSuccessListener(aVoid -> {
                Log.d("MissionAlarm", "Alarma programada correctamente");
            })
            .addOnFailureListener(e -> {
                Log.e("MissionAlarm", "Error al programar alarma", e);
            });
    }

    // Método para validar si una misión puede completarse
    public boolean canCompleteMission(Mission mission) {
        mission.checkIfExpired();
        if (mission.isExpired()) {
            Toast.makeText(getContext(), "Esta misión ha expirado y no puede completarse", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    
    // Método para obtener el ID del usuario actual
    private String getCurrentUserId() {
        // Si usas Firebase Auth:
        // return FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Por ahora, retorna un ID temporal o implementa tu lógica de autenticación
        return "temp_user_id";
    }
}