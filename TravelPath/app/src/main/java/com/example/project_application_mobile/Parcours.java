package com.example.project_application_mobile;

import java.io.Serializable;
import java.util.List;

public class Parcours implements Serializable {
    private String nomOption;
    private List<Etape> listeEtapes;
    private double budgetTotal;
    private int dureeTotale;
    private String niveauDifficulte;

    public Parcours(String nomOption, List<Etape> listeEtapes, double budgetTotal, int dureeTotale, String niveauDifficulte) {
        this.nomOption = nomOption;
        this.listeEtapes = listeEtapes;
        this.budgetTotal = budgetTotal;
        this.dureeTotale = dureeTotale;
        this.niveauDifficulte = niveauDifficulte;
    }

    public String getNomOption() { return nomOption; }
    public List<Etape> getListeEtapes() { return listeEtapes; }
    public double getBudgetTotal() { return budgetTotal;}
    public int getDureeTotale() { return dureeTotale; }
    public String getNiveauDifficulte() { return niveauDifficulte; }
}
