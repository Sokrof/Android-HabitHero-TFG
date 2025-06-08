package com.dgo.habitherov4.models;

public class Reward {
    private String id;
    private String title;
    private String description;
    private boolean claimed;
    private long createdAt;

    public Reward() {
        // Constructor vacío requerido para Firebase
    }

    public Reward(String title, String description) {
        this.title = title;
        this.description = description;
        this.claimed = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}