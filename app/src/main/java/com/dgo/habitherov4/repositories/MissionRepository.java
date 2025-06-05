package com.dgo.habitherov4.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dgo.habitherov4.models.Mission;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissionRepository {
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_MISSIONS = "missions";
    
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final MutableLiveData<List<Mission>> missionsLiveData;
    
    public MissionRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        missionsLiveData = new MutableLiveData<>(new ArrayList<>());
    }
    
    public LiveData<List<Mission>> getMissions() {
        loadMissions();
        return missionsLiveData;
    }
    
    public void loadMissions() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        String userId = currentUser.getUid();
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_MISSIONS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Mission> missionList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mission mission = document.toObject(Mission.class);
                        missionList.add(mission);
                    }
                    missionsLiveData.setValue(missionList);
                })
                .addOnFailureListener(e -> {
                    // Manejar el error
                    // Podríamos cargar misiones de ejemplo en caso de error
                    loadSampleMissions();
                });
    }
    
    public void addMission(Mission mission) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        String userId = currentUser.getUid();
        CollectionReference missionsRef = db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_MISSIONS);
        
        // Si no hay ID, generamos uno nuevo
        if (mission.getId() == null || mission.getId().isEmpty()) {
            DocumentReference newMissionRef = missionsRef.document();
            mission.setId(newMissionRef.getId());
            newMissionRef.set(mission)
                    .addOnSuccessListener(aVoid -> loadMissions())
                    .addOnFailureListener(e -> {
                        // Manejar el error
                    });
        } else {
            // Si ya tiene ID, usamos ese ID
            missionsRef.document(mission.getId()).set(mission)
                    .addOnSuccessListener(aVoid -> loadMissions())
                    .addOnFailureListener(e -> {
                        // Manejar el error
                    });
        }
    }
    
    public void updateMission(Mission mission) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || mission.getId() == null) {
            return;
        }
        
        String userId = currentUser.getUid();
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_MISSIONS)
                .document(mission.getId())
                .set(mission)
                .addOnSuccessListener(aVoid -> loadMissions())
                .addOnFailureListener(e -> {
                    // Manejar el error
                });
    }
    
    public void deleteMission(String missionId) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        String userId = currentUser.getUid();
        db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_MISSIONS)
                .document(missionId)
                .delete()
                .addOnSuccessListener(aVoid -> loadMissions())
                .addOnFailureListener(e -> {
                    // Manejar el error
                });
    }
    
    private void loadSampleMissions() {
        // Cargar misiones de ejemplo en caso de error o para usuarios nuevos
        List<Mission> missionList = new ArrayList<>();
        
        // Misión principal - Aprobar DI
        missionList.add(new Mission(
                "sample1",
                "Aprobar DI",
                "Estudia!!",
                "Principal",
                false,
                47,
                120,
                47,
                "study"
        ));
        
        // Misión secundaria - Limpiar la casa
        missionList.add(new Mission(
                "sample2",
                "Limpiar la casa",
                "Mantén tu espacio ordenado",
                "Secundaria",
                false,
                0,
                1,
                15,
                "default"
        ));
        
        // Misión secundaria - Hacer la compra
        missionList.add(new Mission(
                "sample3",
                "Hacer la compra",
                "Compra alimentos saludables",
                "Secundaria",
                false,
                0,
                1,
                20,
                "food"
        ));
        
        missionsLiveData.setValue(missionList);
    }
}