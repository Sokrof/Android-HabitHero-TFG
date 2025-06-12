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
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dgo.habitherov4.databinding.FragmentDashboardBinding;
import com.dgo.habitherov4.models.Mission;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dgo.habitherov4.EditMissionActivity;
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

// Eliminar estos imports:
// import android.app.DatePickerDialog;
// import android.app.TimePickerDialog;
// import android.widget.AdapterView;
// import android.widget.ArrayAdapter;
// import android.widget.Button;
// import android.widget.Spinner;
// import com.google.android.material.chip.Chip;
// import com.google.android.material.chip.ChipGroup;
// import com.google.android.material.floatingactionbutton.FloatingActionButton;
// import com.google.android.material.textfield.TextInputEditText;
// import java.text.SimpleDateFormat;
// import java.util.Calendar;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;

public class DashboardFragment extends Fragment {

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

        // Inicializar el contador de misiones diarias
        initializeDailyMissionsCountdown();
        
        // Observar cambios en las misiones
        dashboardViewModel.getMissions().observe(getViewLifecycleOwner(), missions -> {
            allMissions = missions;
            updateMissionsDisplay();
        });

        return root;
    }

   // Método corregido para actualizar las estadísticas cuando cambian las misiones
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
    
    // Only update adapters if they exist
    if (dailyMissionsAdapter != null) {
        dailyMissionsAdapter.updateMissions(dailyMissions);
    }
    if (activeMissionsAdapter != null) {
        activeMissionsAdapter.updateMissions(activeMissions);
    }
    
    // IMPORTANTE: Actualizar estadísticas cuando cambien las misiones
    updateStatistics();
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
   
        // Only update adapters if they exist
        if (dailyMissionsAdapter != null) {
            dailyMissionsAdapter.updateMissions(dailyMissions);
        }
        if (activeMissionsAdapter != null) {
            activeMissionsAdapter.updateMissions(activeMissions);
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
    }
    
    // Nuevo método para restablecer las misiones diarias
    private void resetDailyMissions() {
        if (allMissions != null) {
            boolean hasChanges = false;
            for (Mission mission : allMissions) {
                if ("Diaria".equals(mission.getCategory()) && mission.isCompleted()) {
                    mission.setCompleted(false);
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

                // Mostrar notificación al usuario
                Toast.makeText(getContext(), "¡Las misiones diarias se han restablecido!", Toast.LENGTH_SHORT).show();
            }
        }
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
                new String[]{"Principal", "Diaria"});
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
                    customDeadline[0] = 0; // Reset custom deadline
                } else {
                    // Mostrar selector de fecha para otros tipos
                    selectDeadlineBtn.setVisibility(View.VISIBLE);
                    if (customDeadline[0] == 0) {
                        selectedDeadlineText.setText("Fecha límite: Se programará automáticamente para 24 horas");
                    }
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
                            
                            // Validar que la fecha sea al menos 1 hora en el futuro
                            long selectedTime = calendar.getTimeInMillis();
                            long currentTime = System.currentTimeMillis();
                            long oneHourFromNow = currentTime + (60 * 60 * 1000);
                            
                            if (selectedTime < oneHourFromNow) {
                                Toast.makeText(getContext(), 
                                    "La fecha debe ser al menos 1 hora en el futuro", 
                                    Toast.LENGTH_LONG).show();
                                return;
                            }
                            
                            customDeadline[0] = selectedTime;
                            
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
            // Validar campos ANTES de cerrar el diálogo
            if (validateMissionFields(titleInput, descriptionInput, categoryChipGroup, 
                                    difficultyChipGroup, missionTypeSpinner, customDeadline[0])) {
                // Solo si la validación es exitosa, crear la misión y cerrar el diálogo
                createMissionFromDialog(titleInput, descriptionInput, categoryChipGroup,
                                      difficultyChipGroup, missionTypeSpinner, customDeadline[0]);
                dialog.dismiss();
            }
            // Si la validación falla, el diálogo permanece abierto
        });
    
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
    
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                // Validar campos ANTES de cerrar el diálogo
                if (validateMissionFields(titleInput, descriptionInput, categoryChipGroup, 
                                        difficultyChipGroup, missionTypeSpinner, customDeadline[0])) {
                    // Solo si la validación es exitosa, crear la misión y cerrar el diálogo
                    createMissionFromDialog(titleInput, descriptionInput, categoryChipGroup,
                                          difficultyChipGroup, missionTypeSpinner, customDeadline[0]);
                    dialog.dismiss();
                }
                // Si la validación falla, el diálogo permanece abierto
            });
        });
    
        dialog.show();
    }

    private boolean validateMissionFields(TextInputEditText titleInput, 
                                        TextInputEditText descriptionInput,
                                        ChipGroup categoryChipGroup,
                                        ChipGroup difficultyChipGroup,
                                        Spinner missionTypeSpinner,
                                        long customDeadline) {
        
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        
        // Limpiar errores previos
        titleInput.setError(null);
        descriptionInput.setError(null);
        
        boolean isValid = true;
        
        // Validar título
        if (title.isEmpty()) {
            titleInput.setError("El título es requerido");
            if (isValid) titleInput.requestFocus();
            isValid = false;
        } else if (title.length() < 3) {
            titleInput.setError("El título debe tener al menos 3 caracteres");
            if (isValid) titleInput.requestFocus();
            isValid = false;
        } else if (title.length() > 50) {
            titleInput.setError("El título no puede exceder 50 caracteres");
            if (isValid) titleInput.requestFocus();
            isValid = false;
        }
        
        // Validar descripción
        if (description.isEmpty()) {
            descriptionInput.setError("La descripción es requerida");
            if (isValid) descriptionInput.requestFocus();
            isValid = false;
        } else if (description.length() < 10) {
            descriptionInput.setError("La descripción debe tener al menos 10 caracteres");
            if (isValid) descriptionInput.requestFocus();
            isValid = false;
        } else if (description.length() > 200) {
            descriptionInput.setError("La descripción no puede exceder 200 caracteres");
            if (isValid) descriptionInput.requestFocus();
            isValid = false;
        }
        
        // Validar categoría
        if (categoryChipGroup.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(getContext(), "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validar dificultad
        if (difficultyChipGroup.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(getContext(), "Selecciona una dificultad", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        // Validar fecha para misiones no diarias
        String missionType = missionTypeSpinner.getSelectedItem().toString();
        if (!"Diaria".equals(missionType)) {
            if (customDeadline > 0) {
                // Si se especificó fecha, validar que sea al menos 1 hora en el futuro
                long currentTime = System.currentTimeMillis();
                long oneHourFromNow = currentTime + (60 * 60 * 1000); // 1 hora en milisegundos
                
                if (customDeadline < oneHourFromNow) {
                    Toast.makeText(getContext(), 
                        "La fecha debe ser al menos 1 hora en el futuro", 
                        Toast.LENGTH_LONG).show();
                    isValid = false;
                }
            }
            // Si no se especificó fecha, se programará automáticamente para 24h
        }
        
        return isValid;
    }

    private void createMissionFromDialog(TextInputEditText titleInput,
                                       TextInputEditText descriptionInput,
                                       ChipGroup categoryChipGroup,
                                       ChipGroup difficultyChipGroup,
                                       Spinner missionTypeSpinner,
                                       long customDeadline) {
        
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        
        // Obtener categoría seleccionada
        String category = "";
        int checkedChipId = categoryChipGroup.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip selectedChip = categoryChipGroup.findViewById(checkedChipId);
            category = selectedChip.getText().toString();
        }
        
        // Obtener dificultad seleccionada
        String difficulty = "";
        int checkedDifficultyId = difficultyChipGroup.getCheckedChipId();
        if (checkedDifficultyId != View.NO_ID) {
            Chip selectedDifficultyChip = difficultyChipGroup.findViewById(checkedDifficultyId);
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
                isDailyMission ? "Diaria" : category,
                false,
                0,  // manaReward (se asignará automáticamente por setDifficulty)
                iconType,
                difficulty
        );
        
        // Configurar deadline
        if (isDailyMission) {
            // Para misiones diarias, configurar automáticamente para 24 horas
            newMission.setTimeAmount(1);
            newMission.setTimeUnit("días");
            newMission.calculateDeadline();
        } else {
            if (customDeadline > 0) {
                // Usar fecha personalizado si se seleccionó, sino calcular automáticamente
                newMission.setDeadlineTimestamp(customDeadline);
            } else {
                // Si no se especificó fecha, programar para 24h desde ahora
                long twentyFourHoursFromNow = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
                newMission.setDeadlineTimestamp(twentyFourHoursFromNow);
            }
        }
        
        // Asignar la dificultad
        newMission.setDifficulty(difficulty);
        
        // Programar alarma/notificación en Firebase
        scheduleFirebaseAlarm(newMission);
    
        // Guardar la misión
        dashboardViewModel.addMission(newMission);
        Toast.makeText(getContext(), "Misión añadida correctamente", Toast.LENGTH_SHORT).show();
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

    private void updateStatistics() {
        if (allMissions == null) return;
    
        // Calcular estadísticas
        int totalMissions = allMissions.size();
        int completedMissions = 0;
        int expiredMissions = 0;
        int pendingMissions = 0; // Agregar contador de pendientes
        int easyMissions = 0;
        int mediumMissions = 0;
        int hardMissions = 0;
    
        long currentTime = System.currentTimeMillis();
    
        for (Mission mission : allMissions) {
            if (mission.isCompleted()) {
                completedMissions++;
            } else if (mission.getDeadlineTimestamp() > 0 && mission.getDeadlineTimestamp() < currentTime) {
                expiredMissions++;
            } else {
                // Si no está completada ni expirada, entonces está pendiente
                pendingMissions++;
            }
    
            // Contar por dificultad
            String difficulty = mission.getDifficulty();
            if (difficulty != null) {
                switch (difficulty.toLowerCase()) {
                    case "fácil":
                        easyMissions++;
                        break;
                    case "medio":
                        mediumMissions++;
                        break;
                    case "difícil":
                        hardMissions++;
                        break;
                }
            }
        }
    
        // Actualizar cards de estadísticas
        if (binding.totalMissionsCount != null) {
            binding.totalMissionsCount.setText(String.valueOf(totalMissions));
        }
        if (binding.completedMissionsCount != null) {
            binding.completedMissionsCount.setText(String.valueOf(completedMissions));
        }
        if (binding.expiredMissionsCount != null) {
            binding.expiredMissionsCount.setText(String.valueOf(expiredMissions));
        }
    
        // Configurar gráfico circular con las tres categorías correctas
        setupPieChart(completedMissions, expiredMissions, pendingMissions);
    
        // Configurar gráfico de barras
        setupBarChart(easyMissions, mediumMissions, hardMissions);
    
        // Calcular progreso semanal
        updateWeeklyProgress();
    }

    private void setupPieChart(int completed, int expired, int pending) {
        PieChart pieChart = binding.pieChart;
        
        if (pieChart == null) return;
    
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        // Solo agregar categorías que tengan valores > 0
        if (completed > 0) {
            entries.add(new PieEntry(completed, "Completadas"));
            colors.add(Color.rgb(76, 175, 80)); // Verde para completadas
        }
        
        if (expired > 0) {
            entries.add(new PieEntry(expired, "Expiradas"));
            colors.add(Color.rgb(244, 67, 54)); // Rojo para expiradas
        }
        
        if (pending > 0) {
            entries.add(new PieEntry(pending, "Pendientes"));
            colors.add(Color.rgb(255, 193, 7)); // Amarillo para pendientes
        }
    
        // Si no hay datos, mostrar un placeholder
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "Sin misiones"));
            colors.add(Color.rgb(158, 158, 158)); // Gris para sin datos
        }
    
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f); // Espacio entre las secciones
        dataSet.setSelectionShift(8f); // Efecto al seleccionar
    
        PieData data = new PieData(dataSet);
        
        // Formatear los valores para mostrar números enteros
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });
    
        pieChart.setData(data);
    
        // Configuración del gráfico
        Description desc = new Description();
        desc.setText("");
        pieChart.setDescription(desc);
        
        // Configuración del agujero central
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setHoleColor(Color.WHITE);
        
        // Configuración de la leyenda
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setForm(com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE);
        
        // Configuración de entrada de texto
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.BLACK);
        
        // Animación
        pieChart.animateY(1000);
        
        // Actualizar el gráfico
        pieChart.invalidate();
    }

    private void setupBarChart(int easy, int medium, int hard) {
        BarChart barChart = binding.barChart;

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, easy));
        entries.add(new BarEntry(1f, medium));
        entries.add(new BarEntry(2f, hard));

        BarDataSet dataSet = new BarDataSet(entries, "Misiones por Dificultad");
        dataSet.setColors(new int[]{Color.rgb(205, 127, 50), Color.rgb(192, 192, 192), Color.rgb(255, 215, 0)});
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChart.setData(data);
        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    // Método corregido para el progreso semanal
