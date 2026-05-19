package com.example.travelshare.view.fragments.travelpath;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.model.Lieu;
import com.example.travelshare.travelpath.Etape;
import com.example.travelshare.travelpath.Parcours;
import com.example.travelshare.travelpath.WikipediaClient;
import com.example.travelshare.travelpath.WikipediaResponse;
import com.example.travelshare.travelpath.adapter.EtapeAdapter;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.ParcoursViewModel;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultatFragment extends Fragment {

    private MapView map;
    private Parcours parcours;
    private RecyclerView recyclerView;
    private EtapeAdapter etapeAdapter;
    private ParcoursViewModel parcoursViewModel;
    private AuthViewModel authViewModel;

    private final ActivityResultLauncher<Intent> createPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (getActivity() != null && result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        genererContenuPdf(uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle bundle) {
        View view = inflater.inflate(R.layout.activity_resultat, container, false);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        parcoursViewModel = new ViewModelProvider(this).get(ParcoursViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        map = view.findViewById(R.id.mapView);
        map.setMultiTouchControls(true);

        if (getArguments() != null) {
            parcours = (Parcours) getArguments().getSerializable("parcours");
        }

        if (parcours != null) {
            setupUI(view);
            displayParcoursOnMap();
        } else {
            Toast.makeText(getContext(), "Erreur : Impossible de charger le parcours", Toast.LENGTH_SHORT).show();
        }

        observeViewModel();

        return view;
    }

    private void setupUI(View view) {
        TextView tvTitre = view.findViewById(R.id.textTitreParcours);
        TextView tvBudget = view.findViewById(R.id.valBudget);
        TextView tvDuree = view.findViewById(R.id.valDuree);
        TextView tvEffort = view.findViewById(R.id.valEffort);
        TextView tvLieux = view.findViewById(R.id.valLieux);

        recyclerView = view.findViewById(R.id.recyclerViewEtapes);
        Button btnSauvegarder = view.findViewById(R.id.btnSauvegarder);
        ImageView btnPartager = view.findViewById(R.id.btnPartager);
        ImageView btnExportPdf = view.findViewById(R.id.btnExportPdf);

        tvTitre.setText(parcours.getNomOption() != null ? parcours.getNomOption() : "Sans titre");
        tvBudget.setText(String.format(Locale.FRANCE, "Budget: %.2f €", parcours.getBudgetTotal()));

        int nbJours = parcours.getNbJours();
        String strJours = nbJours > 1 ? " jours" : " jour";
        tvDuree.setText(String.format(Locale.FRANCE, "Durée: %d%s", nbJours, strJours));

        tvEffort.setText(String.format(Locale.FRANCE, "Effort: %s", parcours.getNiveauDifficulte()));

        int nbEtapes = (parcours.getListeEtapes() != null) ? parcours.getListeEtapes().size() : 0;
        tvLieux.setText(String.format(Locale.FRANCE, "Lieux: %d", nbEtapes));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        etapeAdapter = new EtapeAdapter(parcours.getListeEtapes() != null ? parcours.getListeEtapes() : new ArrayList<>(), etape -> {
            if (etape.getLieu() != null) {
                GeoPoint gp = new GeoPoint(etape.getLieu().getLatitude(), etape.getLieu().getLongitude());
                map.getController().animateTo(gp);
                map.getController().setZoom(17.0);
            }
        });
        recyclerView.setAdapter(etapeAdapter);

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser == null || parcours.getCreateur_id() != null) {
            btnSauvegarder.setVisibility(View.GONE);
        } else {
            btnSauvegarder.setVisibility(View.VISIBLE);
            btnSauvegarder.setOnClickListener(v -> {
                parcoursViewModel.saveParcours(currentUser.getUid(), parcours);
            });
        }

        btnExportPdf.setOnClickListener(v -> exporterEnPDF());
        btnPartager.setOnClickListener(v -> partagerParcours());

        chargerImageVille(view);
    }

    private void chargerImageVille(View view) {
        ImageView imgBanner = view.findViewById(R.id.imgBanner);
        if (imgBanner == null || getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("TravelPath", Context.MODE_PRIVATE);
        String ville = prefs.getString("VILLE", "Paris");

        String villeWiki = ville.replace(" ", "_");
        WikipediaClient.getApi().getSummary(villeWiki).enqueue(new Callback<WikipediaResponse>() {
            @Override
            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                if (!isAdded() || getView() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().thumbnail != null) {
                    Glide.with(requireContext())
                            .load(response.body().thumbnail.source)
                            .centerCrop()
                            .into(imgBanner);
                }
            }
            @Override
            public void onFailure(Call<WikipediaResponse> call, Throwable t) {
                // On laisse l'image par défaut en cas d'erreur
            }
        });
    }

    private void partagerParcours() {
        if (parcours == null) return;
        StringBuilder textePartage = new StringBuilder();
        textePartage.append(" Découvrez mon parcours TravelShare : ").append(parcours.getNomOption()).append("\n\n");
        textePartage.append(" Budget : ").append(String.format(Locale.FRANCE, "%.2f", parcours.getBudgetTotal())).append("€\n");

        int nbJours = parcours.getNbJours();
        textePartage.append(" Durée : ").append(nbJours).append(nbJours > 1 ? " jours\n\n" : " jour\n\n");

        textePartage.append(" Étapes :\n");

        if (parcours.getListeEtapes() != null) {
            for (Etape e : parcours.getListeEtapes()) {
                if (e.getLieu() != null) {
                    textePartage.append("- ").append(e.getCreneau()).append(" : ").append(e.getLieu().getName()).append("\n");
                }
            }
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textePartage.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Partager mon parcours via...");
        startActivity(shareIntent);
    }

    private void exporterEnPDF() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        String titre = parcours.getNomOption() != null ? parcours.getNomOption() : "Parcours";
        String nomFichier = "Parcours_" + titre.replace(" ", "_") + ".pdf";
        intent.putExtra(Intent.EXTRA_TITLE, nomFichier);

        createPdfLauncher.launch(intent);
    }

    private void genererContenuPdf(Uri uri) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int yPosition = 50;

        paint.setColor(Color.BLACK);
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("Votre Parcours : " + (parcours.getNomOption() != null ? parcours.getNomOption() : ""), 40, yPosition, paint);
        yPosition += 40;

        paint.setTextSize(16f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Budget total : " + String.format(Locale.FRANCE, "%.2f", parcours.getBudgetTotal()) + "€", 40, yPosition, paint);
        yPosition += 25;

        int nbJours = parcours.getNbJours();
        canvas.drawText("Durée totale : " + nbJours + (nbJours > 1 ? " jours" : " jour"), 40, yPosition, paint);

        yPosition += 25;
        canvas.drawText("Effort global : " + parcours.getNiveauDifficulte(), 40, yPosition, paint);
        yPosition += 50;

        paint.setColor(Color.BLACK);
        if (parcours.getListeEtapes() != null) {
            for (Etape etape : parcours.getListeEtapes()) {
                if (yPosition > 780) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPosition = 50;
                }

                paint.setTextSize(16f);
                paint.setFakeBoldText(true);
                String nomLieu = etape.getLieu() != null ? etape.getLieu().getName() : "Inconnu";
                String titreEtape = "Jour " + etape.getJour() + " - " + etape.getCreneau() + " : " + nomLieu;
                canvas.drawText(titreEtape, 40, yPosition, paint);
                yPosition += 20;

                paint.setTextSize(14f);
                paint.setFakeBoldText(false);
                paint.setColor(Color.GRAY);
                String cat = (etape.getLieu() != null) ? etape.getLieu().getCategorie() : "";
                double prix = (etape.getLieu() != null) ? etape.getLieu().getPrixVisite() : 0.0;
                String detailsEtape = "   Catégorie : " + cat + " | Prix : " + prix + "€";
                canvas.drawText(detailsEtape, 40, yPosition, paint);

                paint.setColor(Color.BLACK);
                yPosition += 35;
            }
        }

        document.finishPage(page);

        try {
            if (getContext() != null) {
                OutputStream outputStream = getContext().getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    document.writeTo(outputStream);
                    document.close();
                    outputStream.close();
                    Toast.makeText(getContext(), "PDF téléchargé avec succès ! ", Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erreur lors de la création du PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void observeViewModel() {
        parcoursViewModel.getParcoursSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved) {
                Toast.makeText(getContext(), "Parcours sauvegardé avec succès !", Toast.LENGTH_SHORT).show();
                parcoursViewModel.resetParcoursSaved();
                View b = getView();
                if (b != null) {
                    Button btn = b.findViewById(R.id.btnSauvegarder);
                    if (btn != null) btn.setVisibility(View.GONE);
                }
            }
        });

        parcoursViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Erreur : " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayParcoursOnMap() {
        if (parcours == null || parcours.getListeEtapes() == null || parcours.getListeEtapes().isEmpty()) return;

        GeoPoint firstPoint = null;
        for (Etape etape : parcours.getListeEtapes()) {
            Lieu lieu = etape.getLieu();
            if (lieu != null) {
                GeoPoint gp = new GeoPoint(lieu.getLatitude(), lieu.getLongitude());
                if (firstPoint == null) firstPoint = gp;

                Marker marker = new Marker(map);
                marker.setPosition(gp);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(lieu.getName() + " (" + etape.getCreneau() + ")");
                map.getOverlays().add(marker);
            }
        }

        if (firstPoint != null) {
            map.getController().setZoom(14.0);
            map.getController().setCenter(firstPoint);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }
}