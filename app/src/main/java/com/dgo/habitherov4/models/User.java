package com.dgo.habitherov4.models;

public class User {
    private String id;
    private String name;
    private String email;
    private int level;
    
    // Estadísticas de Maná
    private int currentMana;
    private int maxMana;
    
    // Estadísticas de Vida
    private int currentHp;
    private int maxHp;
    
    // Estadísticas de Experiencia
    private int currentExp;
    private int maxExp;
    
    private String profileImageUrl;
    
    public User() {
        // Constructor vacío requerido para Firebase
        // Valores por defecto FIJOS
        this.currentHp = 5; // Vida a tope
        this.maxHp = 5;
        this.currentMana = 1; // MP a 1
        this.maxMana = 5;
        this.currentExp = 0; // EXP a 0
        this.maxExp = 5;
        this.level = 1;
    }
    
    public User(String name, int level, int currentMana, int maxMana, String profileImageUrl) {
        this.name = name;
        this.level = level;
        this.currentMana = currentMana;
        this.maxMana = maxMana;
        this.profileImageUrl = profileImageUrl;
        
        // Inicializar valores por defecto para HP y EXP
        this.currentHp = 5;
        this.maxHp = 5;
        this.currentExp = 0;
        this.maxExp = 5;
    }
    
    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    
    public int getCurrentMana() { return currentMana; }
    public void setCurrentMana(int currentMana) { this.currentMana = currentMana; }
    
    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int maxMana) { this.maxMana = maxMana; }
    
    public int getManaPercentage() {
        if (maxMana == 0) return 0;
        return (int) ((currentMana * 100.0) / maxMana);
    }
    
    // Método para añadir maná basado en dificultad
    public void addMana(int manaToAdd) {
        this.currentMana = Math.min(this.currentMana + manaToAdd, this.maxMana);
    }
    
    // Getters y setters para HP
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    
    public int getHpPercentage() {
        if (maxHp == 0) return 0;
        return (int) ((currentHp * 100.0) / maxHp);
    }
    
    // Getters y setters para EXP
    public int getCurrentExp() { return currentExp; }
    public void setCurrentExp(int currentExp) { this.currentExp = currentExp; }
    
    public int getMaxExp() { return maxExp; }
    public void setMaxExp(int maxExp) { this.maxExp = maxExp; }
    
    public int getExpPercentage() {
        if (maxExp == 0) return 0;
        return (int) ((currentExp * 100.0) / maxExp);
    }
    
    // Método para añadir experiencia
    public void addExp(int expToAdd) {
        this.currentExp = Math.min(this.currentExp + expToAdd, this.maxExp);
        
        // Verificar si debe subir de nivel
        if (this.currentExp >= this.maxExp) {
            levelUp();
        }
    }
    
    // Método para perder vida
    public void loseHp(int hpToLose) {
        this.currentHp = Math.max(this.currentHp - hpToLose, 0);
    }
    
    // Método para recuperar vida
    public void restoreHp(int hpToRestore) {
        this.currentHp = Math.min(this.currentHp + hpToRestore, this.maxHp);
    }
    
    // Método para subir de nivel
    private void levelUp() {
        this.level++;
        this.currentExp = 0; // Resetear experiencia
        this.maxExp += 2; // Aumentar EXP máxima requerida
        this.maxHp = Math.min(this.maxHp + 1, 5); // Aumentar HP máxima (máximo 5)
        this.maxMana = Math.min(this.maxMana + 1, 5); // Aumentar MP máxima (máximo 5)
        this.currentHp = this.maxHp; // Restaurar HP completa al subir nivel
    }
    
    // MÉTODOS CORREGIDOS PARA OBTENER EL NIVEL DE BARRA
    public int getHpBarLevel() {
        int level = Math.max(0, Math.min(5, currentHp));
        android.util.Log.d("User", "HP: " + currentHp + "/" + maxHp + " -> bar level " + level);
        return level;
    }
    
    public int getManaBarLevel() {
        int level = Math.max(0, Math.min(5, currentMana));
        android.util.Log.d("User", "MP: " + currentMana + "/" + maxMana + " -> bar level " + level);
        return level;
    }
    
    public int getExpBarLevel() {
        int level = Math.max(0, Math.min(5, currentExp));
        android.util.Log.d("User", "EXP: " + currentExp + "/" + maxExp + " -> bar level " + level);
        return level;
    }
}