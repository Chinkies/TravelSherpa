package com.example.travelshare.travelpath;

import android.location.Location;
import com.example.travelshare.model.Lieu;

public class UtilitaireGeographique {
    public static float calculerDistance(Lieu lieuA, Lieu lieuB) {
        float[] resultats = new float[1];

        Location.distanceBetween(
                lieuA.getLatitude(), lieuA.getLongitude(),
                lieuB.getLatitude(), lieuB.getLongitude(),
                resultats
        );

        return resultats[0];
    }

    public static int estimerTempsMarche(float distanceMetres) {
        float distanceReelleEstimee = distanceMetres * 1.3f;

        int minutes = Math.round(distanceReelleEstimee / 75f);
        return minutes;
    }
}
