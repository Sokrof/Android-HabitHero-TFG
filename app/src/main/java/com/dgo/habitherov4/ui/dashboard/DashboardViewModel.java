package com.dgo.habitherov4.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dgo.habitherov4.models.Mission;
import com.dgo.habitherov4.repositories.MissionRepository;

import java.util.List;

public class DashboardViewModel extends ViewModel {

    private final MissionRepository missionRepository;
    private final LiveData<List<Mission>> missions;

    public DashboardViewModel() {
        missionRepository = new MissionRepository();
        missions = missionRepository.getMissions();
    }

    public LiveData<List<Mission>> getMissions() {
        return missions;
    }
    
    public void addMission(Mission mission) {
        missionRepository.addMission(mission);
    }
    
    public void updateMission(Mission updatedMission) {
        missionRepository.updateMission(updatedMission);
    }
    
    public void deleteMission(String missionId) {
        missionRepository.deleteMission(missionId);
    }
}