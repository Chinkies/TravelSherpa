package com.example.travelshare.model;

import com.google.firebase.firestore.GeoPoint;
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
    private String wikipediaTitle;
    private String realImageUrl;

    public Lieu() {}

    public Lieu(String id, String name, String categorie, double prixVisite,
                int dureeVisiteMinute, String niveauEffortRequis,
                boolean estEnExterieur, double latitude, double longitude, String horairesOuverture) {
        this.id = id;
        this.name = name;
        this.categorie = categorie;
        this.prixVisite = prixVisite;
        this.dureeVisiteMinute = dureeVisiteMinute;
        this.niveauEffortRequis = niveauEffortRequis;
        this.estEnExterieur = estEnExterieur;
        this.latitude = latitude;
        this.longitude = longitude;
        this.horairesOuverture = horairesOuverture;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    
    public double getPrixVisite() { return prixVisite; }
    public void setPrixVisite(double prixVisite) { this.prixVisite = prixVisite; }
    
    public int getDureeVisiteMinute() { return dureeVisiteMinute; }
    public void setDureeVisiteMinute(int dureeVisiteMinute) { this.dureeVisiteMinute = dureeVisiteMinute; }
    
    public String getNiveauEffortRequis() { return niveauEffortRequis; }
    public void setNiveauEffortRequis(String niveauEffortRequis) { this.niveauEffortRequis = niveauEffortRequis; }
    
    public boolean isEstEnExterieur() { return estEnExterieur; }
    public void setEstEnExterieur(boolean estEnExterieur) { this.estEnExterieur = estEnExterieur; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    
    public String getHorairesOuverture() { return horairesOuverture; }
    public void setHorairesOuverture(String horairesOuverture) { this.horairesOuverture = horairesOuverture; }
    
    public String getWikipediaTitle() { return wikipediaTitle; }
    public void setWikipediaTitle(String wikipediaTitle) { this.wikipediaTitle = wikipediaTitle; }
    
    public String getRealImageUrl() { return realImageUrl; }
    public void setRealImageUrl(String realImageUrl) { this.realImageUrl = realImageUrl; }

    public GeoPoint getGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }
}
