package com.dgo.habitherov4.models;

import com.google.firebase.Timestamp;

public class InventoryReward {
    private String id;
    private String title;
    private String description;
    private Timestamp obtainedAt;
    private boolean used;

    public InventoryReward() {
        // Constructor vacío requerido por Firebase
    }

    public InventoryReward(String title, String description) {
        this.title = title;
        this.description = description;
        this.obtainedAt = Timestamp.now();
        this.used = false;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getObtainedAt() { return obtainedAt; }
    public void setObtainedAt(Timestamp obtainedAt) { this.obtainedAt = obtainedAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}