package com.example.project_application_mobile;

import java.io.Serializable;

public class Etape implements Serializable {
    private Lieu lieu;
    private String creneau;
    private int distanceDepuisEtapePrecedente;
    private int tempsTrajetMinute;
    private int jour;

    public Etape(Lieu lieu, String creneau, int distanceDepuisEtapePrecedente, int tempsTrajetMinute, int jour) {
        this.lieu = lieu;
        this.creneau = creneau;
        this.distanceDepuisEtapePrecedente = distanceDepuisEtapePrecedente;
        this.tempsTrajetMinute = tempsTrajetMinute;
        this.jour = jour;

    }

    // Getters
    public Lieu getLieu() { return lieu; }
    public String getCreneau() { return creneau; }
    public int getDistanceDepuisEtapePrecedente() { return distanceDepuisEtapePrecedente; }
    public int getTempsTrajetMinutes() { return tempsTrajetMinute; }
    public int getJour() { return jour; }
}
