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
import com.example.travelshare.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onDelete(User user);
        void onPromote(User user);
    }

    private List<User> userList = new ArrayList<>();
    private int layoutId;
    private OnUserActionListener listener;
    private boolean isSearchMode;
    private String currentUserId;

    public UserAdapter(int layoutId, OnUserActionListener listener) {
        this.layoutId = layoutId;
        this.listener = listener;
        this.isSearchMode = false;
    }

    public void setSearchMode(boolean searchMode) {
        this.isSearchMode = searchMode;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.name.setText(user.getPseudo());

        String url = user.getProfilePictureUrl();
        Glide.with(holder.itemView.getContext())
                .load(url != null && !url.trim().isEmpty() ? url : null)
                .placeholder(R.drawable.default_user)
                .error(R.drawable.default_user)
                .circleCrop()
                .into(holder.photo);

        if (isSearchMode) {
            if (holder.btnDelete != null) holder.btnDelete.setVisibility(View.GONE);
            if (holder.btnAddAdmin != null) holder.btnAddAdmin.setVisibility(View.VISIBLE);
        } else {
            // Hide delete button if the user is the current user
            if (holder.btnDelete != null) {
                if (currentUserId != null && user.getId().equals(currentUserId)) {
                    holder.btnDelete.setVisibility(View.GONE);
                } else {
                    holder.btnDelete.setVisibility(View.VISIBLE);
                }
            }

            if (holder.btnAddAdmin != null) {
                if (layoutId == R.layout.item_member) {
                    holder.btnAddAdmin.setVisibility(View.VISIBLE);
                } else {
                    holder.btnAddAdmin.setVisibility(View.GONE);
                }
            }
        }

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> { listener.onDelete(user); });
        }
        if (holder.btnAddAdmin != null) {
            holder.btnAddAdmin.setOnClickListener(v -> { listener.onPromote(user); });
        }
    }

    @Override
    public int getItemCount() { return userList.isEmpty() ? 0 : userList.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView photo;
        ImageButton btnDelete, btnAddAdmin;

        UserViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.item_user_name);
            photo = v.findViewById(R.id.item_user_photo);
            btnDelete = v.findViewById(R.id.item_user_delete);
            btnAddAdmin = v.findViewById(R.id.item_user_add_admin);
        }
    }
}
