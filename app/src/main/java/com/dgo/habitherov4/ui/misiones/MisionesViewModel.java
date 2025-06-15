package com.dgo.habitherov4.ui.misiones;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dgo.habitherov4.models.Mission;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MisionesViewModel extends ViewModel {

    // Uso un MLD para que cuando los datos cambien, notifique automáticamente a los observers,
    private final MutableLiveData<List<Mission>> missions;
    // FireBase
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    // Referenciamos las Misiones de Firestore en Firebase
    private CollectionReference missionsCollection;
    private ListenerRegistration missionsListener; //  listener para tiempo real

    public MisionesViewModel() {
        missions = new MutableLiveData<>();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Inicializar la colección de misiones
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            missionsCollection = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("missions");
            setupRealtimeListener(); // listener en tiempo real
        } else {
            // En un futuro implementaría una carga offline de misiones predeterminadas
        }
    }

    // Cargas las misiones en tiempo real desde Firestore.
    public LiveData<List<Mission>> getMissions() {
        return missions;
    }

    //  Metodo para configurar listener en tiempo real
    private void setupRealtimeListener() {
        if (missionsCollection != null) {
            missionsListener = missionsCollection.addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (e != null) {
                    return;
                }
                // queryDocumentSnapshots: contiene todos los documentos de la colección
                // e: contiene información de errores ( si los hay )

                if (queryDocumentSnapshots != null) {
                    List<Mission> missionList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mission mission = document.toObject(Mission.class);
                        missionList.add(mission);
                    }
                    missions.setValue(missionList);
                }
            });
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