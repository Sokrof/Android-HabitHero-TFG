package com.dgo.habitherov4.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.util.Log;
import com.dgo.habitherov4.models.Mission;
import com.dgo.habitherov4.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<List<Mission>> missions = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    
    public HomeViewModel() {
        loadMissions();
        loadCurrentUser();
    }
    
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
                    android.util.Log.e("HomeViewModel", "Error loading missions", error);
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
                            android.util.Log.e("HomeViewModel", "Error parsing mission", e);
                        }
                    }
                }
                android.util.Log.d("HomeViewModel", "Loaded " + missionList.size() + " missions");
                missions.setValue(missionList);
                isLoading.setValue(false);
            });
    }
    
    private void loadCurrentUser() {
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .addSnapshotListener((documentSnapshot, error) -> {
                if (error != null) {
                    android.util.Log.e("HomeViewModel", "Error loading user", error);
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    try {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId());
                            Log.d("HomeViewModel", "✓ User loaded from Firestore: HP=" + user.getCurrentHp() + 
                                  ", MP=" + user.getCurrentMana() + ", EXP=" + user.getCurrentExp());
                            currentUser.setValue(user);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("HomeViewModel", "Error parsing user", e);
                    }
                } else {
                    Log.w("HomeViewModel", "User document doesn't exist, creating default user");
                    // Si el usuario no existe, crear uno por defecto
                    createDefaultUser(userId);
                }
            });
    }
    
    private void createDefaultUser(String userId) {
        User defaultUser = new User();
        defaultUser.setId(userId);
        defaultUser.setName("Usuario");
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(defaultUser)
            .addOnSuccessListener(aVoid -> {
                Log.d("HomeViewModel", "✓ Default user created");
                currentUser.setValue(defaultUser);
            })
            .addOnFailureListener(e -> {
                Log.e("HomeViewModel", "✗ Error creating default user", e);
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
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public void completeMission(String missionId) {
        if (missionId == null || missionId.isEmpty()) {
            Log.w("HomeViewModel", "Mission ID is null or empty");
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
                                // La misión ya no da maná, pero lo dejamos a 0 en vez de quitarlo por si acaso.
                                updateUserStats(userId, mission.getManaReward(), 0); 
                                Log.d("HomeViewModel", "Mission completed successfully");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("HomeViewModel", "Error completing mission", e);
                            });
                    }
                }
            });
    }

    private void updateUserStats(String userId, int manaToAdd, int expToAdd) {
        Log.d("HomeViewModel", "=== UPDATING USER STATS ===");
        Log.d("HomeViewModel", "Adding: +" + manaToAdd + " MP, +" + expToAdd + " EXP");
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        user.setId(userId);
                        
                        // Log valores antes de actualizar
                        Log.d("HomeViewModel", "BEFORE - HP: " + user.getCurrentHp() + "/" + user.getMaxHp() + 
                              ", MP: " + user.getCurrentMana() + "/" + user.getMaxMana() + 
                              ", EXP: " + user.getCurrentExp() + "/" + user.getMaxExp());
                        
                        // Actualizar stats
                        user.addMana(manaToAdd);
                        user.addExp(expToAdd);
                        
                        // Log valores después de actualizar
                        Log.d("HomeViewModel", "AFTER - HP: " + user.getCurrentHp() + "/" + user.getMaxHp() + 
                              ", MP: " + user.getCurrentMana() + "/" + user.getMaxMana() + 
                              ", EXP: " + user.getCurrentExp() + "/" + user.getMaxExp());
                        
                        // Usar Map para actualizar solo los campos necesarios
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("currentMana", user.getCurrentMana());
                        updates.put("currentExp", user.getCurrentExp());
                        updates.put("currentHp", user.getCurrentHp());
                        updates.put("level", user.getLevel());
                        updates.put("maxExp", user.getMaxExp());
                        updates.put("maxHp", user.getMaxHp());
                        updates.put("maxMana", user.getMaxMana());
                        
                        // Actualizar en Firestore con update() en lugar de set()
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("HomeViewModel", "✓ User stats updated in Firestore successfully");
                                // Actualizar el LiveData inmediatamente para reflejar cambios en UI
                                currentUser.setValue(user);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("HomeViewModel", "✗ Error updating user stats in Firestore", e);
                            });
                    } else {
                        Log.e("HomeViewModel", "✗ User object is null");
                    }
                } else {
                    Log.e("HomeViewModel", "✗ User document doesn't exist");
                }
            })
            .addOnFailureListener(e -> {
                Log.e("HomeViewModel", "✗ Error getting user data", e);
            });
    }

    public void updateUser(User user) {
        if (user == null || user.getId() == null) {
            Log.w("HomeViewModel", "User or user ID is null");
            return;
        }
        
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getId())
            .set(user)
            .addOnSuccessListener(aVoid -> {
                Log.d("HomeViewModel", "Usuario actualizado exitosamente");
                currentUser.setValue(user);
            })
            .addOnFailureListener(e -> {
                Log.e("HomeViewModel", "Error al actualizar usuario", e);
            });
    }

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
                    Log.d("HomeViewModel", "✓ Mana consumed successfully");
                    currentUser.setValue(user);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeViewModel", "✗ Error consuming mana", e);
                });
        }
    }

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
                    Log.d("HomeViewModel", "✓ Experience added successfully");
                    currentUser.setValue(user);
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeViewModel", "✗ Error adding experience", e);
                });
        }
    }
}