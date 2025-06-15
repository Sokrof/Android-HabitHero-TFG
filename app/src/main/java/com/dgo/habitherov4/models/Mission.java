package com.dgo.habitherov4.models;

public class Mission {
    private String id;
    private String title;
    private String description;
    private String category;
    private boolean isCompleted;
    private int manaReward;
    private String iconType;
    private String difficulty;
    private long deadlineTimestamp;
    private boolean isExpired;
    private String timeUnit;
    private int timeAmount;
    private boolean isDailyMission;

    // Constructor vacío requerido para Firebase
    public Mission() {
    }

    public Mission(String id, String title, String description, String category,
                   boolean isCompleted, int manaReward, String iconType, String difficulty) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.isCompleted = isCompleted;
        this.manaReward = manaReward;
        this.iconType = iconType;
        this.difficulty = difficulty;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getManaReward() {
        return manaReward;
    }

    public long getDeadlineTimestamp() {
        return deadlineTimestamp;
    }

    public void setDeadlineTimestamp(long deadlineTimestamp) {
        this.deadlineTimestamp = deadlineTimestamp;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }

    public void setTimeAmount(int timeAmount) {
        this.timeAmount = timeAmount;
    }

    /// /////////////////////////////////////////////////////////////////////////////////
    // Metodo para fijar dificultad y maná asociado.
    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        // Asignar automáticamente el maná basado en la dificultad
        switch (difficulty) {
            case "Fácil":
                this.manaReward = 1;  // Fácil = 1 maná
                break;
            case "Medio":
                this.manaReward = 3;  // Medio = 3 maná
                break;
            case "Difícil":
                this.manaReward = 5;  // Difícil = 5 maná
                break;
            default:
                this.manaReward = 1;
                break;
        }
    }

    // Metodo para calcular si la misión ha expirado
    public boolean checkIfExpired() {
        long currentTime = System.currentTimeMillis();
        this.isExpired = currentTime > deadlineTimestamp;
        return this.isExpired;
    }

    // Metodo para calcular el timestamp de deadline ( Tiempo límite ) basado en tiempo y unidad
    public void calculateDeadline() {
        long currentTime = System.currentTimeMillis();
        long additionalTime = 0;

        switch (timeUnit.toLowerCase()) {
            case "minutos":
                additionalTime = timeAmount * 60 * 1000L;
                break;
            case "horas":
                additionalTime = timeAmount * 60 * 60 * 1000L;
                break;
            case "días":
                additionalTime = timeAmount * 24 * 60 * 60 * 1000L;
                break;
            case "semanas":
                additionalTime = timeAmount * 7 * 24 * 60 * 60 * 1000L;
                break;
            case "meses":
                additionalTime = timeAmount * 30L * 24 * 60 * 60 * 1000L;
                break;
            case "años":
                additionalTime = timeAmount * 365L * 24 * 60 * 60 * 1000L;
                break;
        }
        this.deadlineTimestamp = currentTime + additionalTime;
    }

    public boolean isDailyMission() {
        return isDailyMission;
    }

    // Metodo para configurar automáticamente misiones diarias
    public void setupAsDailyMission() {
        this.isDailyMission = true;
        this.timeUnit = "días";
        this.timeAmount = 1;
        calculateDeadline();
    }
}