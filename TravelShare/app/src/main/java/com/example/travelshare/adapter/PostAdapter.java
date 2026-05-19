package com.example.travelshare.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.model.Post;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    private final SimpleDateFormat dateFormat;

    public PostAdapter(String currentUser, OnPostClickListener listener){
        this.currentUser = currentUser;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRANCE);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    }

    public void setPosts(List<Post> posts) {
        this.postList = posts;
        notifyDataSetChanged();
    }

    public List<Post> getPosts() {
        return postList;
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

        updateLikesUI(holder, currentPost);
        holder.countComments.setText(currentPost.getCommentCount() + " commentaire" + (currentPost.getCommentCount() > 1 ? "s" : ""));

        if (currentPost.getDate() != null) {
            holder.Date.setText(dateFormat.format(currentPost.getDate()));
        }

        String postImg = currentPost.getImageUrl();

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

        holder.btnLikes.setOnClickListener(v -> {
            if (currentUser != null) {
                boolean isLiked = currentPost.getLikers() != null && currentPost.getLikers().contains(currentUser);
                
                if (!isLiked) {
                    holder.btnLikes.setColorFilter(Color.RED);
                    animateHeart(holder.btnLikes);
                } else {
                    holder.btnLikes.clearColorFilter();
                }
                
                // Mise à jour immédiate du texte pour la fluidité
                int newCount = currentPost.getLikesCount() + (isLiked ? -1 : 1);
                holder.countLikes.setText(newCount + " like" + (newCount > 1 ? "s" : ""));

                listener.onLikeClick(currentPost);
            }
        });
        
        holder.btnComment.setOnClickListener(v -> listener.onCommentClick(currentPost));
    }

    private void updateLikesUI(PostViewHolder holder, Post post) {
        int count = post.getLikesCount();
        holder.countLikes.setText(count + " like" + (count > 1 ? "s" : ""));
        
        if (currentUser != null && post.getLikers() != null && post.getLikers().contains(currentUser)) {
            holder.btnLikes.setColorFilter(Color.RED);
        } else {
            holder.btnLikes.clearColorFilter();
        }
    }

    private void animateHeart(View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100).start())
                .start();
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