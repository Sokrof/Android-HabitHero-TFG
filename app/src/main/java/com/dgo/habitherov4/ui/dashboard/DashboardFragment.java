package com.dgo.habitherov4.ui.dashboard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    private MissionsAdapter dailyMissionsAdapter;
    private MissionsAdapter activeMissionsAdapter;
    private List<Mission> allMissions;
    private List<Mission> dailyMissions;
    private List<Mission> activeMissions;

    // Agregar estas nuevas variables para el contador
    private Handler countdownHandler;
    private Runnable countdownRunnable;
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
    
        // Inicializar listas
        allMissions = new ArrayList<>();
        dailyMissions = new ArrayList<>();
        activeMissions = new ArrayList<>();
        
        // Inicializar RecyclerViews después de crear las listas
        setupRecyclerViews();
        
        // Inicializar el contador de misiones diarias
        initializeDailyMissionsCountdown();
        
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
            showAddMissionDialog();
        });
        
        // Observar cambios en las misiones
        dashboardViewModel.getMissions().observe(getViewLifecycleOwner(), missions -> {
            allMissions = missions;
            updateMissionsDisplay();
            updateMissionCounters();
        });

        return root;
    }

    private void setupRecyclerViews() {
        // Configurar RecyclerView para misiones diarias
        RecyclerView dailyRecyclerView = binding.dailyMissionsRecyclerView;
        dailyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyMissionsAdapter = new MissionsAdapter(dailyMissions, this);
        dailyRecyclerView.setAdapter(dailyMissionsAdapter);
        
        // Configurar RecyclerView para misiones activas
        RecyclerView activeRecyclerView = binding.missionsRecyclerView;
        activeRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activeMissionsAdapter = new MissionsAdapter(activeMissions, this);
        activeRecyclerView.setAdapter(activeMissionsAdapter);
    }

    private void updateMissionsDisplay() {
        if (allMissions == null) return;
        
        // Asegurarse de que las listas estén inicializadas
        if (dailyMissions == null) dailyMissions = new ArrayList<>();
        if (activeMissions == null) activeMissions = new ArrayList<>();
        
        dailyMissions.clear();
        activeMissions.clear();
        
        for (Mission mission : allMissions) {
            if ("Diaria".equals(mission.getCategory())) {
                dailyMissions.add(mission);
            } else {
                activeMissions.add(mission);
            }
        }
        
        // Actualizar adaptadores
        dailyMissionsAdapter.updateMissions(dailyMissions);
        activeMissionsAdapter.updateMissions(activeMissions);
        
        // Mostrar/ocultar sección de misiones diarias
        if (dailyMissions.isEmpty()) {
            binding.dailyMissionsSection.setVisibility(View.GONE);
        } else {
            binding.dailyMissionsSection.setVisibility(View.VISIBLE);
        }
    }
    
    private void filterMissions(String query) {
        if (query.isEmpty()) {
            updateMissionsDisplay();
            return;
        }
        
        List<Mission> filteredDaily = new ArrayList<>();
        List<Mission> filteredActive = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();
        
        for (Mission mission : allMissions) {
            if (mission.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                mission.getDescription().toLowerCase().contains(lowerCaseQuery) ||
                mission.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                
                if ("Diaria".equals(mission.getCategory())) {
                    filteredDaily.add(mission);
                } else {
                    filteredActive.add(mission);
                }
            }
        }
        
        // Actualizar listas filtradas
        dailyMissions.clear();
        dailyMissions.addAll(filteredDaily);
        activeMissions.clear();
        activeMissions.addAll(filteredActive);
        
        // Actualizar adaptadores
        dailyMissionsAdapter.updateMissions(dailyMissions);
        activeMissionsAdapter.updateMissions(activeMissions);
        
        // Mostrar/ocultar sección de misiones diarias
        if (dailyMissions.isEmpty()) {
            binding.dailyMissionsSection.setVisibility(View.GONE);
        } else {
            binding.dailyMissionsSection.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateMissionCounters() {
        // Actualizar contador de misiones activas
        binding.activeMissionsCount.setText(String.valueOf(activeMissions.size()));
        
        // Actualizar estado de misiones diarias
        if (dailyMissions.isEmpty()) {
            binding.dailyMissionsStatus.setText("No tienes misiones diarias");
        } else {
            boolean allCompleted = true;
            for (Mission mission : dailyMissions) {
                if (!mission.isCompleted()) {
                    allCompleted = false;
                    break;
                }
            }
            
            if (allCompleted) {
                binding.dailyMissionsStatus.setText("Has completado tus misiones diarias!");
            } else {
                binding.dailyMissionsStatus.setText("Tienes misiones pendientes");
            }
        }
    }

    // Nuevo método para inicializar el contador de misiones diarias
    private void initializeDailyMissionsCountdown() {
        countdownHandler = new Handler(Looper.getMainLooper());
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateDailyMissionsCountdown();
                // Actualizar cada minuto (60000 ms)
                countdownHandler.postDelayed(this, 60000);
            }
        };
        
        // Iniciar el contador inmediatamente
        updateDailyMissionsCountdown();
        // Programar la primera actualización en 1 minuto
        countdownHandler.postDelayed(countdownRunnable, 60000);
    }
    
    // Nuevo método para actualizar el contador de misiones diarias
    private void updateDailyMissionsCountdown() {
        Calendar now = Calendar.getInstance();
        Calendar resetTime = Calendar.getInstance();
        
        // Configurar las 00:01 del día siguiente
        resetTime.add(Calendar.DAY_OF_MONTH, 1);
        resetTime.set(Calendar.HOUR_OF_DAY, 0);
        resetTime.set(Calendar.MINUTE, 1);  // Cambiar a 00:01 en lugar de 00:00
        resetTime.set(Calendar.SECOND, 0);
        resetTime.set(Calendar.MILLISECOND, 0);
        
        long timeUntilReset = resetTime.getTimeInMillis() - now.getTimeInMillis();
        
        // Si el tiempo es negativo o cero, las misiones deben restablecerse
        if (timeUntilReset <= 0) {
            resetDailyMissions();
            // Recalcular para el próximo día
            resetTime.add(Calendar.DAY_OF_MONTH, 1);
            timeUntilReset = resetTime.getTimeInMillis() - now.getTimeInMillis();
        }
        
        // Convertir a horas y minutos
        long hours = timeUntilReset / (1000 * 60 * 60);
        long minutes = (timeUntilReset % (1000 * 60 * 60)) / (1000 * 60);
        
        // Actualizar el TextView
        String countdownText = String.format(Locale.getDefault(), "%d h %d min", hours, minutes);
        binding.dailyMissionsTime.setText(countdownText);
    }
    
    // Nuevo método para restablecer las misiones diarias
    private void resetDailyMissions() {
        if (allMissions != null) {
            boolean hasChanges = false;
            for (Mission mission : allMissions) {
                if ("Diaria".equals(mission.getCategory()) && mission.isCompleted()) {
                    mission.setCompleted(false);
                    mission.setProgress(0);
                    // Recalcular deadline para el próximo día
                    mission.setupAsDailyMission();
                    hasChanges = true;
                }
            }
            
            if (hasChanges) {
                // Actualizar en la base de datos
                for (Mission mission : allMissions) {
                    if ("Diaria".equals(mission.getCategory())) {
                        dashboardViewModel.updateMission(mission);
                    }
                }
                
                // Actualizar la UI
                updateMissionsDisplay();
                updateMissionCounters();
                
                // Mostrar notificación al usuario
                Toast.makeText(getContext(), "¡Las misiones diarias se han restablecido!", Toast.LENGTH_SHORT).show();
            }
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
        
        // Limpiar el handler para evitar memory leaks
        if (countdownHandler != null && countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
        
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
        
        // Listener para ocultar/mostrar selector de fecha según el tipo de misión
        missionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = parent.getItemAtPosition(position).toString();
                if ("Diaria".equals(selectedType)) {
                    // Ocultar selector de fecha para misiones diarias
                    selectDeadlineBtn.setVisibility(View.GONE);
                    selectedDeadlineText.setText("Las misiones diarias se renuevan automáticamente cada 24 horas");
                } else {
                    // Mostrar selector de fecha para otros tipos
                    selectDeadlineBtn.setVisibility(View.VISIBLE);
                    selectedDeadlineText.setText("Fecha límite: Se calculará automáticamente");
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    
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
            boolean isDailyMission = "Diaria".equals(missionType);
            
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
                    isDailyMission ? "Diaria" : category, // Si es diaria, usar "Diaria" como categoría
                    false,
                    0,
                    1,
                    0,
                    iconType,
                    difficulty
            );
            
            // Configurar según el tipo de misión
            if (isDailyMission) {
                // Para misiones diarias, configurar automáticamente para 24 horas
                newMission.setTimeAmount(1);
                newMission.setTimeUnit("días");
                newMission.calculateDeadline();
            } else {
                // Usar deadline personalizado si se seleccionó, sino calcular automáticamente
                if (customDeadline[0] > 0) {
                    newMission.setDeadlineTimestamp(customDeadline[0]);
                } else {
                    newMission.setTimeAmount(1);
                    newMission.setTimeUnit("días");
                    newMission.calculateDeadline();
                }
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