package com.example.travelshare.view.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.adapter.GroupAdapter;
import com.example.travelshare.adapter.PostAdapter;
import com.example.travelshare.model.Post;
import com.example.travelshare.utils.NavigationUtils;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ImageView profilPicture;
    private TextView profilPseudo, profilDesc, countPubs,
        countGroups, countLikes;
    private Button btnAction;
    private RadioGroup radioGroup;
    private RecyclerView recyclerPublications, recyclerGroups;

    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;
    private PostViewModel postViewModel;

    private PostAdapter postAdapter;
    private GroupAdapter groupAdapter;


    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        String visitedUserId = (getArguments() != null) ? getArguments().getString("userId") : currentUserId;
        boolean isMyProfile = !currentUserId.isEmpty() && visitedUserId.equals(currentUserId);

        profilPicture = view.findViewById(R.id.profil_picture);
        profilPseudo = view.findViewById(R.id.profil_pseudo);
        profilDesc = view.findViewById(R.id.profil_description);
        countPubs = view.findViewById(R.id.profile_count_publications);
        countGroups = view.findViewById(R.id.profile_count_groups);
        countLikes = view.findViewById(R.id.profile_count_likes);
        btnAction = view.findViewById(R.id.button_modification);
        radioGroup = view.findViewById(R.id.radio_group);
        recyclerPublications = view.findViewById(R.id.recycler_publications);
        recyclerGroups = view.findViewById(R.id.recycler_groups);

        if (currentUserId.isEmpty()) {
            radioGroup.setVisibility(View.GONE);
            recyclerPublications.setVisibility(View.VISIBLE);
            recyclerGroups.setVisibility(View.GONE);

            view.findViewById(R.id.profile_count_groups).setVisibility(View.GONE);
        } else {
            radioGroup.setVisibility(View.VISIBLE);
        }

        if (isMyProfile) {
            btnAction.setText("Modifier le profil");
            btnAction.setVisibility(View.VISIBLE);
            view.findViewById(R.id.profile_btn_logout).setVisibility(View.VISIBLE);

            btnAction.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("currentName", profilPseudo.getText().toString());
                bundle.putString("currentDesc", profilDesc.getText().toString());
                Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_profileEditFragment, bundle);
            });
        } else {
            btnAction.setVisibility(View.GONE);
            view.findViewById(R.id.profile_btn_logout).setVisibility(View.GONE);
        }

        view.findViewById(R.id.profile_btn_logout).setOnClickListener(v -> {
            authViewModel.logout();
            Navigation.findNavController(view).navigate(R.id.feedFragment);
        });

        setupRecyclerViews(recyclerPublications, recyclerGroups, view, currentUserId);
        setupObservers(profilPseudo, profilDesc, profilPicture, countPubs, countGroups, countLikes);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_display_publications) {
                recyclerPublications.setVisibility(View.VISIBLE);
                recyclerGroups.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_display_groups) {
                recyclerPublications.setVisibility(View.GONE);
                recyclerGroups.setVisibility(View.VISIBLE);
            }
        });

        if (!visitedUserId.isEmpty()) {
            userViewModel.loadUser(visitedUserId);
            postViewModel.loadUserPosts(visitedUserId);
        } else {
            profilPseudo.setText("Mode Invité");
            profilDesc.setText("Connectez-vous pour accéder à votre profil.");
            radioGroup.setVisibility(View.GONE);
            btnAction.setVisibility(View.GONE);
            view.findViewById(R.id.profile_btn_logout).setVisibility(View.GONE);
        }
    }

    private void setupRecyclerViews(RecyclerView recyclerPubs, RecyclerView recyclerGrps, View view, String currentUserId) {
        recyclerPubs.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(currentUserId.isEmpty() ? null : currentUserId, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(Post post) {
                postViewModel.selectPost(post);
                Bundle b = new Bundle();
                b.putString("postId", post.getId());
                Navigation.findNavController(view).navigate(R.id.action_global_to_postDetailFragment, b);
            }
            @Override public void onProfileClick(String id) { }
            @Override public void onMoreClick(View v, Post post) {
                NavigationUtils.showPostMenu(requireContext(), v, post);
            }
            @Override public void onLikeClick(Post post) {
                if (currentUserId.isEmpty()) {
                    Toast.makeText(getContext(), "Veuillez vous connecter pour liker un post", Toast.LENGTH_SHORT).show();
                } else {
                    postViewModel.toggleLike(post, currentUserId);
                }
            }
            @Override public void onCommentClick(Post post) {
                onPostClick(post);
            }
        });
        recyclerPubs.setAdapter(postAdapter);

        recyclerGrps.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter(group -> {
            groupViewModel.selectGroup(group);
            Navigation.findNavController(view).navigate(R.id.action_global_to_groupDetailFragment);
        });
        recyclerGrps.setAdapter(groupAdapter);
    }

    private void setupObservers(TextView pseudo, TextView desc, ImageView pic, TextView pubs, TextView grps, TextView likes) {
        userViewModel.getSelectedUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                pseudo.setText(user.getPseudo());
                desc.setText(user.getDescription());

                countPubs.setText(String.valueOf(user.getNbPublications()));
                countLikes.setText(String.valueOf(user.getNbLikes()));
                countGroups.setText(String.valueOf(user.getNbGroups()));

                String url = user.getProfilePictureUrl();
                Glide.with(this)
                        .load(url != null && !url.trim().isEmpty() ? url : null)
                        .placeholder(R.drawable.default_user)
                        .error(R.drawable.default_user)
                        .circleCrop()
                        .into(pic);
            }
        });

        postViewModel.getUserPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                FirebaseUser currentUser = authViewModel.getCurrentUser();
                String currentUserId = (currentUser != null) ? currentUser.getUid() : "";
                String visitedUserId = (getArguments() != null) ? getArguments().getString("userId") : currentUserId;
                boolean isMyProfile = !currentUserId.isEmpty() && visitedUserId.equals(currentUserId);

                java.util.List<Post> displayedPosts = new java.util.ArrayList<>();
                int totalLikes = 0;

                for (Post post : posts) {
                    if (isMyProfile || post.isPublic()) {
                        displayedPosts.add(post);
                        totalLikes += post.getLikesCount();
                    }
                }

                postAdapter.setPosts(displayedPosts);

                pubs.setText(String.valueOf(displayedPosts.size()));
                likes.setText(String.valueOf(totalLikes));

            } else {
                pubs.setText("0");
                likes.setText("0");
            }
        });
        userViewModel.getUserGroup().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                groupAdapter.setGroups(groups);
                grps.setText(String.valueOf(groups.size()));
            } else {
                grps.setText("0");
            }
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}