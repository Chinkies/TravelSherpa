package com.example.travelshare.travelpath.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.model.Lieu;
import com.example.travelshare.travelpath.Etape;
import com.example.travelshare.travelpath.WikipediaClient;
import com.example.travelshare.travelpath.WikipediaResponse;

import java.util.List;

public class EtapeAdapter extends RecyclerView.Adapter<EtapeAdapter.EtapeViewHolder> {

    private List<Etape> listeEtapes;
    private OnEtapeClickListener listener;

    public interface OnEtapeClickListener {
        void onEtapeClick(Etape etape);
    }

    public EtapeAdapter(List<Etape> listeEtapes, OnEtapeClickListener listener) {
        this.listeEtapes = listeEtapes;
        this.listener = listener;
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

        if (position == 0 || etape.getJour() != listeEtapes.get(position - 1).getJour()) {
            holder.textEnTeteJour.setVisibility(View.VISIBLE);
            holder.textEnTeteJour.setText("JOUR " + etape.getJour());
        } else {
            holder.textEnTeteJour.setVisibility(View.GONE);
        }

        holder.textNomLieu.setText(etape.getCreneau() + " - " + lieu.getName());

        int duree = lieu.getDureeVisiteMinute();
        String texteDuree = (duree >= 60) ? (duree / 60) + "h" + (duree % 60 == 0 ? "00" : String.format("%02d", duree % 60)) : duree + " min";
        holder.textInfos.setText(lieu.getCategorie() + " • Visite : " + texteDuree);

        if (lieu.getPrixVisite() == 0.0) {
            holder.textPrix.setText("Coût : Gratuit");
        } else {
            holder.textPrix.setText("Coût : ~" + lieu.getPrixVisite() + "€");
        }

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
                    } else {
                        finalUrl = "https://picsum.photos/seed/" + lieu.getId() + "/400/300";
                    }

                    lieu.setRealImageUrl(finalUrl);
                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                            .load(finalUrl)
                            .into(holder.imageLieu);
                }

                @Override
                public void onFailure(retrofit2.Call<WikipediaResponse> call, Throwable t) {
                    String finalUrl = "https://picsum.photos/seed/" + lieu.getId() + "/400/300";
                    lieu.setRealImageUrl(finalUrl);
                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                            .load(finalUrl)
                            .into(holder.imageLieu);
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEtapeClick(etape);
        });
    }

    public static class EtapeViewHolder extends RecyclerView.ViewHolder {
        TextView textEnTeteJour, textNomLieu, textInfos, textPrix;
        ImageView imageLieu;

        public EtapeViewHolder(@NonNull View itemView) {
            super(itemView);
            textEnTeteJour = itemView.findViewById(R.id.textEnTeteJour);
            textNomLieu = itemView.findViewById(R.id.textNomLieu);
            textInfos = itemView.findViewById(R.id.textInfos);
            textPrix = itemView.findViewById(R.id.textPrix);
            imageLieu = itemView.findViewById(R.id.imageLieu);
        }
    }

    @Override
    public int getItemCount() {
        return listeEtapes.size();
    }
}
