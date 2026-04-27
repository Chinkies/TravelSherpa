package com.example.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.model.Group;

import java.util.ArrayList;
import java.util.List;

public class SelectedGroupAdapter extends RecyclerView.Adapter<SelectedGroupAdapter.ViewHolder> {
    private List<Group> selectedGroups = new ArrayList<>();

    public void addGroup(Group group) {
        boolean exists = false;
        for(Group g : selectedGroups) {
            if(g.getGroupId().equals(group.getGroupId())) exists = true;
        }

        if (!exists) {
            selectedGroups.add(group);
            notifyDataSetChanged();
        }
    }

    public List<Group> getSelectedGroups() {
        return selectedGroups;
    }

    public void removeGroup(Group group) {
        selectedGroups.remove(group);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_group_publish, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group g = selectedGroups.get(position);
        holder.name.setText(g.getGroupName());
        holder.icon.setImageResource(R.drawable.img_app);//PLACE HOLDER
        holder.btnDelete.setOnClickListener(v -> removeGroup(g));
    }

    @Override
    public int getItemCount() { return selectedGroups.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        ImageButton btnDelete;
        ViewHolder(View iv) {
            super(iv);
            icon = iv.findViewById(R.id.add_group_profil_picture);
            name = iv.findViewById(R.id.add_group_name);
            btnDelete = iv.findViewById(R.id.add_group_delete);
        }
    }
}