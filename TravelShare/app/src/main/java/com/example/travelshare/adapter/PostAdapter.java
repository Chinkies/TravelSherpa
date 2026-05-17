package com.example.travelshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.model.Post;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
        void onProfileClick(String userId);
        void onMoreClick(View view, Post post);
        void onLikeClick(Post post);
        void onCommentClick(Post post);
    }

    private List<Post> postList = new ArrayList<>();
    private final OnPostClickListener listener;
    private String currentUser;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRANCE);

    public PostAdapter(String currentUser, OnPostClickListener listener){
        this.currentUser = currentUser;
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post currentPost = postList.get(position);

        holder.Pseudo.setText(currentPost.getAuthorName());
        holder.Description.setText(currentPost.getDescription());

        holder.countLikes.setText(currentPost.getLikesCount() + " like" + (currentPost.getLikesCount() > 1 ? "s" : ""));
        holder.countComments.setText(currentPost.getCommentCount() + " commentaire" + (currentPost.getCommentCount() > 1 ? "s" : ""));

        if (currentPost.getDate() != null) {
            holder.Date.setText(dateFormat.format(currentPost.getDate()));
        }

        String postImg = currentPost.getImageUrl();

        if (currentUser != null && currentPost.getLikers() != null && currentPost.getLikers().contains(currentUser)) {
            holder.btnLikes.setColorFilter(android.graphics.Color.RED);
        } else {
            holder.btnLikes.clearColorFilter();;
        }

        Glide.with(holder.itemView.getContext())
                .load(postImg != null && !postImg.trim().isEmpty() ? postImg : null)
                .placeholder(R.drawable.img_app)
                .error(R.drawable.img_app)
                .centerCrop()
                .into(holder.PostImage);

        String profileImg = currentPost.getAuthorProfilPictureUrl();
        Glide.with(holder.itemView.getContext())
                .load(profileImg != null && !profileImg.trim().isEmpty() ? profileImg : null)
                .placeholder(R.drawable.default_user)
                .error(R.drawable.default_user)
                .circleCrop()
                .into(holder.ProfilPicture);

        holder.itemView.setOnClickListener(v -> listener.onPostClick(currentPost));

        View.OnClickListener profileClick = v -> listener.onProfileClick(currentPost.getAuthorId());
        holder.ProfilPicture.setOnClickListener(profileClick);
        holder.Pseudo.setOnClickListener(profileClick);

        holder.btnMore.setOnClickListener(v -> listener.onMoreClick(v, currentPost));

        holder.btnLikes.setOnClickListener(v -> listener.onLikeClick(currentPost));
        holder.btnComment.setOnClickListener(v -> listener.onCommentClick(currentPost));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updateUserId(String userId) {
        this.currentUser = userId;
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView Pseudo, Date, Description, countLikes, countComments;
        ImageView PostImage, ProfilPicture, btnMore, btnLikes, btnComment;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ProfilPicture = itemView.findViewById(R.id.post_profil_picture);
            Pseudo = itemView.findViewById(R.id.post_pseudo);
            Date = itemView.findViewById(R.id.post_date);
            PostImage = itemView.findViewById(R.id.post_picture);
            Description = itemView.findViewById(R.id.post_description);
            btnLikes = itemView.findViewById(R.id.post_btn_like);
            btnComment = itemView.findViewById(R.id.post_btn_comment);
            countLikes = itemView.findViewById(R.id.post_likes);
            countComments = itemView.findViewById(R.id.post_comment);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}