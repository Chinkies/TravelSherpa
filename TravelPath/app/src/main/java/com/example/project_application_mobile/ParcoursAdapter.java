package com.example.project_application_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

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

    @NonNull
    @Override
    public ParcoursViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parcours, parent, false);
        return new ParcoursViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParcoursViewHolder holder, int position) {
        Parcours parcours = listeParcours.get(position);

        holder.textNomParcours.setText("Option : " + parcours.getNomOption());

        int nombreEtapes = parcours.getListeEtapes().size();

        holder.textResumeParcours.setText("Budget: " + parcours.getBudgetTotal() + "€" + " | " + nombreEtapes + " étapes");

        holder.textEffortParcours.setText("Effort global: " + parcours.getNiveauDifficulte());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onParcoursClick(parcours);
        });
    }

    @Override
    public int getItemCount() {
        return listeParcours.size();
    }

    public static class ParcoursViewHolder extends RecyclerView.ViewHolder {
        TextView textNomParcours, textResumeParcours, textEffortParcours;
        public ParcoursViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomParcours = itemView.findViewById(R.id.textNomParcours);
            textResumeParcours = itemView.findViewById(R.id.textResumeParcours);
            textEffortParcours = itemView.findViewById(R.id.textEffortParcours);
        }
    }
}