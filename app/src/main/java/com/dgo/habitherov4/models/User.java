package com.dgo.habitherov4.models;

public class User {
    private String name;
    private int level;
    private int currentExp;
    private int maxExp;
    private String profileImageUrl;
    
    public User() {
        // Constructor vacío requerido para Firebase
    }
    
    public User(String name, int level, int currentExp, int maxExp, String profileImageUrl) {
        this.name = name;
        this.level = level;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.profileImageUrl = profileImageUrl;
    }
    
    // Getters y setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getCurrentExp() { return currentExp; }
    public void setCurrentExp(int currentExp) { this.currentExp = currentExp; }
    
    public int getMaxExp() { return maxExp; }
    public void setMaxExp(int maxExp) { this.maxExp = maxExp; }
    
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    
    public int getExpPercentage() {
        if (maxExp == 0) return 0;
        return (int) ((currentExp * 100.0) / maxExp);
    }
}