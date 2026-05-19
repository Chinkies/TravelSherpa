package com.example.travelshare.view.fragments.detail;

import static com.example.travelshare.utils.NavigationUtils.showPostMenu;
import static com.example.travelshare.utils.NavigationUtils.showCommentMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.adapter.CommentAdapter;
import com.example.travelshare.model.Comment;
import com.example.travelshare.model.Post;
import com.example.travelshare.model.User;
import com.example.travelshare.viewmodel.CommentViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

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
    private MaterialButton btnViewLieu;

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
                
                if ((currentPost.getIndication() != null && !currentPost.getIndication().isEmpty()) || currentPost.getLocation() != null) {
                    btnViewLieu.setVisibility(View.VISIBLE);
                } else {
                    btnViewLieu.setVisibility(View.GONE);
                }
            }
        });

        commentViewModel.getComment().observe(getViewLifecycleOwner(), allComments -> {
            if (allComments != null) {
                commentAdapter.setComments(allComments);
                comments.setText(allComments.size() + " commentaires");

                if (userViewModel.getCurrentUser().getValue() != null && currentPost != null) {
                    postViewModel.updatePostCommentCount(currentPost.getId(), allComments.size());
                }
            }
        });

        postViewModel.getPostDeleted().observe(getViewLifecycleOwner(), isDeleted -> {
            if (isDeleted) {
                postViewModel.resetPostDeleted();
                Navigation.findNavController(view).popBackStack();
            }
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
        btnViewLieu = view.findViewById(R.id.post_detail_btn_view_lieu);

        View.OnClickListener goToProfileListener = v -> {
            if (currentPost != null && currentPost.getAuthorId() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", currentPost.getAuthorId());
                Navigation.findNavController(view).navigate(R.id.action_global_to_profileFragment, bundle);
            }
        };

        imgProfil.setOnClickListener(goToProfileListener);
        pseudo.setOnClickListener(goToProfileListener);

        view.findViewById(R.id.post_detail_btn_adresse).setOnClickListener(v -> {
            if (currentPost != null && currentPost.getLocation() != null) {
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
            User currentUser = userViewModel.getCurrentUser().getValue();
            if (currentUser != null && currentPost != null) {
                postViewModel.toggleLike(currentPost, currentUser);
            }
        });

        view.findViewById(R.id.post_detail_btn_more).setOnClickListener(v -> {
            if (currentPost != null) {
                User currentUser = userViewModel.getCurrentUser().getValue();
                String uid = (currentUser != null) ? currentUser.getId() : "";
                showPostMenu(getContext(), view.findViewById(R.id.post_detail_btn_more), currentPost, uid, postViewModel);
            }
        });

        btnViewLieu.setOnClickListener(v -> {
            if (currentPost != null) {
                Bundle bundle = new Bundle();
                
                if (currentPost.getLieuId() != null) {
                    bundle.putString("LIEU_ID", currentPost.getLieuId());
                }

                if (currentPost.getLocation() != null) {
                    bundle.putDouble("LATITUDE", currentPost.getLocation().getLatitude());
                    bundle.putDouble("LONGITUDE", currentPost.getLocation().getLongitude());
                }

                // Toujours passer l'indication pour le champ "Lieu favori"
                if (currentPost.getIndication() != null && !currentPost.getIndication().isEmpty()) {
                    bundle.putString("SUGGESTED_LIEU", currentPost.getIndication());
                } else if (currentPost.getLocation() != null) {
                    // Si pas d'indication, on met les coords dans le lieu favori aussi
                    String coords = String.format(Locale.US, "%.5f, %.5f", 
                            currentPost.getLocation().getLatitude(), 
                            currentPost.getLocation().getLongitude());
                    bundle.putString("SUGGESTED_LIEU", coords);
                }

                if (currentPost.getTags() != null) {
                    bundle.putStringArrayList("POST_TAGS", new ArrayList<>(currentPost.getTags()));
                }

                Navigation.findNavController(view).navigate(R.id.preferenceFragment, bundle);
            }
        });

        setupRecyclerView(view);
    }

    private void bindPostData() {
        if (currentPost == null) return;

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        timeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        pseudo.setText(currentPost.getAuthorName());
        description.setText(currentPost.getDescription());
        likes.setText(currentPost.getLikesCount() + " likes");
        comments.setText(currentPost.getCommentCount() + " commentaires");

        if (currentPost.getDate() != null) {
            date.setText(timeFormat.format(currentPost.getDate()));
            detailDate.setText(timeFormat.format(currentPost.getDate()));
        }

        if (currentPost.getLocation() != null) {
            String locationStr = String.format(Locale.FRANCE, "%.4f° N, %.4f° E", 
                    currentPost.getLocation().getLatitude(), currentPost.getLocation().getLongitude());
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

        User currentUser = userViewModel.getCurrentUser().getValue();
        ImageButton btnLike = getView().findViewById(R.id.post_detail_btn_like);

        if (currentUser != null && currentPost.getLikers() != null && currentPost.getLikers().contains(currentUser.getId())) {
            btnLike.setColorFilter(android.graphics.Color.RED);
        } else {
            btnLike.clearColorFilter();
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerComments = view.findViewById(R.id.post_detail_comment_section);
        
        User currentUser = userViewModel.getCurrentUser().getValue();
        String uid = (currentUser != null) ? currentUser.getId() : "";
        
        commentAdapter = new CommentAdapter(uid, (v, comment) -> {
            showCommentMenu(getContext(), v, comment, uid, commentViewModel);
        });

        recyclerComments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerComments.setAdapter(commentAdapter);
    }

    private void sendComment() {
        if (currentPost == null) return;
        String text = inputComment.getText().toString().trim();
        if (text.isEmpty()) return;
        User currentUser = userViewModel.getCurrentUser().getValue();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Veuillez vous connecter pour commenter", Toast.LENGTH_SHORT).show();
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
        commentViewModel.addComment(newComment, currentPost, currentUser);
        inputComment.setText("");
        btnSend.setEnabled(true);
    }
}
