package com.example.project_application_mobile;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public class OpenStreetMapClient {

    private static final String BASE_URL = "https://overpass.openstreetmap.fr/";
    private static OverpassApi api;

    public static OverpassApi getApi() {
        if (api == null) {
            // 1. On crée un client HTTP qui est très patient (60 secondes)
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            // 2. On l'ajoute à Retrofit
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // <-- C'EST LA LIGNE MAGIQUE
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(OverpassApi.class);
        }
        return api;
    }

    public static List<Lieu> convertirEnLieux(OverpassResponse response) {
        List<Lieu> listeLieux = new ArrayList<>();

        if (response == null || response.elements == null) return listeLieux;

        for (OverpassResponse.Element element : response.elements) {
            if (element.tags == null || element.tags.name == null) continue;

            String nom = element.tags.name;
            String id = String.valueOf(element.id);

            double lat = element.lat;
            double lon = element.lon;
            if (element.center != null) {
                lat = element.center.lat;
                lon = element.center.lon;
            }

            String categorie = "Sites culturels";
            double prix = 10.0;
            int duree = 60;
            String effort = "Facile";
            boolean exterieur = false;

            if ("museum".equals(element.tags.tourism)) {
                categorie = "Musées"; prix = 15.0; duree = 120; effort = "Facile";
            } else if ("monument".equals(element.tags.historic) || "castle".equals(element.tags.historic)) {
                categorie = "Monuments"; prix = 12.0; duree = 60; effort = "Medium"; exterieur = true;
            } else if ("park".equals(element.tags.leisure) || "garden".equals(element.tags.leisure)) {
                categorie = "Parcs"; prix = 0.0; duree = 90; effort = "Facile"; exterieur = true;
            } else if ("attraction".equals(element.tags.tourism)) {
                categorie = "Sites culturels"; prix = 10.0; duree = 60; effort = "Medium"; exterieur = true;
            } else if ("restaurant".equals(element.tags.amenity) || "cafe".equals(element.tags.amenity)) {
                categorie = "Restauration"; prix = 25.0; duree = 90; effort = "Facile";
            } else if ("theme_park".equals(element.tags.tourism)) {
                categorie = "Parcs d'attractions"; prix = 80.0; duree = 480; effort = "Sportif"; exterieur = true;
            } else if ("bar".equals(element.tags.amenity) || "pub".equals(element.tags.amenity)) {
                categorie = "Bars"; prix = 15.0; duree = 90; effort = "Facile";
            }

            String horaires = "Horaires non précisés";
            if (element.tags.opening_hours != null) {
                horaires = element.tags.opening_hours;
            } else if (exterieur) {
                horaires = "Accès libre (24h/24)";
            }
            Lieu nouveauLieu = new Lieu(id, nom, categorie, prix, duree, effort, exterieur, lat, lon, horaires);
            if (element.tags.wikipedia != null) {
                String wikiTag = element.tags.wikipedia;
                if (wikiTag.contains(":")) {
                    nouveauLieu.setWikipediaTitle(wikiTag.split(":", 2)[1]);
                } else {
                    nouveauLieu.setWikipediaTitle(wikiTag);
                }
            }

            listeLieux.add(nouveauLieu);
        }
        return listeLieux;
    }
}