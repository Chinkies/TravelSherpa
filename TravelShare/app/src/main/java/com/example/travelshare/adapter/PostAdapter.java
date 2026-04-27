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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
        void onProfileClick(String userId);
        void onMoreClick(View view, Post post);
    }

    private List<Post> postList = new ArrayList<>();
    private final OnPostClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.FRANCE);

    public PostAdapter(OnPostClickListener listener){
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
        holder.LikesCount.setText(String.valueOf(currentPost.getLikesCount()));
        holder.CommentCount.setText(String.valueOf(currentPost.getCommentCount()));

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
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView Pseudo, Date, Description, LikesCount, CommentCount;
        ImageView PostImage, ProfilPicture, btnMore;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            ProfilPicture = itemView.findViewById(R.id.post_profil_picture);
            Pseudo = itemView.findViewById(R.id.post_pseudo);
            Date = itemView.findViewById(R.id.post_date);
            PostImage = itemView.findViewById(R.id.post_picture);
            Description = itemView.findViewById(R.id.post_description);
            LikesCount = itemView.findViewById(R.id.post_likes);
            CommentCount = itemView.findViewById(R.id.post_comment);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}