package com.dgo.habitherov4.ui.misiones;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.dgo.habitherov4.databinding.FragmentDashboardBinding;
import com.dgo.habitherov4.models.Mission;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MisionesFragment extends Fragment {

    // Variables
    private FragmentDashboardBinding binding;
    private MisionesViewModel misionesViewModel;
    private List<Mission> allMissions;
    private List<Mission> dailyMissions;
    private List<Mission> activeMissions;

    // Ejecución del programa
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        misionesViewModel = new ViewModelProvider(this).get(MisionesViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar listas
        allMissions = new ArrayList<>();
        dailyMissions = new ArrayList<>();
        activeMissions = new ArrayList<>();

        // Observar cambios en las misiones
        misionesViewModel.getMissions().observe(getViewLifecycleOwner(), missions -> {
            allMissions = missions;
            updateMissionsDisplay();
        });
        return root;
    }

    // Metodo para actualizar las estadísticas cuando cambian las misiones
    private void updateMissionsDisplay() {
        if (allMissions == null) return;

        // Asegurar de que las listas estén inicializadas
        if (dailyMissions == null) dailyMissions = new ArrayList<>();
        if (activeMissions == null) activeMissions = new ArrayList<>();

        dailyMissions.clear();
        activeMissions.clear();

        for (Mission mission : allMissions) {
            if ("Diaria".equals(mission.getCategory())) {
                dailyMissions.add(mission);
            } else {
                activeMissions.add(mission);
            }
        }
        // Actualizar estadísticas
        updateStatistics();
    }

    private void updateStatistics() {
        if (allMissions == null) return;

        // Calcular estadísticas
        int totalMissions = allMissions.size();
        int completedMissions = 0;
        int expiredMissions = 0;
        int pendingMissions = 0; // Agregar contador de pendientes
        int easyMissions = 0;
        int mediumMissions = 0;
        int hardMissions = 0;

        for (Mission mission : allMissions) {
            if (mission.isCompleted()) {
                completedMissions++;
            } else if (mission.isExpired()) {
                expiredMissions++;
            } else {
                // Si no está completada ni expirada, entonces está pendiente
                pendingMissions++;
            }

            // Contar por dificultad
            String difficulty = mission.getDifficulty();
            if (difficulty != null) {
                switch (difficulty.toLowerCase()) {
                    case "fácil":
                        easyMissions++;
                        break;
                    case "medio":
                        mediumMissions++;
                        break;
                    case "difícil":
                        hardMissions++;
                        break;
                }
            }
        }

        // Actualizar cards de estadísticas
        binding.totalMissionsCount.setText(String.valueOf(totalMissions));
        binding.completedMissionsCount.setText(String.valueOf(completedMissions));
        binding.expiredMissionsCount.setText(String.valueOf(expiredMissions));

        // Gráfico circular con las tres categorías
        setupPieChart(completedMissions, expiredMissions, pendingMissions);

        // Gráfico de barras
        setupBarChart(easyMissions, mediumMissions, hardMissions);

        // Calcular progreso semanal
        updateWeeklyProgress();
    }

    // Chart circular
    private void setupPieChart(int completed, int expired, int pending) {
        PieChart pieChart = binding.pieChart;

        if (pieChart == null) return;

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Solo agregar categorías que tengan valores > 0
        if (completed > 0) {
            entries.add(new PieEntry(completed, "Completadas"));
            colors.add(Color.rgb(76, 175, 80)); // Verde para completadas
        }

        if (expired > 0) {
            entries.add(new PieEntry(expired, "Expiradas"));
            colors.add(Color.rgb(244, 67, 54)); // Rojo para expiradas
        }

        if (pending > 0) {
            entries.add(new PieEntry(pending, "Pendientes"));
            colors.add(Color.rgb(255, 193, 7)); // Amarillo para pendientes
        }

        // Si no hay datos, mostrar un placeholder
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "Sin misiones"));
            colors.add(Color.rgb(158, 158, 158)); // Gris para el resto.
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(2f); // Espacio entre las secciones
        dataSet.setSelectionShift(8f); // Efecto al seleccionar

        PieData data = new PieData(dataSet);

        // Formatear los valores para mostrar números enteros
        data.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        pieChart.setData(data);

        // Configuración del gráfico
        Description desc = new Description();
        desc.setText("");
        pieChart.setDescription(desc);

        // Configuración del agujero central
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setHoleColor(Color.WHITE);

        // Configuración de la leyenda
        pieChart.getLegend().setEnabled(true);
        pieChart.getLegend().setTextSize(12f);
        pieChart.getLegend().setForm(com.github.mikephil.charting.components.Legend.LegendForm.CIRCLE);

        // Configuración de entrada de texto
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.BLACK);

        // Animación
        pieChart.animateY(1000);

        // Actualizar el gráfico
        pieChart.invalidate();
    }

    //  Chart lineal
    private void setupBarChart(int easy, int medium, int hard) {
        BarChart barChart = binding.barChart;

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, easy));
        entries.add(new BarEntry(1f, medium));
        entries.add(new BarEntry(2f, hard));

        BarDataSet dataSet = new BarDataSet(entries, "Misiones por Dificultad");
        dataSet.setColors(new int[]{Color.rgb(205, 127, 50), Color.rgb(192, 192, 192), Color.rgb(255, 215, 0)});
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        barChart.setData(data);
        Description desc = new Description();
        desc.setText("");
        barChart.setDescription(desc);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    // Progreso semanal
    @SuppressLint("SetTextI18n")
    private void updateWeeklyProgress() {
        if (allMissions == null) return;

        // Obtener fecha actual
        Calendar now = Calendar.getInstance();
        Calendar startOfWeek = Calendar.getInstance();

        // Configurar inicio de semana (lunes)
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0);
        startOfWeek.set(Calendar.MINUTE, 0);
        startOfWeek.set(Calendar.SECOND, 0);
        startOfWeek.set(Calendar.MILLISECOND, 0);

        long startOfWeekTimestamp = startOfWeek.getTimeInMillis();

        // Progreso semanal: objetivo de 7 misiones completadas esta semana
        int weeklyTarget = 7;
        int weeklyCompleted = 0;

        // Contar solo las misiones completadas en esta semana
        for (Mission mission : allMissions) {
            if (mission.isCompleted()) {
                // NOTA: Por ahora contamos todas las completadas a falta de saber como
                // resetear semanalmente
                weeklyCompleted++;
            }
        }

        // Limitar el progreso al máximo del objetivo
        int actualCompleted = Math.min(weeklyCompleted, weeklyTarget);
        int progress = weeklyTarget > 0 ? (actualCompleted * 100) / weeklyTarget : 0;

        binding.weeklyProgress.setProgress(progress);
        binding.weeklyProgressText.setText(progress + "% completado esta semana (" + actualCompleted + "/" + weeklyTarget + ")");
    }

    // Liberamos la referencia al binding para evitar memory leaks
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
