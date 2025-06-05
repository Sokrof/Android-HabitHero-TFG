package com.dgo.habitherov4.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dgo.habitherov4.models.Mission;
import com.dgo.habitherov4.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

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
        
        // CAMBIO AQUÍ: Usar la misma estructura que DashboardViewModel
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
                            currentUser.setValue(user);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("HomeViewModel", "Error parsing user", e);
                    }
                }
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
            android.util.Log.w("HomeViewModel", "Mission ID is null or empty");
            return;
        }
        
        String userId = getCurrentUserId();
        if (userId == null) return;
        
        // CAMBIO AQUÍ: Usar la misma estructura para completar misiones
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("missions")
            .document(missionId)
            .update("completed", true)
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("HomeViewModel", "Mission completed successfully");
                // Las misiones se actualizarán automáticamente por el listener
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("HomeViewModel", "Error completing mission", e);
            });
    }
}