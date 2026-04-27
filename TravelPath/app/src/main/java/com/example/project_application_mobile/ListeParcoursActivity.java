package com.example.project_application_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListeParcoursActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_parcours);

        recyclerView = findViewById(R.id.recyclerViewParcours);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences sharedPreferences = getSharedPreferences("TravelPath", MODE_PRIVATE);
        double budgetMax = sharedPreferences.getFloat("BUDGET", 150f);
        int nbJours = sharedPreferences.getInt("JOURS", 3);
        String niveauEffort = sharedPreferences.getString("EFFORT", "Medium");
        String lieuFavori = sharedPreferences.getString("LIEU_FAVORI", "");
        String villeDestination = sharedPreferences.getString("VILLE", "Paris");

        List<String> activitesChoisies = getIntent().getStringArrayListExtra("ACTIVITES_CHOISIES");
        if (activitesChoisies == null) activitesChoisies = new ArrayList<>();

        List<String> meteoChoisies = getIntent().getStringArrayListExtra("METEO_CHOISIES");
        if (meteoChoisies == null) meteoChoisies = new ArrayList<>();

        PreferenceUtilisateur prefs = new PreferenceUtilisateur(
                activitesChoisies, budgetMax, nbJours, niveauEffort, meteoChoisies, lieuFavori, villeDestination
                );

        double latTemp = 48.8529;
        double lonTemp = 2.3499;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(prefs.getVille(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                latTemp = address.getLatitude();
                lonTemp = address.getLongitude();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        final double finalLat = latTemp;
        final double finalLon = lonTemp;

        Toast.makeText(this, "Vérification de la météo à " + prefs.getVille() + "...", Toast.LENGTH_SHORT).show();

        WeatherClient.getApi().getCurrentWeather(finalLat, finalLon).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().current_weather != null) {
                    double temp = response.body().current_weather.temperature;
                    int code = response.body().current_weather.weathercode;
                    double wind = response.body().current_weather.windspeed;

                    if (code >= 51) {
                        prefs.ajouterSensibiliteMeteo("Humidité");
                        Toast.makeText(ListeParcoursActivity.this, "Il pleut️ ! Musées et intérieur privilégiés.", Toast.LENGTH_LONG).show();

                    } else if (wind > 40.0) {
                        prefs.ajouterSensibiliteMeteo("Vent");
                        Toast.makeText(ListeParcoursActivity.this, "Beaucoup de vent ! On évite les parcs.", Toast.LENGTH_LONG).show();

                    } else if (temp > 30.0) {
                        prefs.ajouterSensibiliteMeteo("Chaleur");
                        Toast.makeText(ListeParcoursActivity.this, "Alerte Chaleur (" + temp + "°C) ! On reste au frais.", Toast.LENGTH_LONG).show();

                    } else if (temp < 5.0) {
                        prefs.ajouterSensibiliteMeteo("Froid");
                        Toast.makeText(ListeParcoursActivity.this, "Il fait froid  ! On va se mettre au chaud.", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(ListeParcoursActivity.this, "Météo idéale ️ (" + temp + "°C).", Toast.LENGTH_SHORT).show();
                    }
                }

                lancerRechercheOSM(finalLat, finalLon, prefs);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                lancerRechercheOSM(finalLat, finalLon, prefs);
            }
        });
    }

    private void lancerRechercheOSM(double centreLat, double centreLon, PreferenceUtilisateur prefs) {

        int rayon = prefs.getNbJours() > 3 ? 10000 : 5000;

        String queryOSM = "[out:json][timeout:30];" +
                "(" +
                "nwr(around:" + rayon + "," + centreLat + "," + centreLon + ")[\"tourism\"=\"museum\"][\"wikipedia\"];" +
                "nwr(around:" + rayon + "," + centreLat + "," + centreLon + ")[\"historic\"=\"monument\"][\"wikipedia\"];" +
                "nwr(around:" + rayon + "," + centreLat + "," + centreLon + ")[\"tourism\"=\"attraction\"][\"wikipedia\"];" +
                "nwr(around:" + rayon + "," + centreLat + "," + centreLon + ")[\"leisure\"=\"park\"][\"wikipedia\"];" +
                "node(around:" + rayon + "," + centreLat + "," + centreLon + ")[\"amenity\"=\"restaurant\"][\"name\"];" +
                ");" +
                "out center;";

        Toast.makeText(this, "Recherche des lieux en cours...", Toast.LENGTH_SHORT).show();

        OpenStreetMapClient.getApi().getLieux(queryOSM).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {
                        List<Lieu> vraisLieux = OpenStreetMapClient.convertirEnLieux(response.body());

                        ParcoursGenerator moteur = new ParcoursGenerator();
                        List<Lieu> lieuxFiltres = moteur.filtrerLieu(prefs, vraisLieux, false);

                        List<Lieu> activites = new ArrayList<>();
                        List<Lieu> restos = new ArrayList<>();

                        for (Lieu l : lieuxFiltres) {
                            if ("Restauration".equals(l.getCategorie())) {
                                restos.add(l);
                            } else {
                                activites.add(l);
                            }
                        }

                        if (activites.size() > 150) activites = activites.subList(0, 150);
                        if (restos.size() > 50) restos = restos.subList(0, 50);

                        lieuxFiltres.clear();
                        lieuxFiltres.addAll(activites);
                        lieuxFiltres.addAll(restos);

                        android.util.Log.e("DEBUG_PARCOURS", "Activités gardées : " + activites.size() + " | Restos gardés : " + restos.size());

                        List<Parcours> optionsGenerees = moteur.optionGenerator(prefs, lieuxFiltres);

                        handler.post(() -> {
                            if (optionsGenerees != null && !optionsGenerees.isEmpty()) {
                                ParcoursAdapter adapter = new ParcoursAdapter(optionsGenerees, parcoursClique -> {
                                    Intent intent = new Intent(ListeParcoursActivity.this, ResultatActivity.class);
                                    intent.putExtra("PARCOURS_SELECTIONNE", parcoursClique);
                                    startActivity(intent);
                                });
                                recyclerView.setAdapter(adapter);
                            } else {
                                Toast.makeText(ListeParcoursActivity.this, "Aucun parcours trouvé avec ces critères.", Toast.LENGTH_LONG).show();
                            }
                        });
                    });

                } else {
                    Toast.makeText(ListeParcoursActivity.this, "Erreur Serveur OSM", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                Toast.makeText(ListeParcoursActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}