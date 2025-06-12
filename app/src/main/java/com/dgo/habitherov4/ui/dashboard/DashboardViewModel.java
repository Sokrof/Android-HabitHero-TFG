package com.dgo.habitherov4.ui.dashboard;

import android.util.Log;

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
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<List<Mission>> missions;
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private CollectionReference missionsCollection;
    private ListenerRegistration missionsListener; // Agregar listener para tiempo real

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
            setupRealtimeListener(); // Configurar listener en tiempo real
        } else {
            loadMissions(); // Cargar misiones locales si no hay usuario
        }
    }

    public LiveData<List<Mission>> getMissions() {
        return missions;
    }

    // Nuevo método para configurar listener en tiempo real
    private void setupRealtimeListener() {
        if (missionsCollection != null) {
            missionsListener = missionsCollection.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    Log.w("DashboardViewModel", "Listen failed.", e);
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    List<Mission> missionList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mission mission = document.toObject(Mission.class);
                        missionList.add(mission);
                    }
                    missions.setValue(missionList);
                    Log.d("DashboardViewModel", "Missions updated from Firestore: " + missionList.size());
                }
            });
        }
    }

    private void loadMissions() {
        // Cargar misiones desde Firestore (método de respaldo)
        if (missionsCollection != null) {
            missionsCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Mission> missionList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Mission mission = document.toObject(Mission.class);
                        missionList.add(mission);
                    }
                    missions.setValue(missionList);
                    Log.d("DashboardViewModel", "Missions loaded: " + missionList.size());
                } else {
                    Log.e("DashboardViewModel", "Error loading missions", task.getException());
                }
            });
        }
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
                        Log.d("DashboardViewModel", "Mission added successfully to Firestore");
                        // No necesitamos actualizar manualmente aquí, el listener se encarga
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DashboardViewModel", "Error adding mission to Firestore", e);
                        // En caso de error, actualizar solo localmente
                        updateLocalMissionList(mission, "add");
                    });
        } else {
            // Si no hay usuario autenticado, solo actualizar la lista local
            updateLocalMissionList(mission, "add");
        }
    }

    public void updateMission(Mission updatedMission) {
        // Verificar si el usuario está autenticado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && missionsCollection != null) {
            // Actualizar la misión en Firestore
            missionsCollection.document(updatedMission.getId()).set(updatedMission)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DashboardViewModel", "Mission updated successfully in Firestore");
                        // No necesitamos actualizar manualmente aquí, el listener se encarga
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DashboardViewModel", "Error updating mission in Firestore", e);
                        // En caso de error, actualizar solo localmente
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

    // Nuevo método para actualizar lista local (agregar/eliminar)
    private void updateLocalMissionList(Mission mission, String action) {
        List<Mission> currentMissions = missions.getValue();
        if (currentMissions == null) {
            currentMissions = new ArrayList<>();
        }

        switch (action) {
            case "add":
                currentMissions.add(mission);
                break;
            case "remove":
                currentMissions.removeIf(m -> m.getId().equals(mission.getId()));
                break;
        }

        missions.setValue(currentMissions);
    }

    public void completeMission(String missionId) {
        if (missionId == null || missionId.isEmpty()) {
            Log.w("DashboardViewModel", "Mission ID is null or empty");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && missionsCollection != null) {
            // Update mission as completed in Firestore
            missionsCollection.document(missionId)
                    .update("completed", true)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DashboardViewModel", "Mission completed successfully in Firestore");
                        // No necesitamos actualizar manualmente aquí, el listener se encarga
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DashboardViewModel", "Error completing mission in Firestore", e);
                        // En caso de error, actualizar solo localmente
                        updateLocalMissionCompletion(missionId);
                    });
        } else {
            // If no authenticated user, only update local list
            updateLocalMissionCompletion(missionId);
        }
    }

    private void updateLocalMissionCompletion(String missionId) {
        List<Mission> currentMissions = missions.getValue();
        if (currentMissions != null) {
            for (Mission mission : currentMissions) {
                if (mission.getId().equals(missionId)) {
                    mission.setCompleted(true);
                    break;
                }
            }
            missions.setValue(currentMissions);
        }
    }

    // Método para eliminar misión
    public void deleteMission(String missionId) {
        if (missionId == null || missionId.isEmpty()) {
            Log.w("DashboardViewModel", "Mission ID is null or empty");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && missionsCollection != null) {
            // Eliminar misión de Firestore
            missionsCollection.document(missionId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DashboardViewModel", "Mission deleted successfully from Firestore");
                        // No necesitamos actualizar manualmente aquí, el listener se encarga
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DashboardViewModel", "Error deleting mission from Firestore", e);
                        // En caso de error, actualizar solo localmente
                        updateLocalMissionList(new Mission(missionId, "", "", "", false, 0, "", ""), "remove");
                    });
        } else {
            // Si no hay usuario autenticado, solo actualizar la lista local
            updateLocalMissionList(new Mission(missionId, "", "", "", false, 0, "", ""), "remove");
        }
    }

    // Método para refrescar manualmente las misiones
    public void refreshMissions() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && missionsCollection != null) {
            loadMissions();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Limpiar el listener cuando el ViewModel se destruye
        if (missionsListener != null) {
            missionsListener.remove();
        }
    }
}