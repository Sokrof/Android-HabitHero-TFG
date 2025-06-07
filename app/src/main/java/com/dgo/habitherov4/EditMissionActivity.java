package com.dgo.habitherov4;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditMissionActivity extends AppCompatActivity {
    
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private ChipGroup categoryChipGroup;
    private ChipGroup difficultyChipGroup;
    private Button dateButton;
    private Button saveButton;
    private Button deleteButton;
    
    private String missionId;
    private long selectedDateTimestamp;
    private Calendar selectedDate;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mission);
        
        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Inicializar vistas
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryChipGroup = findViewById(R.id.category_chip_group);
        difficultyChipGroup = findViewById(R.id.difficulty_chip_group);
        dateButton = findViewById(R.id.dateButton);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        
        // Obtener datos de la misión
        Intent intent = getIntent();
        missionId = intent.getStringExtra("mission_id");
        String title = intent.getStringExtra("mission_title");
        String description = intent.getStringExtra("mission_description");
        String category = intent.getStringExtra("mission_category");
        String difficulty = intent.getStringExtra("mission_difficulty");
        selectedDateTimestamp = intent.getLongExtra("mission_deadline", 0);
        
        // Llenar campos
        if (title != null) titleEditText.setText(title);
        if (description != null) descriptionEditText.setText(description);
        
        // Seleccionar categoría
        selectCategoryChip(category);
        
        // Seleccionar dificultad
        selectDifficultyChip(difficulty);
        
        // Mostrar fecha
        if (selectedDateTimestamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateButton.setText(sdf.format(new Date(selectedDateTimestamp)));
        }
        
        // Configurar listeners
        dateButton.setOnClickListener(v -> showDatePicker());
        saveButton.setOnClickListener(v -> saveMission());
        deleteButton.setOnClickListener(v -> deleteMission());
    }
    
    private void selectCategoryChip(String category) {
        if (category == null) return;
        
        switch (category.toLowerCase()) {
            case "economía":
            case "economia":
                categoryChipGroup.check(R.id.chip_economia);
                break;
            case "salud":
                categoryChipGroup.check(R.id.chip_salud);
                break;
            case "académico":
            case "academico":
                categoryChipGroup.check(R.id.chip_academico);
                break;
        }
    }
    
    private void selectDifficultyChip(String difficulty) {
        if (difficulty == null) return;
        
        switch (difficulty.toLowerCase()) {
            case "fácil":
            case "facil":
            case "easy":
                difficultyChipGroup.check(R.id.chip_easy);
                break;
            case "medio":
            case "medium":
                difficultyChipGroup.check(R.id.chip_medium);
                break;
            case "difícil":
            case "dificil":
            case "hard":
                difficultyChipGroup.check(R.id.chip_hard);
                break;
        }
    }
    
    private String getSelectedCategory() {
        int selectedId = categoryChipGroup.getCheckedChipId();
        if (selectedId == R.id.chip_economia) return "Economía";
        if (selectedId == R.id.chip_salud) return "Salud";
        if (selectedId == R.id.chip_academico) return "Académico";
        return null;
    }
    
    private String getSelectedDifficulty() {
        int selectedId = difficultyChipGroup.getCheckedChipId();
        if (selectedId == R.id.chip_easy) return "Fácil";
        if (selectedId == R.id.chip_medium) return "Medio";
        if (selectedId == R.id.chip_hard) return "Difícil";
        return null;
    }
    
    private int getExpForDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "fácil":
            case "facil":
                return 50;
            case "medio":
                return 160;
            case "difícil":
            case "dificil":
                return 380;
            default:
                return 50;
        }
    }
    
    private int getManaForDifficulty(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "facil":
                return 1;  // Fácil = 1 maná
            case "medio":
                return 3;  // Medio = 3 maná
            case "dificil":
                return 5;  // Difícil = 5 maná
            default:
                return 1;
        }
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                selectedDateTimestamp = selectedDate.getTimeInMillis();
                updateDateButton();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void updateDateButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateButton.setText(sdf.format(selectedDate.getTime()));
    }
    
    private void saveMission() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = getSelectedCategory();
        String difficulty = getSelectedDifficulty();
        
        // Validaciones
        if (title.isEmpty()) {
            titleEditText.setError("El título es requerido");
            return;
        }
        
        if (category == null) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (difficulty == null) {
            Toast.makeText(this, "Selecciona una dificultad", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedDateTimestamp == 0) {
            Toast.makeText(this, "Selecciona una fecha límite", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Crear mapa de actualización
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", description);
        updates.put("category", category);
        updates.put("difficulty", difficulty);
        updates.put("manaReward", getManaForDifficulty(difficulty));  // CAMBIAR de expReward a manaReward
        updates.put("deadline", selectedDateTimestamp);
        
        // Actualizar en Firestore
        String userId = getCurrentUserId();
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("missions").document(missionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditMissionActivity.this, "Misión actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditMissionActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }
    
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar Misión")
            .setMessage("¿Estás seguro de que quieres eliminar esta misión? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> deleteMission())
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void deleteMission() {
        String userId = getCurrentUserId();
        if (userId != null && missionId != null) {
            db.collection("users")
                .document(userId)
                .collection("missions")
                .document(missionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Misión eliminada exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar la misión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Toast.makeText(this, "Error: Usuario no autenticado o ID de misión inválido", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }
}