private void updateWeeklyProgress() {
    if (allMissions == null) return;

    // Obtener fecha actual
    Calendar now = Calendar.getInstance();
    Calendar startOfWeek = Calendar.getInstance();
    
    // Configurar inicio de semana (lunes)
    startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
    startOfWeek.set(Calendar.MINUTE, 0);
    startOfWeek.set(Calendar.SECOND, 0);
    startOfWeek.set(Calendar.MILLISECOND, 0);
    
    long startOfWeekTimestamp = startOfWeek.getTimeInMillis();
    
    // Progreso semanal: objetivo de 7 misiones completadas esta semana
    int weeklyTarget = 7;
    int weeklyCompleted = 0;

    // Contar solo las misiones completadas en esta semana
    for (Mission mission : allMissions) {
        if (mission.isCompleted()) {
            // Si tienes timestamp de cuando se completó la misión, úsalo aquí
            // Por ahora contamos todas las completadas
            // TODO: Agregar campo completedTimestamp a Mission para filtrar por semana actual
            weeklyCompleted++;
        }
    }

    // Limitar el progreso al máximo del objetivo
    int actualCompleted = Math.min(weeklyCompleted, weeklyTarget);
    int progress = weeklyTarget > 0 ? (actualCompleted * 100) / weeklyTarget : 0;

    if (binding.weeklyProgress != null) {
        binding.weeklyProgress.setProgress(progress);
    }
    if (binding.weeklyProgressText != null) {
        binding.weeklyProgressText.setText(progress + "% completado esta semana (" + actualCompleted + "/" + weeklyTarget + ")");
    }
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
}
