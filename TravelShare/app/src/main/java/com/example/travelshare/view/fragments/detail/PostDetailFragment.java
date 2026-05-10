package com.example.travelshare.view.fragments.detail;

import static com.example.travelshare.utils.NavigationUtils.showPostMenu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.adapter.CommentAdapter;
import com.example.travelshare.model.Comment;
import com.example.travelshare.model.Post;
import com.example.travelshare.model.User;
import com.example.travelshare.viewmodel.CommentViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PostDetailFragment extends Fragment {

    private CommentViewModel commentViewModel;
    private UserViewModel userViewModel;
    private PostViewModel postViewModel;
    private CommentAdapter commentAdapter;

    private Post currentPost;

    private TextView pseudo, date, description, likes, comments,
        detailDate, location, indications;
    private ImageView imgPost, imgProfil;
    private EditText inputComment;
    private ImageButton btnSend;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentViewModel = new ViewModelProvider(requireActivity()).get(CommentViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        initView(view);

        postViewModel.getSelectedPost().observe(getViewLifecycleOwner(), post -> {
            if (post != null) {
                this.currentPost = post;
                bindPostData();
                commentViewModel.fetchComment(post.getId());
            }
        });

        setupRecyclerView(view);

        commentViewModel.getComment().observe(getViewLifecycleOwner(), allComments -> {
            commentAdapter.setComments(allComments);
            comments.setText(allComments.size() + " commentaires");
        });

        btnSend.setOnClickListener(v -> sendComment());
    }

    private void initView(View view) {

        imgProfil = view.findViewById(R.id.post_detail_profil_picture);
        pseudo = view.findViewById(R.id.post_detail_pseudo);
        date = view.findViewById(R.id.post_detail_date);
        imgPost = view.findViewById(R.id.post_detail_picture);
        description = view.findViewById(R.id.post_description);

        likes = view.findViewById(R.id.post_detail_likes);
        comments = view.findViewById(R.id.post_detail_comment);

        detailDate = view.findViewById(R.id.post_detail_date_value);
        location = view.findViewById(R.id.post_detail_location_value);
        indications = view.findViewById(R.id.post_detail_indications_value);

        inputComment = view.findViewById(R.id.post_detail_comment_input);
        btnSend = view.findViewById(R.id.post_detail_btn_send);

        view.findViewById(R.id.post_detail_btn_adresse).setOnClickListener(v -> {
            if (currentPost.getLocation() != null) {
                double latitude = currentPost.getLocation().getLatitude();
                double longitude = currentPost.getLocation().getLongitude();
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", latitude, longitude, latitude, longitude);
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri));
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Aucune coordonnée disponible", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.post_detail_btn_like).setOnClickListener(v -> {
            User currentUser = userViewModel.getSelectedUser().getValue();
            if (currentUser != null) {
                postViewModel.toggleLike(currentPost, currentUser.getId());

            }
        });

        view.findViewById(R.id.post_detail_btn_more).setOnClickListener(v -> {
            showPostMenu(getContext(), view, currentPost);
        });
    }

    private void bindPostData() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);

        pseudo.setText(currentPost.getAuthorName());
        description.setText(currentPost.getDescription());
        likes.setText(currentPost.getLikesCount() + " likes");
        comments.setText(currentPost.getCommentCount() + " commentaires");

        if (currentPost.getDate() != null) {
            date.setText(timeFormat.format(currentPost.getDate()));
            detailDate.setText(timeFormat.format(currentPost.getDate()));
        }

        if (currentPost.getLocation() != null) {
            String locationStr = currentPost.getLocation().getLatitude() + "° N, "
                    + currentPost.getLocation().getLongitude() + "° E";

            location.setText(locationStr);
        } else {
            location.setText("Non spécifiée");
        }

        indications.setText(currentPost.getIndication() != null && !currentPost.getIndication().isEmpty() ?
                currentPost.getIndication() : "Aucune indication");

        Glide.with(this).load(currentPost.getImageUrl())
                .placeholder(R.drawable.img_app).centerCrop().into(imgPost);

        Glide.with(this).load(currentPost.getAuthorProfilPictureUrl())
                .placeholder(R.drawable.default_user).circleCrop().into(imgProfil);

        User currentUser = userViewModel.getSelectedUser().getValue();
        ImageButton btnLike = getView().findViewById(R.id.post_detail_btn_like);

        if (currentUser != null && currentPost.getLikers() != null && currentPost.getLikers().contains(currentUser.getId())) {
            btnLike.setColorFilter(android.graphics.Color.RED);
        } else {
            btnLike.clearColorFilter();
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerComments = view.findViewById(R.id.post_detail_comment_section);
        commentAdapter = new CommentAdapter();

        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerComments.setAdapter(commentAdapter);
    }

    private void sendComment() {
        if (currentPost == null) return;

        String text = inputComment.getText().toString().trim();
        if (text.isEmpty()) return;

        User currentUser = userViewModel.getSelectedUser().getValue();

        if (currentUser == null) {
            Toast.makeText(getContext(), "Erreur : Utilisateur non identifié. Réessayez dans un instant.", Toast.LENGTH_SHORT).show();
            // Optionnel : on peut tenter de recharger l'utilisateur ici si besoin
            return;
        }

        btnSend.setEnabled(false);

        Comment newComment = new Comment(
                currentPost.getId(),
                currentUser.getId(),
                currentUser.getPseudo(),
                text,
                currentUser.getProfilePictureUrl()
        );

        commentViewModel.addComment(newComment);

        inputComment.setText("");
        btnSend.setEnabled(true);
    }
}