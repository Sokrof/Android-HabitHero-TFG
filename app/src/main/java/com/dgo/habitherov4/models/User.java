package com.dgo.habitherov4.models;

public class User {
    private String id;
    private String name;
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

    // La idea era asignar tu foto de google a un perfil de "juego". De momento
    // no supe como hacerlo, lo dejo para futura investigación.
    private String profileImageUrl;


    public User() {
        // Valores por defecto FIJOS
        this.currentHp = 5; // Vida a tope
        this.maxHp = 5;
        this.currentMana = 1; // MP a 1
        this.maxMana = 5;
        this.currentExp = 0; // EXP a 0
        this.maxExp = 5;
        this.level = 1;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(int currentMana) {
        this.currentMana = currentMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    // Getters y setters para HP
    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    // Getters y setters para EXP
    public int getCurrentExp() {
        return currentExp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    // Metodo para añadir experiencia
    public void addExp(int expToAdd) {
        this.currentExp += expToAdd;

        // Verificar si debe subir de nivel (cuando EXP llega a 5)
        while (this.currentExp >= 5) {
            this.currentExp -= 5; // Reiniciar EXP restando 5
            levelUp();
        }
    }

    // Metodo para subir de nivel
    private void levelUp() {
        this.level++;
        // Ya no modificamos maxExp, se mantiene en 5
        this.maxHp = Math.min(this.maxHp + 1, 5); // Aumentar HP máxima (máximo 5)
        this.maxMana = Math.min(this.maxMana + 1, 5); // Aumentar MP máxima (máximo 5)
        this.currentHp = this.maxHp; // Restaurar HP completa al subir nivel
    }


    // Metodo para añadir maná basado en dificultad
    public void addMana(int manaToAdd) {
        this.currentMana = Math.min(this.currentMana + manaToAdd, this.maxMana);
    }

    // Asignar VISUALMENTe HP, MP y EXP con los PNG de Drawable.
    // Calcula el nivel de la barra para mostrar en la UI
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