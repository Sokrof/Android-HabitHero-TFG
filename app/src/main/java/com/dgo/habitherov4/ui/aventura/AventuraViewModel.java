package com.dgo.habitherov4.ui.aventura;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dgo.habitherov4.models.Mission;
import com.dgo.habitherov4.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AventuraViewModel extends ViewModel {
    // Listas con observer para reaccionar a los cambios
    private MutableLiveData<List<Mission>> missions = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public AventuraViewModel() {
        loadMissions();
        loadCurrentUser();
    }

    // Cargar misiones
    private void loadMissions() {
        isLoading.setValue(true);
        String userId = getCurrentUserId();
        if (userId == null) {
            isLoading.setValue(false);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("missions")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        isLoading.setValue(false);
                        return;
                    }

                    List<Mission> missionList = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Mission mission = doc.toObject(Mission.class);
                                if (mission != null) {
                                    mission.setId(doc.getId());
                                    missionList.add(mission);
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    missions.setValue(missionList);
                    isLoading.setValue(false);
                });
    }

    // Cargar usuario desde Firebase
    private void loadCurrentUser() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        try {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                user.setId(documentSnapshot.getId());
                                currentUser.setValue(user);
                            }
                        } catch (Exception e) {
                        }
                    } else {
                        // Si el usuario no existe, crear uno por defecto
                        createDefaultUser(userId);
                    }
                });
    }

    // usuario por defecto
    private void createDefaultUser(String userId) {
        User defaultUser = new User();
        defaultUser.setId(userId);
        defaultUser.setName("Usuario");

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(defaultUser)
                .addOnSuccessListener(aVoid -> {
                    currentUser.setValue(defaultUser);
                })
                .addOnFailureListener(e -> {
                });
    }

    private String getCurrentUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    public LiveData<List<Mission>> getMissions() {
        return missions;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void completeMission(String missionId) {
        if (missionId == null || missionId.isEmpty()) {
            return;
        }

        String userId = getCurrentUserId();
        if (userId == null) return;

        // Primero obtener la misión para saber cuánto maná dar
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("missions")
                .document(missionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Mission mission = documentSnapshot.toObject(Mission.class);
                        if (mission != null && !mission.isCompleted()) {
                            // Marcar misión como completada
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .collection("missions")
                                    .document(missionId)
                                    .update("completed", true)
                                    .addOnSuccessListener(aVoid -> {
                                        updateUserStats(userId, mission.getManaReward(), 0);
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        }
                    }
                });
    }

    private void updateUserStats(String userId, int manaToAdd, int expToAdd) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(userId);

                            // Actualizar stats
                            user.addMana(manaToAdd);
                            user.addExp(expToAdd);

                            // Usar Map para actualizar solo los campos necesarios
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("currentMana", user.getCurrentMana());
                            updates.put("currentExp", user.getCurrentExp());
                            updates.put("currentHp", user.getCurrentHp());
                            updates.put("level", user.getLevel());
                            updates.put("maxExp", user.getMaxExp());
                            updates.put("maxHp", user.getMaxHp());
                            updates.put("maxMana", user.getMaxMana());

                            // Actualizar en Firestore
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        // Actualizar el LiveData inmediatamente para reflejar cambios en UI
                                        currentUser.setValue(user);
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        } else {
                        }
                    } else {
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    // Consumir MP
    public void consumeMana(int manaToConsume) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        User user = currentUser.getValue();
        if (user != null && user.getCurrentMana() >= manaToConsume) {
            user.setCurrentMana(user.getCurrentMana() - manaToConsume);

            Map<String, Object> updates = new HashMap<>();
            updates.put("currentMana", user.getCurrentMana());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        currentUser.setValue(user);
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    // Agregar EXP
    public void addExperience(int expToAdd) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        User user = currentUser.getValue();
        if (user != null) {
            user.addExp(expToAdd);

            Map<String, Object> updates = new HashMap<>();
            updates.put("currentExp", user.getCurrentExp());
            updates.put("level", user.getLevel());
            updates.put("maxExp", user.getMaxExp());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        currentUser.setValue(user);
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }

    // Anyadir misión
    public void addMission(Mission mission) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        // Generar un ID único si no tiene uno
        if (mission.getId() == null || mission.getId().isEmpty()) {
            mission.setId(java.util.UUID.randomUUID().toString());
        }

        // Guardar la misión en Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("missions")
                .document(mission.getId())
                .set(mission)
                .addOnSuccessListener(aVoid -> {
                    // La lista se actualizará automáticamente por el listener
                })
                .addOnFailureListener(e -> {
                });
    }
}