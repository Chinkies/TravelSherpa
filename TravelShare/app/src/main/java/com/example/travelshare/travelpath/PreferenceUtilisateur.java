package com.example.travelshare.travelpath;

import java.util.List;

public class PreferenceUtilisateur {
    private List<String> activites;
    private double budgetMax;
    private int nbJours;
    private String niveauEffortMax;
    private List<String> sensibilitesMeteo;
    private String lieuObligatoire;
    private String ville;

    public PreferenceUtilisateur(List<String> activites, double budgetMax, int nbJours, String niveauEffortMax, List<String> sensibilitesMeteo, String lieuObligatoire, String ville){
        this.activites = activites;
        this.budgetMax = budgetMax;
        this.nbJours = nbJours;
        this.niveauEffortMax = niveauEffortMax;
        this.sensibilitesMeteo = sensibilitesMeteo;
        this.lieuObligatoire = lieuObligatoire;
        this.ville = ville;
    }

    public List<String> getActivites() { return activites; }
    public double getBudgetMax() { return budgetMax; }
    public int getNbJours() { return nbJours; }
    public String getNiveauEffortMax() { return niveauEffortMax; }
    public List<String> getSensibilitesMeteo() { return sensibilitesMeteo; }
    public String getLieuObligatoire() { return lieuObligatoire; }
    public String getVille() { return ville; }

    public void ajouterSensibiliteMeteo(String meteo) {
        if (!sensibilitesMeteo.contains(meteo)) {
            sensibilitesMeteo.add(meteo);
        }
    }
}