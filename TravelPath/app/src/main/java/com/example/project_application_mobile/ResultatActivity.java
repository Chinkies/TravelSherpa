package com.example.project_application_mobile;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import com.google.gson.Gson;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.IOException;
import java.io.OutputStream;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ResultatActivity extends AppCompatActivity {

    private TextView textTitreParcours, textMetriques;
    private RecyclerView recyclerViewEtapes;
    private MapView map;
    private Button btnSauvegarder, btnPartager, btnExportPdf;
    private Parcours parcoursActuel;

    private final ActivityResultLauncher<Intent> createPdfLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        genererContenuPdf(uri);
                    }
                }
            }
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.content.SharedPreferences osmdroidPrefs = getSharedPreferences("osmdroid", MODE_PRIVATE);
        Configuration.getInstance().load(getApplicationContext(), osmdroidPrefs);
        setContentView(R.layout.activity_resultat);

        textTitreParcours = findViewById(R.id.textTitreParcours);
        textMetriques = findViewById(R.id.textMetriques);
        recyclerViewEtapes = findViewById(R.id.recyclerViewEtapes);
        map = findViewById(R.id.mapView);
        btnSauvegarder = findViewById(R.id.btnSauvegarder);
        btnPartager = findViewById(R.id.btnPartager);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        recyclerViewEtapes.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        parcoursActuel = (Parcours) getIntent().getSerializableExtra("PARCOURS_SELECTIONNE");

        if (parcoursActuel != null) {
            afficherParcours(parcoursActuel);
            afficherCarte(parcoursActuel);

            btnPartager.setOnClickListener(v -> partagerParcours());

            btnSauvegarder.setOnClickListener(v -> sauvegarderParcoursFirebase());

            btnExportPdf.setOnClickListener(v -> exporterEnPDF());

        } else {
            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
        }
    }

    private void partagerParcours() {
        StringBuilder textePartage = new StringBuilder();
        textePartage.append(" Découvrez mon parcours TravelPath : ").append(parcoursActuel.getNomOption()).append("\n\n");
        textePartage.append(" Budget : ").append(parcoursActuel.getBudgetTotal()).append("€\n");
        textePartage.append("️ Durée : ").append(parcoursActuel.getDureeTotale()).append(" min\n\n");
        textePartage.append(" Étapes :\n");

        for (Etape e : parcoursActuel.getListeEtapes()) {
            textePartage.append("- ").append(e.getCreneau()).append(" : ").append(e.getLieu().getName()).append("\n");
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, textePartage.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Partager mon parcours via...");
        startActivity(shareIntent);
    }

    private void sauvegarderParcoursFirebase() {
        Toast.makeText(this, "Enregistrement dans le Cloud en cours...", Toast.LENGTH_SHORT).show();
        btnSauvegarder.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String idCreateur = "user_test_123";

        Map<String, Object> parcoursData = new HashMap<>();
        parcoursData.put("nomOption", parcoursActuel.getNomOption());
        parcoursData.put("budgetTotal", parcoursActuel.getBudgetTotal());
        parcoursData.put("dureeTotale", parcoursActuel.getDureeTotale());
        parcoursData.put("niveauDifficulte", parcoursActuel.getNiveauDifficulte());
        parcoursData.put("createur_id", idCreateur);

        parcoursData.put("etapes", parcoursActuel.getListeEtapes());

        db.collection("parcours")
                .add(parcoursData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ResultatActivity.this, "Parcours sauvegardé sur le Cloud ! ️", Toast.LENGTH_LONG).show();
                    btnSauvegarder.setText("Sauvegardé ");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResultatActivity.this, "Erreur de sauvegarde : " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnSauvegarder.setEnabled(true);
                });
    }
    private void exporterEnPDF() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Nom par défaut du fichier
        String nomFichier = "Parcours_" + parcoursActuel.getNomOption().replace(" ", "_") + ".pdf";
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
        canvas.drawText("Votre Parcours : " + parcoursActuel.getNomOption(), 40, yPosition, paint);
        yPosition += 40;

        paint.setTextSize(16f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Budget total : " + parcoursActuel.getBudgetTotal() + "€", 40, yPosition, paint);
        yPosition += 25;
        canvas.drawText("Durée totale : " + parcoursActuel.getDureeTotale() + " min", 40, yPosition, paint);
        yPosition += 25;
        canvas.drawText("Effort global : " + parcoursActuel.getNiveauDifficulte(), 40, yPosition, paint);
        yPosition += 50;

        paint.setColor(Color.BLACK);
        for (Etape etape : parcoursActuel.getListeEtapes()) {

            if (yPosition > 780) {
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = 50;
            }

            paint.setTextSize(16f);
            paint.setFakeBoldText(true);
            String titreEtape = "Jour " + etape.getJour() + " - " + etape.getCreneau() + " : " + etape.getLieu().getName();
            canvas.drawText(titreEtape, 40, yPosition, paint);
            yPosition += 20;

            paint.setTextSize(14f);
            paint.setFakeBoldText(false);
            paint.setColor(Color.GRAY);
            String detailsEtape = "   Catégorie : " + etape.getLieu().getCategorie() + " | Prix : " + etape.getLieu().getPrixVisite() + "€";
            canvas.drawText(detailsEtape, 40, yPosition, paint);

            paint.setColor(Color.BLACK);
            yPosition += 35;
        }

        document.finishPage(page);

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            document.writeTo(outputStream);
            document.close();
            outputStream.close();
            Toast.makeText(this, "PDF téléchargé avec succès ! ", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la création du PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void afficherParcours(Parcours parcours) {
        textTitreParcours.setText(parcours.getNomOption());
        String metriques = "Budget : " + parcours.getBudgetTotal() + "€ | Effort : " + parcours.getNiveauDifficulte();
        textMetriques.setText(metriques);
        EtapeAdapter adapter = new EtapeAdapter(parcours.getListeEtapes());
        recyclerViewEtapes.setAdapter(adapter);
    }

    private void afficherCarte(Parcours parcours) {
        map.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        if (parcours.getListeEtapes().isEmpty()) return;

        Polyline ligneTrajet = new Polyline();
        ligneTrajet.setColor(android.graphics.Color.RED);
        ligneTrajet.setWidth(5f);

        for (int i = 0; i < parcours.getListeEtapes().size(); i++) {
            Lieu lieu = parcours.getListeEtapes().get(i).getLieu();
            GeoPoint point = new GeoPoint(lieu.getLatitude(), lieu.getLongitude());

            ligneTrajet.addPoint(point);

            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setTitle(lieu.getName());
            marker.setSnippet("Jour " + parcours.getListeEtapes().get(i).getJour());
            map.getOverlays().add(marker);

            if (i == 0) {
                map.getController().setZoom(13.0);
                map.getController().setCenter(point);
            }
        }

        map.getOverlays().add(ligneTrajet);
    }

    @Override
    protected void onResume() { super.onResume(); map.onResume(); }
    @Override
    protected void onPause() { super.onPause(); map.onPause(); }
}