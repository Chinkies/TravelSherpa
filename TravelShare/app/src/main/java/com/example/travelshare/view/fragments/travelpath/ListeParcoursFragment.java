package com.example.travelshare.view.fragments.travelpath;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.model.Lieu;
import com.example.travelshare.travelpath.OpenStreetMapClient;
import com.example.travelshare.travelpath.OverpassResponse;
import com.example.travelshare.travelpath.Parcours;
import com.example.travelshare.travelpath.ParcoursGenerator;
import com.example.travelshare.travelpath.PreferenceUtilisateur;
import com.example.travelshare.travelpath.WeatherClient;
import com.example.travelshare.travelpath.WeatherResponse;
import com.example.travelshare.travelpath.adapter.ParcoursAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListeParcoursFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView textTitrePage, badgeDestination;
    private View layoutLoading;

    public ListeParcoursFragment() {
        super(R.layout.activity_liste_parcours);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewParcours);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        textTitrePage = view.findViewById(R.id.textTitrePage);
        badgeDestination = view.findViewById(R.id.badgeDestination);
        layoutLoading = view.findViewById(R.id.layout_loading);

        chargerDonneesEtLancerRecherche(view);
    }

    private void chargerDonneesEtLancerRecherche(View view) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Context context = getContext();
            if (context == null) return;

            SharedPreferences sharedPreferences = context.getSharedPreferences("TravelPath", Context.MODE_PRIVATE);
            double budgetMax = sharedPreferences.getFloat("BUDGET", 150f);
            int nbJours = sharedPreferences.getInt("JOURS", 3);
            String niveauEffort = sharedPreferences.getString("EFFORT", "Medium");
            String lieuFavori = sharedPreferences.getString("LIEU_FAVORI", "");
            String villeDestination = sharedPreferences.getString("VILLE", "Paris");

            List<String> activitesChoisies = getArguments() != null ? getArguments().getStringArrayList("ACTIVITES_CHOISIES") : new ArrayList<>();
            List<String> meteoChoisies = getArguments() != null ? getArguments().getStringArrayList("METEO_CHOISIES") : new ArrayList<>();

            PreferenceUtilisateur prefs = new PreferenceUtilisateur(
                    activitesChoisies != null ? activitesChoisies : new ArrayList<>(),
                    budgetMax, nbJours, niveauEffort,
                    meteoChoisies != null ? meteoChoisies : new ArrayList<>(),
                    lieuFavori, villeDestination
            );

            double latTemp = 48.8529;
            double lonTemp = 2.3499;
            boolean coordsParsed = false;

            // Tentative de lecture directe si format "lat, lon"
            if (prefs.getVille().contains(",")) {
                try {
                    String[] parts = prefs.getVille().split(",");
                    if (parts.length == 2) {
                        latTemp = Double.parseDouble(parts[0].trim());
                        lonTemp = Double.parseDouble(parts[1].trim());
                        coordsParsed = true;
                    }
                } catch (Exception ignored) {}
            }

            if (!coordsParsed) {
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocationName(prefs.getVille(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        latTemp = addresses.get(0).getLatitude();
                        lonTemp = addresses.get(0).getLongitude();
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            final double finalLat = latTemp;
            final double finalLon = lonTemp;

            handler.post(() -> {
                if (isAdded() && textTitrePage != null) {
                    textTitrePage.setText("Parcours pour " + villeDestination);
                    badgeDestination.setText("Destination : " + villeDestination);
                    lancerMeteoEtOSM(finalLat, finalLon, prefs, view);
                }
            });
        });
    }

    private void lancerMeteoEtOSM(double lat, double lon, PreferenceUtilisateur prefs, View view) {
        WeatherClient.getApi().getCurrentWeather(lat, lon).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && response.body().current_weather != null) {
                    double temp = response.body().current_weather.temperature;
                    int code = response.body().current_weather.weathercode;
                    if (code >= 51) prefs.ajouterSensibiliteMeteo("Humidité");
                    else if (temp > 30.0) prefs.ajouterSensibiliteMeteo("Chaleur");
                    else if (temp < 5.0) prefs.ajouterSensibiliteMeteo("Froid");
                }
                if (isAdded()) lancerRechercheOSM(lat, lon, prefs, view);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if (isAdded()) lancerRechercheOSM(lat, lon, prefs, view);
            }
        });
    }

    private void lancerRechercheOSM(double lat, double lon, PreferenceUtilisateur prefs, View view) {
        int rayon = prefs.getNbJours() > 3 ? 10000 : 5000;
        
        String queryOSM = "[out:json][timeout:30];" +
                "(" +
                "nwr(around:" + rayon + "," + lat + "," + lon + ")[\"tourism\"=\"museum\"];" +
                "nwr(around:" + rayon + "," + lat + "," + lon + ")[\"historic\"=\"monument\"];" +
                "nwr(around:" + rayon + "," + lat + "," + lon + ")[\"historic\"=\"castle\"];" +
                "nwr(around:" + rayon + "," + lat + "," + lon + ")[\"leisure\"=\"park\"];" +
                "nwr(around:" + rayon + "," + lat + "," + lon + ")[\"leisure\"=\"garden\"];" +
                "nwr(around:" + rayon + "," + lat + "," + lon + ")[\"tourism\"=\"attraction\"];" +
                "nwr(around:" + rayon + "," + lat + "," + lon + ")[\"tourism\"=\"theme_park\"];" +
                "node(around:" + rayon + "," + lat + "," + lon + ")[\"amenity\"=\"restaurant\"];" +
                "node(around:" + rayon + "," + lat + "," + lon + ")[\"amenity\"=\"cafe\"];" +
                "node(around:" + rayon + "," + lat + "," + lon + ")[\"amenity\"=\"bar\"];" +
                ");" +
                "out center;";

        OpenStreetMapClient.getApi().getLieux(queryOSM).enqueue(new Callback<OverpassResponse>() {
            @Override
            public void onResponse(Call<OverpassResponse> call, Response<OverpassResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        List<Lieu> vraisLieux = OpenStreetMapClient.convertirEnLieux(response.body());
                        ParcoursGenerator moteur = new ParcoursGenerator();
                        List<Lieu> lieuxFiltres = moteur.filtrerLieu(prefs, vraisLieux, false);
                        List<Parcours> options = moteur.optionGenerator(prefs, lieuxFiltres);
                        
                        new Handler(Looper.getMainLooper()).post(() -> {
                            if (!isAdded() || getContext() == null) return;

                            // Masquer le loader
                            if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
                            // Afficher la liste
                            if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);

                            if (options != null && !options.isEmpty()) {
                                recyclerView.setAdapter(new ParcoursAdapter(options, p -> {
                                    if (isAdded()) {
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("parcours", p);
                                        Navigation.findNavController(view).navigate(R.id.action_listeParcoursFragment_to_resultatFragment, bundle);
                                    }
                                }));
                            } else {
                                Toast.makeText(getContext(), "Aucun parcours trouvé.", Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded() && layoutLoading != null) layoutLoading.setVisibility(View.GONE);
                    });
                }
            }
            @Override
            public void onFailure(Call<OverpassResponse> call, Throwable t) {
                if (isAdded()) {
                    if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
