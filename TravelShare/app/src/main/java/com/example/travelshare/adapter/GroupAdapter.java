package com.example.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.model.Group;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    private List<Group> groupList = new ArrayList<>();
    private OnGroupClickListener listener;

    public GroupAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void setGroups(List<Group> groups) {
        this.groupList = groups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        Group currentGroup = groupList.get(position);
        holder.bind(currentGroup, listener);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView GroupIcon;
        TextView GroupName, Date, Description;
        private SimpleDateFormat dateFormat;

        ImageButton btnGroup;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            GroupIcon = itemView.findViewById(R.id.group_profil_picture);
            GroupName = itemView.findViewById(R.id.group_name);
            Date = itemView.findViewById(R.id.group_date);
            Description = itemView.findViewById(R.id.group_description);
            dateFormat = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm", Locale.FRANCE);
        }

        public void bind(Group group, OnGroupClickListener listener) {
            GroupName.setText(group.getGroupName());
            Description.setText(group.getDescription());

            if (group.getLastActivityDate() != null){
                Date.setText("Dernière acti : " + dateFormat.format(group.getLastActivityDate()));
            } else {
                Date.setText("Nouveau groupe");
            }

            if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(group.getImageUrl()).placeholder(R.drawable.default_user).into(GroupIcon);
            } else {
                GroupIcon.setImageResource(R.drawable.default_user);
            }

            itemView.setOnClickListener(v -> listener.onGroupClick(group));
        }
    }
}