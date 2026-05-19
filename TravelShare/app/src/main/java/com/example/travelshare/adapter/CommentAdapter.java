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
import com.example.travelshare.model.Comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public interface OnCommentClickListener {
        void onMoreClick(View view, Comment comment);
    }

    private List<Comment> commentList = new ArrayList<>();
    private final OnCommentClickListener listener;
    private final String currentUserId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);

    public CommentAdapter(String currentUserId, OnCommentClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setComments(List<Comment> comments) {
        this.commentList = comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment currentComment = commentList.get(position);

        holder.Author.setText(currentComment.getAuthorName());
        holder.Text.setText(currentComment.getText());

        if (currentComment.getDate() != null) {
            holder.Date.setText(dateFormat.format(currentComment.getDate()));
        }

        if (currentComment.getAuthorPictureUrl() != null && !currentComment.getAuthorPictureUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(currentComment.getAuthorPictureUrl())
                    .placeholder(R.drawable.default_user)
                    .circleCrop()
                    .into(holder.ProfilPicture);
        } else {
            holder.ProfilPicture.setImageResource(R.drawable.default_user);
        }

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMoreClick(v, currentComment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView Author, Text, Date;
        ImageView ProfilPicture;
        ImageButton btnMore;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            Author = itemView.findViewById(R.id.comment_pseudo);
            Text = itemView.findViewById(R.id.comment_description);
            Date = itemView.findViewById(R.id.comment_date);
            ProfilPicture = itemView.findViewById(R.id.comment_profil_picture);
            btnMore = itemView.findViewById(R.id.comment_btn_more);
        }
    }
}
