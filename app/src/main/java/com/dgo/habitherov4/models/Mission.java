package com.dgo.habitherov4.models;

public class Mission {
    private String id;
    private String title;
    private String description;
    private String category;
    private boolean isCompleted;
    private int progress;
    private int maxProgress;
    private int expReward;
    private String iconType;
    private String difficulty; // Nuevo campo para dificultad
    private long deadlineTimestamp; // Timestamp de cuando expira la misión
    private boolean isExpired;
    private String timeUnit; // "minutos", "horas", "días", "semanas", "meses", "años"
    private int timeAmount; // Cantidad de tiempo
    
    public Mission() {
        // Constructor vacío requerido para Firebase
    }
    
    public Mission(String id, String title, String description, String category, 
                   boolean isCompleted, int progress, int maxProgress, int expReward, String iconType, String difficulty) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.isCompleted = isCompleted;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.expReward = expReward;
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
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    
    public int getMaxProgress() { return maxProgress; }
    public void setMaxProgress(int maxProgress) { this.maxProgress = maxProgress; }
    
    public int getExpReward() { return expReward; }
    public void setExpReward(int expReward) { this.expReward = expReward; }
    
    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { 
        this.difficulty = difficulty;
        // Asignar automáticamente la experiencia basada en la dificultad
        switch (difficulty) {
            case "Fácil":
                this.expReward = 50;
                break;
            case "Medio":
                this.expReward = 160;
                break;
            case "Difícil":
                this.expReward = 380;
                break;
            default:
                this.expReward = 50;
                break;
        }
    }
    
    public String getProgressText() {
        if (category.equals("Diaria") && maxProgress == 1) {
            return isCompleted ? "Completado" : "Pendiente";
        }
        return progress + "/" + maxProgress;
    }
    
    // Getters y setters para los nuevos campos
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
    
    public String getTimeUnit() {
        return timeUnit;
    }
    
    public void setTimeUnit(String timeUnit) {
        this.timeUnit = timeUnit;
    }
    
    public int getTimeAmount() {
        return timeAmount;
    }
    
    public void setTimeAmount(int timeAmount) {
        this.timeAmount = timeAmount;
    }
    
    // Método para calcular si la misión ha expirado
    public boolean checkIfExpired() {
        long currentTime = System.currentTimeMillis();
        this.isExpired = currentTime > deadlineTimestamp;
        return this.isExpired;
    }
    
    // Método para calcular el timestamp de deadline basado en tiempo y unidad
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
                additionalTime = timeAmount * 30L * 24 * 60 * 60 * 1000L; // Aproximado
                break;
            case "años":
                additionalTime = timeAmount * 365L * 24 * 60 * 60 * 1000L; // Aproximado
                break;
        }
        
        this.deadlineTimestamp = currentTime + additionalTime;
    }
}