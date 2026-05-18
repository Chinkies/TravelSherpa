package com.example.travelshare.travelpath.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.model.Lieu;
import com.example.travelshare.travelpath.Etape;
import com.example.travelshare.travelpath.Parcours;
import com.example.travelshare.travelpath.WikipediaClient;
import com.example.travelshare.travelpath.WikipediaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParcoursAdapter extends RecyclerView.Adapter<ParcoursAdapter.ParcoursViewHolder> {

    private List<Parcours> listeParcours;
    private OnParcoursClickListener listener;

    public interface OnParcoursClickListener {
        void onParcoursClick(Parcours parcours);
    }

    public ParcoursAdapter(List<Parcours> listeParcours, OnParcoursClickListener listener) {
        this.listeParcours = listeParcours;
        this.listener = listener;
    }

    public void setParcoursList(List<Parcours> newList) {
        this.listeParcours = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParcoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parcours, parent, false);
        return new ParcoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcoursViewHolder holder, int position) {
        if (listeParcours == null || position >= listeParcours.size()) return;
        Parcours parcours = listeParcours.get(position);
        Context context = holder.itemView.getContext();

        holder.itemView.setTag(position);
        holder.textNomParcours.setText(parcours.getNomOption());

        // Formatage de la durée
        int totalMin = parcours.getDureeTotale();
        int j = totalMin / 1440;
        int h = (totalMin % 1440) / 60;
        int m = totalMin % 60;
        String dureeStr = (j > 0 ? j + "j " : "") + (h > 0 ? h + "h " : "") + m + "min";

        String stats = "Budget: " + (int)parcours.getBudgetTotal() + "€ | " + dureeStr + " | " + parcours.getNiveauDifficulte();
        holder.textStats.setText(stats);

        holder.layoutChipsLieux.removeAllViews();
        if (parcours.getListeEtapes() != null) {
            for (int i = 0; i < Math.min(3, parcours.getListeEtapes().size()); i++) {
                TextView chip = new TextView(context);
                chip.setText(parcours.getListeEtapes().get(i).getLieu().getName());
                chip.setTextSize(10f);
                chip.setTextColor(Color.WHITE);
                chip.setBackgroundResource(R.drawable.bg_rounded_white);
                if (chip.getBackground() != null) {
                    chip.getBackground().setTint(Color.parseColor("#2C3E50"));
                }
                chip.setPadding(12, 4, 12, 4);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
                lp.setMargins(0, 0, 8, 0);
                chip.setLayoutParams(lp);
                holder.layoutChipsLieux.addView(chip);
            }
        }

        // --- CHARGEMENT D'IMAGE INTELLIGENT ---
        holder.imgParcours.setImageResource(R.drawable.ballon_voyage_recherrche_default);
        if (parcours.getListeEtapes() != null && !parcours.getListeEtapes().isEmpty()) {
            tenterChargerImage(holder, parcours.getListeEtapes(), 0, position);
        }

        holder.btnVoirDetails.setOnClickListener(v -> {
            if (listener != null) listener.onParcoursClick(parcours);
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onParcoursClick(parcours);
        });
    }

    private void tenterChargerImage(ParcoursViewHolder holder, List<Etape> etapes, int index, int pos) {
        if (index >= etapes.size() || index >= 5) return;
        
        Lieu lieu = etapes.get(index).getLieu();
        if (lieu.getRealImageUrl() != null) {
            Glide.with(holder.itemView.getContext()).load(lieu.getRealImageUrl()).centerCrop().into(holder.imgParcours);
            return;
        }

        String titre = (lieu.getWikipediaTitle() != null) ? lieu.getWikipediaTitle() : lieu.getName();
        WikipediaClient.getApi().getSummary(titre.replace(" ", "_")).enqueue(new Callback<WikipediaResponse>() {
            @Override
            public void onResponse(Call<WikipediaResponse> call, Response<WikipediaResponse> response) {
                if (holder.itemView.getTag() == null || !String.valueOf(pos).equals(holder.itemView.getTag().toString())) return;
                
                if (response.isSuccessful() && response.body() != null && response.body().thumbnail != null) {
                    String url = response.body().thumbnail.source;
                    lieu.setRealImageUrl(url);
                    Glide.with(holder.itemView.getContext()).load(url).centerCrop().into(holder.imgParcours);
                } else {
                    tenterChargerImage(holder, etapes, index + 1, pos);
                }
            }
            @Override
            public void onFailure(Call<WikipediaResponse> call, Throwable t) {
                tenterChargerImage(holder, etapes, index + 1, pos);
            }
        });
    }

    @Override
    public int getItemCount() { return listeParcours != null ? listeParcours.size() : 0; }

    public static class ParcoursViewHolder extends RecyclerView.ViewHolder {
        TextView textNomParcours, textStats;
        ImageView imgParcours;
        LinearLayout layoutChipsLieux;
        Button btnVoirDetails;
        public ParcoursViewHolder(@NonNull View itemView) {
            super(itemView);
            imgParcours = itemView.findViewById(R.id.imgParcours);
            textNomParcours = itemView.findViewById(R.id.textNomParcours);
            textStats = itemView.findViewById(R.id.textStats);
            layoutChipsLieux = itemView.findViewById(R.id.layoutChipsLieux);
            btnVoirDetails = itemView.findViewById(R.id.btnVoirDetails);
        }
    }
}
