package com.example.travelshare.travelpath;

import com.example.travelshare.model.Lieu;
import java.io.Serializable;

public class Etape implements Serializable {
    private Lieu lieu;
    private String creneau;
    private int distanceDepuisEtapePrecedente;
    private int tempsTrajetMinute;
    private int jour;

    public Etape() {}

    public Etape(Lieu lieu, String creneau, int distanceDepuisEtapePrecedente, int tempsTrajetMinute, int jour) {
        this.lieu = lieu;
        this.creneau = creneau;
        this.distanceDepuisEtapePrecedente = distanceDepuisEtapePrecedente;
        this.tempsTrajetMinute = tempsTrajetMinute;
        this.jour = jour;
    }

    public Lieu getLieu() { return lieu; }
    public void setLieu(Lieu lieu) { this.lieu = lieu; }
    
    public String getCreneau() { return creneau; }
    public void setCreneau(String creneau) { this.creneau = creneau; }
    
    public int getDistanceDepuisEtapePrecedente() { return distanceDepuisEtapePrecedente; }
    public void setDistanceDepuisEtapePrecedente(int distanceDepuisEtapePrecedente) { this.distanceDepuisEtapePrecedente = distanceDepuisEtapePrecedente; }
    
    public int getTempsTrajetMinute() { return tempsTrajetMinute; }
    public void setTempsTrajetMinute(int tempsTrajetMinute) { this.tempsTrajetMinute = tempsTrajetMinute; }
    
    public int getJour() { return jour; }
    public void setJour(int jour) { this.jour = jour; }
}
