package com.dgo.habitherov4.ui.aventura;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Handler;
import android.os.Looper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;


public class AventuraFragment extends Fragment implements MissionsAdapter.OnMissionClickListener {

    private FragmentHomeBinding binding;
    private AventuraViewModel aventuraViewModel;
    private MissionsAdapter missionsAdapter;

    // HP del enemigo (Se puede moficiar desde interfaz visual)
    private int enemyCurrentHealth = 2;
    private int enemyMaxHealth = 2;

    private int chestCount = 0;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;
    private String currentUserId;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        aventuraViewModel = new ViewModelProvider(this).get(AventuraViewModel.class);

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

        // Configurar FAB
        FloatingActionButton addButton = binding.addMissionButton;
        addButton.setOnClickListener(v -> {
            showAddMissionDialog();
        });

        return root;
    }

    // Gestiona cuando hacemos Click en el cofre
    private void setupChestClick() {
        binding.chestContainer.setOnClickListener(v -> {
            if (chestCount <= 0) {
                Toast.makeText(getContext(), "No tienes cofres disponibles", Toast.LENGTH_SHORT).show();
                return;
            }
            showChestConfirmationDialog();
        });
    }

    // Alert al abrir el cofre
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

    // Obtener recompensas aleatorias de Firestore
    private void openChest() {
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

    // Guardamos la recompensa en la mochila
    private void addRewardToInventory(Reward reward) {
        InventoryReward inventoryReward = new InventoryReward(reward.getTitle(), reward.getDescription());

        db.collection("users").document(currentUserId)
                .collection("inventory")
                .add(inventoryReward)
                .addOnSuccessListener(documentReference -> {
                    loadBagCount(); // Actualizar contador de la mochila
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar en inventario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Gestiona el click del inventario
    private void setupBagClick() {
        binding.bagContainer.setOnClickListener(v -> {
            showBagConfirmationDialog();
        });
    }

    // Alerta para acceder al inventario
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

    // Actualiza los items de la mochila
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

    // Actualiza el "contador" de items
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

    // Alertas, contadores y configuraciones varias de enemigo, inventario y cofres
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

    // Alerta antes de atacar al enemigo
    private void showAttackConfirmationDialog() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        User currentUser = aventuraViewModel.getCurrentUser().getValue();
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
        }
    }

    // Método para atacar al enemigo
    private void attackEnemy() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        User currentUser = aventuraViewModel.getCurrentUser().getValue();
        if (currentUser == null || currentUser.getCurrentMana() <= 0) {
            return;
        }

        try {
            // Consumir 1 MP del jugador
            aventuraViewModel.consumeMana(1);

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
                aventuraViewModel.addExperience(2);

                // Resetear enemigo con delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isAdded()) {
                        resetEnemy();
                    }
                }, 1000);
            }
        } catch (Exception e) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Error durante el ataque", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Cuando ele nemigo muere, aparece otro al instante ( o casi )
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
        }
    }

    // Cargar el GIF animado del personaje usando Glide
    private void setupCharacterAnimation() {
        Glide.with(this)
                .asGif()
                .load(R.drawable.personaje)
                .into(binding.characterGif);

        // Cargar el GIF animado del enemigo
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
        aventuraViewModel.getMissions().observe(getViewLifecycleOwner(), missions -> {
            if (missions != null) {
                // Filtrar misiones para mostrar solo las NO completadas en el Home
                List<Mission> incompleteMissions = new ArrayList<>();
                for (Mission mission : missions) {
                    if (!mission.isCompleted()) {
                        incompleteMissions.add(mission);
                    }
                }
                missionsAdapter.updateMissions(incompleteMissions);

                // Mostrar/ocultar mensaje de misiones vacías
                if (incompleteMissions.isEmpty()) {
                    binding.missionsRecyclerView.setVisibility(View.GONE);
                    binding.emptyMissionsMessage.setVisibility(View.VISIBLE);
                } else {
                    binding.missionsRecyclerView.setVisibility(View.VISIBLE);
                    binding.emptyMissionsMessage.setVisibility(View.GONE);
                }
            }
        });


        // Observar cambios en relación a las barras de HP, MP y EXP del personaje ( usuario )
        aventuraViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateAllStatBars(user);
            } else {
                // Si no hay usuario, mostrar barras por defecto
                updateHpBar(5);    // HP completa
                updateManaBar(1);  // MP a 1
                updateExpBar(0);   // EXP a 0
            }
        });
    }

    private void updateAllStatBars(User user) {
        int hpLevel = user.getHpBarLevel();
        int manaLevel = user.getManaBarLevel();
        int expLevel = user.getExpBarLevel();

        updateHpBar(hpLevel);
        updateManaBar(manaLevel);
        updateExpBar(expLevel);
        updatePlayerLevel(user.getLevel()); // Agregar esta línea
    }

    private void updatePlayerLevel(int level) {
        binding.playerLevelCounter.setText("Nivel: " + level);
    }

    private void updateManaBar(int manaLevel) {
        // Asegurar que el nivel esté en el rango válido
        int level = Math.max(0, Math.min(5, manaLevel));
        String drawableName = "mp_" + level;

        try {
            int resourceId = getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
            if (resourceId != 0) {
                binding.manaBar.setImageResource(resourceId);
            } else {
                binding.manaBar.setImageResource(R.drawable.mp_0);
            }
        } catch (Exception e) {
            binding.manaBar.setImageResource(R.drawable.mp_0);
        }
    }

    private void updateHpBar(int hpLevel) {
        // Asegurar que el nivel esté en el rango válido
        int level = Math.max(0, Math.min(5, hpLevel));
        String drawableName = "hp_" + level;

        try {
            int resourceId = getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
            if (resourceId != 0) {
                binding.healthBar.setImageResource(resourceId);
            } else {
                binding.healthBar.setImageResource(R.drawable.hp_5);
            }
        } catch (Exception e) {
            binding.healthBar.setImageResource(R.drawable.hp_5);
        }
    }

    private void updateExpBar(int expLevel) {
        // Asegurar que el nivel esté en el rango válido
        int level = Math.max(0, Math.min(5, expLevel));
        String drawableName = "exp_" + level;

        try {
            int resourceId = getResources().getIdentifier(drawableName, "drawable", requireContext().getPackageName());
            if (resourceId != 0) {
                binding.experienceBar.setImageResource(resourceId);
            } else {
                binding.experienceBar.setImageResource(R.drawable.exp_0);
            }
        } catch (Exception e) {
            binding.experienceBar.setImageResource(R.drawable.exp_0);
        }
    }

    // Misiones //////////////////////////
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

    // Alerta para añadir misión
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
                    selectDeadlineBtn.setVisibility(View.GONE);
                    selectedDeadlineText.setText("Las misiones diarias se renuevan automáticamente cada 24 horas");
                    customDeadline[0] = 0;
                } else {
                    selectDeadlineBtn.setVisibility(View.VISIBLE);
                    if (customDeadline[0] == 0) {
                        selectedDeadlineText.setText("Fecha límite: Se programará automáticamente para 24 horas");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Configurar selector de fecha límite específica
        selectDeadlineBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                getContext(),
                                (timeView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    calendar.set(Calendar.SECOND, 0);

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

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            if (validateMissionFields(titleInput, descriptionInput, categoryChipGroup,
                    difficultyChipGroup, missionTypeSpinner, customDeadline[0])) {
                createMissionFromDialog(titleInput, descriptionInput, categoryChipGroup,
                        difficultyChipGroup, missionTypeSpinner, customDeadline[0]);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Validar campos obligatorios de Misiones
    private boolean validateMissionFields(TextInputEditText titleInput,
                                          TextInputEditText descriptionInput,
                                          ChipGroup categoryChipGroup,
                                          ChipGroup difficultyChipGroup,
                                          Spinner missionTypeSpinner,
                                          long customDeadline) {

        String title = titleInput.getText().toString().trim();

        titleInput.setError(null);
        descriptionInput.setError(null);

        boolean isValid = true;

        // Validación del título
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

        // Validación de categoría
        if (categoryChipGroup.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(getContext(), "Selecciona una categoría", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // Validación de dificultad
        if (difficultyChipGroup.getCheckedChipId() == View.NO_ID) {
            Toast.makeText(getContext(), "Selecciona una dificultad", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        return isValid;
    }

    // Crear misión
    private void createMissionFromDialog(TextInputEditText titleInput,
                                         TextInputEditText descriptionInput,
                                         ChipGroup categoryChipGroup,
                                         ChipGroup difficultyChipGroup,
                                         Spinner missionTypeSpinner,
                                         long customDeadline) {

        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        String category = "";
        int checkedChipId = categoryChipGroup.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip selectedChip = categoryChipGroup.findViewById(checkedChipId);
            category = selectedChip.getText().toString();
        }

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

        String missionType = missionTypeSpinner.getSelectedItem().toString();
        boolean isDailyMission = "Diaria".equals(missionType);

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
                0,
                iconType,
                difficulty
        );

        if (isDailyMission) {
            newMission.setTimeAmount(1);
            newMission.setTimeUnit("días");
            newMission.calculateDeadline();
        } else {
            if (customDeadline > 0) {
                newMission.setDeadlineTimestamp(customDeadline);
            } else {
                long twentyFourHoursFromNow = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
                newMission.setDeadlineTimestamp(twentyFourHoursFromNow);
            }
        }

        newMission.setDifficulty(difficulty);

        aventuraViewModel.addMission(newMission);
        Toast.makeText(getContext(), "Misión añadida correctamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMissionClick(Mission mission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(mission.getTitle())
                .setMessage("¿Qué deseas hacer con esta misión?")
                .setPositiveButton("Completar", (dialog, which) -> {
                    if (!mission.isCompleted()) {
                        int manaReward = mission.getManaReward();
                        aventuraViewModel.completeMission(mission.getId());
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
}
