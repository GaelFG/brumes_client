/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.gembasher.burnzombies;

/**
 *
 * @author Gaël
 */
public class Player {
    private int hpMax;
    private int manaMax;
    private double hp;
    private double mana;
    private int killCounter;
    private String nom;
    
    /**
     * 
     * @param pHpMax
     * @param pManaMax 
     */
    public Player(int pHpMax, int pManaMax) {
        hpMax = pHpMax;
        manaMax = pManaMax;
        killCounter = 0;
        hp = hpMax;
        mana = manaMax;
        nom = "Robert";
    }

    /**
     * @return the hpMax
     */
    public int getHpMax() {
        return hpMax;
    }

    /**
     * @param hpMax the hpMax to set
     */
    public void setHpMax(int hpMax) {
        this.hpMax = hpMax;
    }

    /**
     * @return the manaMax
     */
    public int getManaMax() {
        return manaMax;
    }

    /**
     * @param manaMax the manaMax to set
     */
    public void setManaMax(int manaMax) {
        this.manaMax = manaMax;
    }

    /**
     * @return the hp
     */
    public double getHp() {
        return hp;
    }

    /**
     * @param hp the hp to set
     */
    public void setHp(double hp) {
        this.hp = hp;
    }

    /**
     * @return the mana
     */
    public double getMana() {
        return mana;
    }

    /**
     * @param mana the mana to set
     */
    public void setMana(double mana) {
        this.mana = mana;
    }

    /**
     * @return the killCounter
     */
    public int getKillCounter() {
        return killCounter;
    }

    /**
     * @param killCounter the killCounter to set
     */
    public void setKillCounter(int killCounter) {
        this.killCounter = killCounter;
    }

    /**
     * @return the nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * @param nom the nom to set
     */
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    
}
