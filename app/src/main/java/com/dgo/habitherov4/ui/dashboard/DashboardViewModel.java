package com.dgo.habitherov4.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dgo.habitherov4.models.Mission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<List<Mission>> missions;

    public DashboardViewModel() {
        missions = new MutableLiveData<>();
        loadMissions();
    }

    public LiveData<List<Mission>> getMissions() {
        return missions;
    }
    
    private void loadMissions() {
        // Aquí cargaríamos las misiones desde Firebase o una base de datos local
        // Por ahora, crearemos algunas misiones de ejemplo
        List<Mission> missionList = new ArrayList<>();
        
        // Misión principal - Aprobar DI
        missionList.add(new Mission(
                UUID.randomUUID().toString(),
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
                UUID.randomUUID().toString(),
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
                UUID.randomUUID().toString(),
                "Hacer la compra",
                "Compra alimentos saludables",
                "Secundaria",
                false,
                0,
                1,
                20,
                "food"
        ));
        
        missions.setValue(missionList);
    }
    
    public void addMission(Mission mission) {
        List<Mission> currentMissions = missions.getValue();
        if (currentMissions != null) {
            currentMissions.add(mission);
            missions.setValue(currentMissions);
        }
    }
    
    public void updateMission(Mission updatedMission) {
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