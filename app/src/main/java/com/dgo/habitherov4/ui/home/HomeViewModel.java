package com.dgo.habitherov4.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.dgo.habitherov4.models.Mission;
import com.dgo.habitherov4.models.User;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<User> currentUser;
    private final MutableLiveData<List<Mission>> missions;
    private final MutableLiveData<Boolean> isLoading;

    public HomeViewModel() {
        currentUser = new MutableLiveData<>();
        missions = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        
        loadUserData();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    
    public LiveData<List<Mission>> getMissions() {
        return missions;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    private void loadUserData() {
        // Aquí conectarías con Firebase para obtener datos reales
        // Por ahora, datos de ejemplo basados en la imagen
        User user = new User("Ricardo Sarnosa", 25, 75, 100, "");
        currentUser.setValue(user);
    }
    
    public void completeMission(String missionId) {
        List<Mission> currentMissions = missions.getValue();
        if (currentMissions != null) {
            for (Mission mission : currentMissions) {
                if (mission.getId().equals(missionId)) {
                    mission.setCompleted(true);
                    mission.setProgress(mission.getMaxProgress());
                    break;
                }
            }
            missions.setValue(currentMissions);
            
            // Aquí actualizarías la experiencia del usuario
            updateUserExperience();
        }
    }
    
    private void updateUserExperience() {
        User user = currentUser.getValue();
        if (user != null) {
            // Lógica para actualizar experiencia y nivel
            currentUser.setValue(user);
        }
    }
}