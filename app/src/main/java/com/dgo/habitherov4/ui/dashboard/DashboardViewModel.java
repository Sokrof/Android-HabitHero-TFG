package com.dgo.habitherov4.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dgo.habitherov4.models.Mission;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<List<Mission>> missions;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private CollectionReference missionsCollection;

    public DashboardViewModel() {
        missions = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Inicializar la colección de misiones para el usuario actual
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            missionsCollection = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("missions");
            loadMissions();
        } else {

        }
    }

    public LiveData<List<Mission>> getMissions() {
        return missions;
    }
    
    private void loadMissions() {
        // Cargar misiones desde Firestore
        missionsCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Mission> missionList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Mission mission = document.toObject(Mission.class);
                    missionList.add(mission);
                }
                missions.setValue(missionList);
            } else {

            }
        });
    }

    
    public void addMission(Mission mission) {
        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && missionsCollection != null) {
            // Generar un ID único si no tiene uno
            if (mission.getId() == null || mission.getId().isEmpty()) {
                mission.setId(UUID.randomUUID().toString());
            }
            
            // Guardar la misión en Firestore
            missionsCollection.document(mission.getId()).set(mission)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar la lista local de misiones
                        List<Mission> currentMissions = missions.getValue();
                        if (currentMissions != null) {
                            currentMissions.add(mission);
                            missions.setValue(currentMissions);
                        }
                    });
        } else {
            // Si no hay usuario autenticado, solo actualizar la lista local
            List<Mission> currentMissions = missions.getValue();
            if (currentMissions != null) {
                currentMissions.add(mission);
                missions.setValue(currentMissions);
            }
        }
    }
    
    public void updateMission(Mission updatedMission) {
        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && missionsCollection != null) {
            // Actualizar la misión en Firestore
            missionsCollection.document(updatedMission.getId()).set(updatedMission)
                    .addOnSuccessListener(aVoid -> {
                        // Actualizar la lista local de misiones
                        updateLocalMission(updatedMission);
                    });
        } else {
            // Si no hay usuario autenticado, solo actualizar la lista local
            updateLocalMission(updatedMission);
        }
    }
    
    private void updateLocalMission(Mission updatedMission) {
        List<Mission> currentMissions = missions.getValue();
        if (currentMissions != null) {
            for (int i = 0; i < currentMissions.size(); i++) {
                if (currentMissions.get(i).getId().equals(updatedMission.getId())) {
                    currentMissions.set(i, updatedMission);
                    break;
                }
            }
            missions.setValue(currentMissions);
        }
    }
}