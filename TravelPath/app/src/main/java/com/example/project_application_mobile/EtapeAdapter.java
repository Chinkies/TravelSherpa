package com.example.project_application_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EtapeAdapter extends RecyclerView.Adapter<EtapeAdapter.EtapeViewHolder> {

    private List<Etape> listeEtapes;
    private Map<Integer, Integer> dureeParJour;

    public EtapeAdapter(List<Etape> listeEtapes) {
        this.listeEtapes = listeEtapes;
        this.dureeParJour = new HashMap<>();

        for (Etape e : listeEtapes) {
            int jour = e.getJour();
            int temps = e.getLieu().getDureeVisiteMinute() + e.getTempsTrajetMinutes();
            dureeParJour.put(jour, dureeParJour.getOrDefault(jour, 0) + temps);
        }
    }

    @NonNull
    @Override
    public EtapeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_etape, parent, false);
        return new EtapeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtapeViewHolder holder, int position) {
        Etape etape = listeEtapes.get(position);
        Lieu lieu = etape.getLieu();

        if (position == 0 || listeEtapes.get(position).getJour() != listeEtapes.get(position - 1).getJour()) {
            holder.textEnTeteJour.setVisibility(View.VISIBLE);

            int jourActuel = etape.getJour();
            int dureeJourMins = dureeParJour.getOrDefault(jourActuel, 0);

            String texteDureeJour = (dureeJourMins >= 60) ?
                    (dureeJourMins / 60) + "h" + (dureeJourMins % 60 == 0 ? "00" : String.format("%02d", dureeJourMins % 60))
                    : dureeJourMins + " min";

            holder.textEnTeteJour.setText("Jour " + jourActuel + " (Durée estimée : " + texteDureeJour + ")");
        } else {
            holder.textEnTeteJour.setVisibility(View.GONE);
        }

        holder.textNomLieu.setText(lieu.getName());
        if (lieu.getRealImageUrl() != null) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(lieu.getRealImageUrl())
                    .into(holder.imageLieu);
        } else {
            holder.imageLieu.setImageDrawable(null);
            holder.imageLieu.setBackgroundColor(android.graphics.Color.LTGRAY);

            String titreWiki = lieu.getWikipediaTitle();

            if (titreWiki == null) titreWiki = lieu.getName();
            titreWiki = titreWiki.replace(" ", "_");

            WikipediaClient.getApi().getSummary(titreWiki).enqueue(new retrofit2.Callback<WikipediaResponse>() {
                @Override
                public void onResponse(retrofit2.Call<WikipediaResponse> call, retrofit2.Response<WikipediaResponse> response) {
                    String finalUrl;

                    if (response.isSuccessful() && response.body() != null && response.body().thumbnail != null) {
                        finalUrl = response.body().thumbnail.source;
                        android.util.Log.d("WIKI_DEBUG", " Image trouvée pour : " + lieu.getName());
                    } else {
                        finalUrl = "https://picsum.photos/seed/" + lieu.getId() + "/400/300";
                        android.util.Log.e("WIKI_DEBUG", " Échec pour : " + lieu.getName() + " -> Erreur HTTP : " + response.code());
                    }

                    lieu.setRealImageUrl(finalUrl);

                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                            .load(finalUrl)
                            .into(holder.imageLieu);
                }

                @Override
                public void onFailure(retrofit2.Call<WikipediaResponse> call, Throwable t) {
                    android.util.Log.e("WIKI_DEBUG", " Pas de connexion internet pour : " + lieu.getName());
                    String finalUrl = "https://picsum.photos/seed/" + lieu.getId() + "/400/300";
                    lieu.setRealImageUrl(finalUrl);
                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                            .load(finalUrl)
                            .into(holder.imageLieu);
                }
            });
        }

        int duree = lieu.getDureeVisiteMinute();
        String texteDuree = (duree >= 60) ? (duree / 60) + "h" + (duree % 60 == 0 ? "00" : (duree % 60)) : duree + " min";

        String horaires = lieu.getHorairesOuverture() != null ? lieu.getHorairesOuverture() : "Non précisés";
        String infos = etape.getCreneau() + " - " + lieu.getPrixVisite() + "€ - Visite: " + texteDuree + "\n " + horaires;
        holder.textInfos.setText(infos);

        if (position == 0 || holder.textEnTeteJour.getVisibility() == View.VISIBLE) {
            holder.textTrajet.setText("Début de journée");
        } else {
            holder.textTrajet.setText("Trajet : " + etape.getTempsTrajetMinutes() + " min");
        }
    }

    public static class EtapeViewHolder extends RecyclerView.ViewHolder {
        TextView textEnTeteJour, textNomLieu, textInfos, textTrajet;
        ImageView imageLieu;

        public EtapeViewHolder(@NonNull View itemView) {
            super(itemView);
            textEnTeteJour = itemView.findViewById(R.id.textEnTeteJour);
            textNomLieu = itemView.findViewById(R.id.textNomLieu);
            textInfos = itemView.findViewById(R.id.textInfos);
            textTrajet = itemView.findViewById(R.id.textTrajet);
            imageLieu = itemView.findViewById(R.id.imageLieu);
        }
    }

    @Override
    public int getItemCount() {
        return listeEtapes.size();
    }
}