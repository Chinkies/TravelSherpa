package com.example.travelshare.travelpath;

import com.example.travelshare.model.Lieu;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParcoursGenerator {

    private int getValeurEffort(String effort) {
        if (effort == null) return 0;
        switch (effort) {
            case "Facile": return 1;
            case "Medium": return 2;
            case "Difficile": return 3;
            case "Sportif": return 4;
            default: return 0;
        }
    }

    public List<Lieu> filtrerLieu(PreferenceUtilisateur prefs, List<Lieu> toutLeslieux, boolean meteoActuelleMauvaise) {
        List<Lieu> lieuxFiltres = new ArrayList<>();
        int maxEffortTolerable = getValeurEffort(prefs.getNiveauEffortMax());

        for (Lieu lieu : toutLeslieux) {

            if (!prefs.getActivites().isEmpty() && !prefs.getActivites().contains(lieu.getCategorie())) {
                continue;
            }
            if(lieu.getPrixVisite() > prefs.getBudgetMax()){
                continue;
            }
            if (getValeurEffort(lieu.getNiveauEffortRequis()) > maxEffortTolerable) {
                continue;
            }
            if (lieu.isEstEnExterieur()) {
                List<String> meteo = prefs.getSensibilitesMeteo();
                if (meteo != null && (meteo.contains("Froid") || meteo.contains("Humidité") ||
                        meteo.contains("Chaleur") || meteo.contains("Vent") || meteo.contains("Soleil"))) {
                    continue;
                }
            }
            lieuxFiltres.add(lieu);
        }

        if (prefs.getLieuObligatoire() != null && !prefs.getLieuObligatoire().isEmpty()) {
            for (Lieu l : toutLeslieux) {
                if (l.getName().toLowerCase().contains(prefs.getLieuObligatoire().toLowerCase())) {
                    if (!lieuxFiltres.contains(l)) lieuxFiltres.add(0, l);
                    break;
                }
            }
        }

        return lieuxFiltres;
    }

    public List<Parcours> optionGenerator(PreferenceUtilisateur prefs, List<Lieu> lieuxFiltres) {
        List<Parcours> optionsProposees = new ArrayList<>();
        int nbJours = prefs.getNbJours();
        List<String> idLieuxDepartUtilises = new ArrayList<>();

        // Variation Économique
        List<Lieu> lieuxEco = new ArrayList<>(lieuxFiltres);
        Collections.sort(lieuxEco, (o1, o2) -> Double.compare(o1.getPrixVisite(), o2.getPrixVisite()));
        optionsProposees.addAll(genererVariations("Économique", lieuxEco, prefs.getBudgetMax(), nbJours, 3, idLieuxDepartUtilises));

        // Variation Confort
        List<Lieu> lieuxConfort = new ArrayList<>(lieuxFiltres);
        Collections.sort(lieuxConfort, (o1, o2) -> getValeurEffort(o1.getNiveauEffortRequis()) - getValeurEffort(o2.getNiveauEffortRequis()));
        optionsProposees.addAll(genererVariations("Confort", lieuxConfort, prefs.getBudgetMax(), nbJours, 3, idLieuxDepartUtilises));

        // Variation Équilibrée
        List<Lieu> lieuxEquilibre = new ArrayList<>(lieuxFiltres);
        Collections.shuffle(lieuxEquilibre);
        optionsProposees.addAll(genererVariations("Équilibré", lieuxEquilibre, prefs.getBudgetMax(), nbJours, 3, idLieuxDepartUtilises));

        return optionsProposees;
    }

    private List<Parcours> genererVariations(String type, List<Lieu> lieuxTries, double budgetMax, int nbJours, int nbVariations, List<String> idLieuxDepartUtilises) {
        List<Parcours> resultats = new ArrayList<>();
        int variationsTrouvees = 0;
        int index = 0;

        while (variationsTrouvees < nbVariations && index < lieuxTries.size()) {
            Lieu lieuDepart = lieuxTries.get(index);
            index++;

            if (idLieuxDepartUtilises.contains(lieuDepart.getId())) continue;

            List<Lieu> lieuxDisponibles = new ArrayList<>(lieuxTries);
            lieuxDisponibles.remove(lieuDepart);
            lieuxDisponibles.add(0, lieuDepart);

            String nomDynamique = type + " - " + lieuDepart.getName();
            Parcours p = constuireParcours(nomDynamique, lieuxDisponibles, budgetMax, nbJours);
            if (p != null) {
                resultats.add(p);
                idLieuxDepartUtilises.add(lieuDepart.getId());
                variationsTrouvees++;
            }
        }
        return resultats;
    }

    private Parcours constuireParcours(String nomOption, List<Lieu> lieuTries, double budgetMax, int nbJours) {
        List<Etape> etapes = new ArrayList<>();
        List<Lieu> restosDispos = new ArrayList<>();
        List<Lieu> barsDispos = new ArrayList<>();
        List<Lieu> activitesDispos = new ArrayList<>();

        for (Lieu l : lieuTries) {
            if ("Restauration".equals(l.getCategorie())) restosDispos.add(l);
            else if ("Bars".equals(l.getCategorie())) barsDispos.add(l);
            else activitesDispos.add(l);
        }

        double budgetDepense = 0;
        int dureeTotale = 0;
        Lieu dernierLieuVisite = null;

        for (int jourActuel = 1; jourActuel <= nbJours; jourActuel++) {
            int tempsEcouleCeJour = 0;
            boolean aMangeCeMidi = false;

            while (tempsEcouleCeJour < 420) {
                Lieu candidatRetenu = null;
                float distanceMin = Float.MAX_VALUE;
                int tempsTrajetRetenu = 0;

                boolean cEstLHeureDuResto = (!aMangeCeMidi && tempsEcouleCeJour >= 180 && tempsEcouleCeJour <= 300 && !restosDispos.isEmpty());

                List<Lieu> listeCible;
                if (cEstLHeureDuResto) {
                    listeCible = restosDispos;
                } else {
                    listeCible = activitesDispos;
                }

                if (listeCible.isEmpty()) {
                    if (cEstLHeureDuResto) {
                        aMangeCeMidi = true;
                        continue;
                    }
                    break;
                }

                for (Lieu l : listeCible) {
                    float distance = (dernierLieuVisite == null) ? 0 : UtilitaireGeographique.calculerDistance(dernierLieuVisite, l);
                    int trajet = (dernierLieuVisite == null) ? 0 : UtilitaireGeographique.estimerTempsMarche(distance);

                    int dureeLieu = l.getDureeVisiteMinute();

                    if (tempsEcouleCeJour + dureeLieu + trajet <= 480
                            && budgetDepense + l.getPrixVisite() <= budgetMax) {
                        if (distance < distanceMin) {
                            distanceMin = distance;
                            candidatRetenu = l;
                            tempsTrajetRetenu = trajet;
                        }
                    }
                }

                if (candidatRetenu != null) {
                    String creneau;
                    int dureeEffective = candidatRetenu.getDureeVisiteMinute();

                    if ("Restauration".equals(candidatRetenu.getCategorie())) {
                        creneau = "Déjeuner";
                        aMangeCeMidi = true;
                        restosDispos.remove(candidatRetenu);
                    } else {
                        creneau = (tempsEcouleCeJour < 240) ? "Matin" : "Après-midi";
                        activitesDispos.remove(candidatRetenu);
                    }

                    etapes.add(new Etape(candidatRetenu, creneau, (int) distanceMin, tempsTrajetRetenu, jourActuel));
                    budgetDepense += candidatRetenu.getPrixVisite();
                    tempsEcouleCeJour += (dureeEffective + tempsTrajetRetenu);
                    dureeTotale += (dureeEffective + tempsTrajetRetenu);
                    dernierLieuVisite = candidatRetenu;
                } else {
                    if (cEstLHeureDuResto) aMangeCeMidi = true;
                    else break;
                }
            }

            if (!barsDispos.isEmpty()) {
                Lieu barRetenu = null;
                float distMinBar = Float.MAX_VALUE;
                int trajetBar = 0;

                for (Lieu b : barsDispos) {
                    float distance = (dernierLieuVisite == null) ? 0 : UtilitaireGeographique.calculerDistance(dernierLieuVisite, b);
                    int trajet = (dernierLieuVisite == null) ? 0 : UtilitaireGeographique.estimerTempsMarche(distance);

                    if (budgetDepense + b.getPrixVisite() <= budgetMax) {
                        if (distance < distMinBar) {
                            distMinBar = distance;
                            barRetenu = b;
                            trajetBar = trajet;
                        }
                    }
                }

                if (barRetenu != null) {
                    etapes.add(new Etape(barRetenu, "Soirée", (int) distMinBar, trajetBar, jourActuel));
                    budgetDepense += barRetenu.getPrixVisite();

                    int dureeBar = 45;
                    dureeTotale += (dureeBar + trajetBar);

                    dernierLieuVisite = barRetenu;
                    barsDispos.remove(barRetenu);
                }
            }
        }

        if (etapes.isEmpty()) return null;

        int maxEffort = 0;
        String effortGlobal = "Facile";
        for (Etape e : etapes) {
            int effortLieu = getValeurEffort(e.getLieu().getNiveauEffortRequis());
            if (effortLieu > maxEffort) {
                maxEffort = effortLieu;
                effortGlobal = e.getLieu().getNiveauEffortRequis();
            }
        }
        return new Parcours(nomOption, etapes, budgetDepense, dureeTotale, effortGlobal);
    }
}