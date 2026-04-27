package com.example.project_application_mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class ActivityPreference extends AppCompatActivity {

    private Slider sliderBudget, sliderJours;
    private MaterialButtonToggleGroup toggleGroupDifficulte;
    private ChipGroup chipGroupActivites, chipGroupMeteo;
    private TextInputEditText editLieuObligatoire;
    private Button btnGenererParcours;
    private TextInputEditText editVille;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        sliderBudget = findViewById(R.id.sliderBudget);
        sliderJours = findViewById(R.id.sliderJours);
        toggleGroupDifficulte = findViewById(R.id.toggleGroupDifficulte);
        chipGroupActivites = findViewById(R.id.chipGroupActivites);
        chipGroupMeteo = findViewById(R.id.chipGroupMeteo);
        editLieuObligatoire = findViewById(R.id.editLieuObligatoire);
        btnGenererParcours = findViewById(R.id.btnGenererParcours);
        editVille = findViewById(R.id.editVille);

        btnGenererParcours.setOnClickListener(v -> sauvegarderPreference());
    }

    private void sauvegarderPreference() {
        float budget = sliderBudget.getValue();
        int jours = (int) sliderJours.getValue();

        String effort = "Medium";
        int checkedId = toggleGroupDifficulte.getCheckedButtonId();
        if (checkedId == R.id.btnFacile) effort = "Facile";
        else if (checkedId == R.id.btnDifficile) effort = "Difficile";
        else if (checkedId == R.id.btnSportif) effort = "Sportif";

        ArrayList<String> activitesChoisies = new ArrayList<>();
        for (int id : chipGroupActivites.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            activitesChoisies.add(chip.getText().toString());
        }

        ArrayList<String> meteoChoisies = new ArrayList<>();
        for (int id : chipGroupMeteo.getCheckedChipIds()) {
            Chip chip = findViewById(id);
            meteoChoisies.add(chip.getText().toString());
        }

        String ville = editVille.getText().toString().trim();
        String lieuObligatoire = editLieuObligatoire.getText().toString().trim();

        if (ville.isEmpty()) {
            ville = "Paris";
        }

        SharedPreferences sharedPreferences = getSharedPreferences("TravelPath", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("VILLE", ville);
        editor.putFloat("BUDGET", budget);
        editor.putInt("JOURS", jours);
        editor.putString("EFFORT", effort);
        editor.putString("LIEU_FAVORI", lieuObligatoire);
        editor.apply();

        Intent intent = new Intent(ActivityPreference.this, ListeParcoursActivity.class);
        intent.putStringArrayListExtra("ACTIVITES_CHOISIES", activitesChoisies);
        intent.putStringArrayListExtra("METEO_CHOISIES", meteoChoisies);
        startActivity(intent);
    }
}