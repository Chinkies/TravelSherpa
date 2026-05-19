package com.example.travelshare.view.fragments.travelpath;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.travelpath.WikipediaClient;
import com.example.travelshare.travelpath.WikipediaResponse;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreferenceFragment extends Fragment {

    private Slider sliderBudget, sliderJours;
    private MaterialButtonToggleGroup toggleGroupDifficulte;
    private ChipGroup chipGroupActivites, chipGroupMeteo;
    private EditText editLieuObligatoire, editVille;
    private Button btnGenererParcours;
    private TextView textValeurBudget;
    private ImageView imgHeaderVille;
    private ProgressBar progressImageVille;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public PreferenceFragment() {
        super(R.layout.activity_preference);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sliderBudget = view.findViewById(R.id.sliderBudget);
        sliderJours = view.findViewById(R.id.sliderJours);
        toggleGroupDifficulte = view.findViewById(R.id.toggleGroupDifficulte);
        chipGroupActivites = view.findViewById(R.id.chipGroupActivites);
        chipGroupMeteo = view.findViewById(R.id.chipGroupMeteo);
        editLieuObligatoire = view.findViewById(R.id.editLieuObligatoire);
        btnGenererParcours = view.findViewById(R.id.btnGenererParcours);
        editVille = view.findViewById(R.id.editVille);
        textValeurBudget = view.findViewById(R.id.textValeurBudget);
        imgHeaderVille = view.findViewById(R.id.imgHeaderVille);
        progressImageVille = view.findViewById(R.id.progressImageVille);

        if (getArguments() != null) {
            if (getArguments().containsKey("SUGGESTED_LIEU")) {
                editLieuObligatoire.setText(getArguments().getString("SUGGESTED_LIEU"));
            }

            if (getArguments().containsKey("LATITUDE") && getArguments().containsKey("LONGITUDE")) {
                double lat = getArguments().getDouble("LATITUDE");
                double lng = getArguments().getDouble("LONGITUDE");
                String coords = String.format(Locale.US, "%.5f, %.5f", lat, lng);
                editVille.setText(coords);
                reverseGeocode(lat, lng);
            }

            if (getArguments().containsKey("POST_TAGS")) {
                ArrayList<String> postTags = getArguments().getStringArrayList("POST_TAGS");
                if (postTags != null) {
                    autoSelectChips(postTags);
                }
            }
        }

        sliderBudget.addOnChangeListener((slider, value, fromUser) -> {
            textValeurBudget.setText((int) value + "€");
        });

        editVille.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String ville = s.toString().trim();
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                
                searchRunnable = () -> {
                    if (ville.length() >= 3 && !ville.contains(",")) {
                        chargerImageVille(ville);
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });

        btnGenererParcours.setOnClickListener(v -> sauvegarderPreference(view));
    }

    private void reverseGeocode(double lat, double lng) {
        Context context = getContext();
        if (context == null) return;

        new Thread(() -> {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String city = addresses.get(0).getLocality();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded() && city != null) {
                            // On ne change pas le texte si c'est déjà des coordonnées
                            // mais on charge l'image de la ville trouvée
                            chargerImageVille(city);
                        }
                    });
                }
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    private void autoSelectChips(ArrayList<String> tags) {
        for (int i = 0; i < chipGroupActivites.getChildCount(); i++) {
            View child = chipGroupActivites.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String chipText = chip.getText().toString().toLowerCase();
                for (String tag : tags) {
                    if (tag.toLowerCase().contains(chipText) || chipText.contains(tag.toLowerCase())) {
                        chip.setChecked(true);
                        break;
                    }
                }
            }
        }
    }

    private void chargerImageVille(String ville) {
        if (getContext() == null) return;

        progressImageVille.setVisibility(View.VISIBLE);
        String villeWiki = ville.replace(" ", "_");

        WikipediaClient.getApi().getSummary(villeWiki).enqueue(new Callback<WikipediaResponse>() {
            @Override
            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                if (!isAdded() || getView() == null) return;
                progressImageVille.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().thumbnail != null) {
                    Glide.with(requireContext())
                            .load(response.body().thumbnail.source)
                            .centerCrop()
                            .into(imgHeaderVille);
                }
            }
            @Override
            public void onFailure(Call<WikipediaResponse> call, Throwable t) {
                if (isAdded() && progressImageVille != null) {
                    progressImageVille.setVisibility(View.GONE);
                }
            }
        });
    }

    private void sauvegarderPreference(View view) {
        Context context = getContext();
        if (context == null) return;

        btnGenererParcours.setEnabled(false);
        btnGenererParcours.setText("Lancement...");

        float budget = sliderBudget.getValue();
        int jours = (int) sliderJours.getValue();

        String effort = "Medium";
        int checkedId = toggleGroupDifficulte.getCheckedButtonId();
        if (checkedId == R.id.btnFacile) effort = "Facile";
        else if (checkedId == R.id.btnDifficile) effort = "Difficile";
        else if (checkedId == R.id.btnSportif) effort = "Sportif";

        ArrayList<String> activitesChoisies = new ArrayList<>();
        for (int id : chipGroupActivites.getCheckedChipIds()) {
            Chip chip = view.findViewById(id);
            if (chip != null) activitesChoisies.add(chip.getText().toString());
        }

        ArrayList<String> meteoChoisies = new ArrayList<>();
        for (int id : chipGroupMeteo.getCheckedChipIds()) {
            Chip chip = view.findViewById(id);
            if (chip != null) meteoChoisies.add(chip.getText().toString());
        }

        String ville = editVille.getText().toString().trim();
        if (ville.isEmpty()) ville = "Paris";

        SharedPreferences sharedPreferences = context.getSharedPreferences("TravelPath", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString("VILLE", ville)
                .putFloat("BUDGET", budget)
                .putInt("JOURS", jours)
                .putString("EFFORT", effort)
                .putString("LIEU_FAVORI", editLieuObligatoire.getText().toString().trim());
        
        if (getArguments() != null && getArguments().containsKey("LATITUDE")) {
            editor.putFloat("LAT_FAVORI", (float) getArguments().getDouble("LATITUDE"));
            editor.putFloat("LONG_FAVORI", (float) getArguments().getDouble("LONGITUDE"));
        }
        
        editor.apply();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("ACTIVITES_CHOISIES", activitesChoisies);
        bundle.putStringArrayList("METEO_CHOISIES", meteoChoisies);
        Navigation.findNavController(view).navigate(R.id.action_preferenceFragment_to_listeParcoursFragment, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (btnGenererParcours != null) {
            btnGenererParcours.setEnabled(true);
            btnGenererParcours.setText("Rechercher mon parcours");
        }
    }
}
