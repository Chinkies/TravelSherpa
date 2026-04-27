package com.example.project_application_mobile;

import java.io.Serializable;

public class Lieu implements Serializable {
    private String id;
    private String name;
    private String categorie;
    private double prixVisite;
    private int dureeVisiteMinute;
    private String niveauEffortRequis;
    private boolean estEnExterieur;
    private double latitude;
    private double longitude;
    private String horairesOuverture;
    private String wikipediaTitle = null;
    private String realImageUrl = null;

    public Lieu(String id, String name, String categorie, double prixVisite,
                int dureeVisiteMinutes, String niveauEffortRequis,
                boolean estEnExterieur, double latitude, double longitude, String horairesOuverture) {
        this.id = id;
        this.name = name;
        this.categorie = categorie;
        this.prixVisite = prixVisite;
        this.dureeVisiteMinute = dureeVisiteMinutes;
        this.niveauEffortRequis = niveauEffortRequis;
        this.estEnExterieur = estEnExterieur;
        this.latitude = latitude;
        this.longitude = longitude;
        this.horairesOuverture = horairesOuverture;

    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategorie() { return categorie; }
    public double getPrixVisite() { return prixVisite; }
    public int getDureeVisiteMinute() { return dureeVisiteMinute; }
    public String getNiveauEffortRequis() { return niveauEffortRequis; }
    public boolean isEstEnExterieur() { return estEnExterieur; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getHorairesOuverture() { return horairesOuverture; }
    public String getRealImageUrl() { return realImageUrl; }
    public void setRealImageUrl(String realImageUrl) { this.realImageUrl = realImageUrl; }
    public String getWikipediaTitle() { return wikipediaTitle; }
    public void setWikipediaTitle(String wikipediaTitle) { this.wikipediaTitle = wikipediaTitle; }
}
