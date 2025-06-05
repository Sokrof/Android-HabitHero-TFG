package com.dgo.habitherov4.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.dgo.habitherov4.R;
import com.dgo.habitherov4.models.Mission;

import java.util.UUID;

public class AddMissionDialog extends DialogFragment {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private Spinner categorySpinner;
    private EditText maxProgressEditText;
    private EditText expRewardEditText;
    private Spinner iconTypeSpinner;
    
    private OnMissionAddedListener listener;
    
    public interface OnMissionAddedListener {
        void onMissionAdded(Mission mission);
    }
    
    public void setOnMissionAddedListener(OnMissionAddedListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_mission, null);
        
        titleEditText = view.findViewById(R.id.mission_title);
        descriptionEditText = view.findViewById(R.id.mission_description);
        categorySpinner = view.findViewById(R.id.mission_category);
        maxProgressEditText = view.findViewById(R.id.mission_max_progress);
        expRewardEditText = view.findViewById(R.id.mission_exp_reward);
        iconTypeSpinner = view.findViewById(R.id.mission_icon_type);
        
        // Configurar spinner de categorías
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.mission_categories,
                android.R.layout.simple_spinner_item
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Configurar spinner de tipos de icono
        ArrayAdapter<CharSequence> iconAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.mission_icon_types,
                android.R.layout.simple_spinner_item
        );
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        iconTypeSpinner.setAdapter(iconAdapter);
        
        builder.setView(view)
                .setTitle("Añadir Misión")
                .setPositiveButton("Guardar", null) // Se configura después para evitar cierre automático
                .setNegativeButton("Cancelar", (dialog, id) -> dismiss());
        
        AlertDialog dialog = builder.create();
        
        // Configurar el botón positivo para validar antes de cerrar
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                if (validateForm()) {
                    Mission mission = createMissionFromForm();
                    if (listener != null) {
                        listener.onMissionAdded(mission);
                    }
                    dismiss();
                }
            });
        });
        
        return dialog;
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        if (titleEditText.getText().toString().trim().isEmpty()) {
            titleEditText.setError("El título es obligatorio");
            isValid = false;
        }
        
        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            descriptionEditText.setError("La descripción es obligatoria");
            isValid = false;
        }
        
        String maxProgressText = maxProgressEditText.getText().toString().trim();
        if (maxProgressText.isEmpty()) {
            maxProgressEditText.setError("El progreso máximo es obligatorio");
            isValid = false;
        } else {
            try {
                int maxProgress = Integer.parseInt(maxProgressText);
                if (maxProgress <= 0) {
                    maxProgressEditText.setError("El progreso máximo debe ser mayor que 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                maxProgressEditText.setError("Debe ser un número válido");
                isValid = false;
            }
        }
        
        String expRewardText = expRewardEditText.getText().toString().trim();
        if (expRewardText.isEmpty()) {
            expRewardEditText.setError("La recompensa es obligatoria");
            isValid = false;
        } else {
            try {
                int expReward = Integer.parseInt(expRewardText);
                if (expReward <= 0) {
                    expRewardEditText.setError("La recompensa debe ser mayor que 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                expRewardEditText.setError("Debe ser un número válido");
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private Mission createMissionFromForm() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        int maxProgress = Integer.parseInt(maxProgressEditText.getText().toString().trim());
        int expReward = Integer.parseInt(expRewardEditText.getText().toString().trim());
        String iconType = iconTypeSpinner.getSelectedItem().toString().toLowerCase();
        
        return new Mission(
                UUID.randomUUID().toString(),
                title,
                description,
                category,
                false,
                0,
                maxProgress,
                expReward,
                iconType
        );
    }
}