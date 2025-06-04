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
    
    public Mission() {
        // Constructor vacío requerido para Firebase
    }
    
    public Mission(String id, String title, String description, String category, 
                   boolean isCompleted, int progress, int maxProgress, int expReward, String iconType) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.isCompleted = isCompleted;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.expReward = expReward;
        this.iconType = iconType;
    }
    
    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
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
    
    public String getProgressText() {
        if (category.equals("Diaria") && maxProgress == 1) {
            return isCompleted ? "Completado" : "Pendiente";
        }
        return progress + "/" + maxProgress;
    }
}