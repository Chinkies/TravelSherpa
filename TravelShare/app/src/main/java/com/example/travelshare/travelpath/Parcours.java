package com.example.travelshare.travelpath;

import java.io.Serializable;
import java.util.List;

public class Parcours implements Serializable {
    private String id;
    private String createur_id;
    private String nomOption;
    private List<Etape> listeEtapes;
    private double budgetTotal;
    private int dureeTotale;
    private String niveauDifficulte;

    public Parcours() {}

    public Parcours(String nomOption, List<Etape> listeEtapes, double budgetTotal, int dureeTotale, String niveauDifficulte) {
        this.nomOption = nomOption;
        this.listeEtapes = listeEtapes;
        this.budgetTotal = budgetTotal;
        this.dureeTotale = dureeTotale;
        this.niveauDifficulte = niveauDifficulte;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreateur_id() { return createur_id; }
    public void setCreateur_id(String createur_id) { this.createur_id = createur_id; }

    public String getNomOption() { return nomOption; }
    public void setNomOption(String nomOption) { this.nomOption = nomOption; }

    public List<Etape> getListeEtapes() { return listeEtapes; }
    public void setListeEtapes(List<Etape> listeEtapes) { this.listeEtapes = listeEtapes; }

    public double getBudgetTotal() { return budgetTotal;}
    public void setBudgetTotal(double budgetTotal) { this.budgetTotal = budgetTotal; }

    public int getDureeTotale() { return dureeTotale; }
    public void setDureeTotale(int dureeTotale) { this.dureeTotale = dureeTotale; }

    public String getNiveauDifficulte() { return niveauDifficulte; }
    public void setNiveauDifficulte(String niveauDifficulte) { this.niveauDifficulte = niveauDifficulte; }
}
