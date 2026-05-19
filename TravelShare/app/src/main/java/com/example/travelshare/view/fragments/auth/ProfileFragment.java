package com.example.travelshare.view.fragments.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.example.travelshare.model.User;
import com.example.travelshare.travelpath.adapter.ParcoursAdapter;
import com.example.travelshare.utils.NavigationUtils;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView profilPicture;
    private TextView profilPseudo, profilDesc, countPubs,
        countGroups, countLikes, countParcours;
    private Button btnAction;
    private ImageButton btnSettings, btnNotifications;
    private RadioGroup radioGroup;
    private RecyclerView recyclerPublications, recyclerGroups, recyclerParcours;

    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;
    private PostViewModel postViewModel;

    private PostAdapter postAdapter;
    private GroupAdapter groupAdapter;
    private ParcoursAdapter parcoursAdapter;


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
        countParcours = view.findViewById(R.id.profile_count_parcours);
        btnAction = view.findViewById(R.id.button_modification);
        btnSettings = view.findViewById(R.id.profile_btn_settings);
        btnNotifications = view.findViewById(R.id.profile_btn_notifications);
        radioGroup = view.findViewById(R.id.radio_group);
        recyclerPublications = view.findViewById(R.id.recycler_publications);
        recyclerGroups = view.findViewById(R.id.recycler_groups);
        recyclerParcours = view.findViewById(R.id.recycler_parcours);

        if (currentUserId.isEmpty()) {
            radioGroup.setVisibility(View.GONE);
            recyclerPublications.setVisibility(View.VISIBLE);
        } else {
            radioGroup.setVisibility(View.VISIBLE);
        }

        if (isMyProfile) {
            btnAction.setText("Modifier le profil");
            btnAction.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.VISIBLE);
            btnNotifications.setVisibility(View.VISIBLE);
            view.findViewById(R.id.profile_btn_logout).setVisibility(View.VISIBLE);

            btnAction.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("currentName", profilPseudo.getText().toString());
                bundle.putString("currentDesc", profilDesc.getText().toString());
                Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_profileEditFragment, bundle);
            });

            btnSettings.setOnClickListener(v -> {
                Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_notificationSettingsFragment);
            });

            btnNotifications.setOnClickListener(v -> {
                Navigation.findNavController(view).navigate(R.id.notificationsFragment);
            });
        } else {
            btnAction.setVisibility(View.GONE);
            btnSettings.setVisibility(View.GONE);
            btnNotifications.setVisibility(View.GONE);
            view.findViewById(R.id.profile_btn_logout).setVisibility(View.GONE);
        }

        view.findViewById(R.id.profile_btn_logout).setOnClickListener(v -> {
            authViewModel.logout();
            Navigation.findNavController(view).navigate(R.id.feedFragment);
        });

        setupRecyclerViews(view, currentUserId);
        setupObservers();

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            recyclerPublications.setVisibility(checkedId == R.id.radio_display_publications ? View.VISIBLE : View.GONE);
            recyclerGroups.setVisibility(checkedId == R.id.radio_display_groups ? View.VISIBLE : View.GONE);
            recyclerParcours.setVisibility(checkedId == R.id.radio_display_parcours ? View.VISIBLE : View.GONE);
        });

        if (!visitedUserId.isEmpty()) {
            userViewModel.loadUser(visitedUserId);
            
            // On ne recharge les posts que si la liste est vide ou si on a changé d'utilisateur
            List<Post> currentPosts = postViewModel.getUserPosts().getValue();
            if (currentPosts == null || currentPosts.isEmpty() || !currentPosts.get(0).getAuthorId().equals(visitedUserId)) {
                postViewModel.loadUserPosts(visitedUserId);
            }

            userViewModel.fetchUserParcours(visitedUserId);
        } else {
            profilPseudo.setText("Mode Invité");
            profilDesc.setText("Connectez-vous pour accéder à votre profil.");
            radioGroup.setVisibility(View.GONE);
            btnAction.setVisibility(View.GONE);
            view.findViewById(R.id.profile_btn_logout).setVisibility(View.GONE);
        }
    }

    private void setupRecyclerViews(View view, String currentUserId) {
        recyclerPublications.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(currentUserId.isEmpty() ? null : currentUserId, new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(Post post) {
                postViewModel.selectPost(post);
                Bundle b = new Bundle();
                b.putString("postId", post.getId());
                Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_postDetailFragment, b);
            }
            @Override public void onProfileClick(String id) { }
            @Override public void onMoreClick(View v, Post post) {
                NavigationUtils.showPostMenu(requireContext(), v, post, currentUserId, postViewModel);
            }
            @Override public void onLikeClick(Post post) {
                User currentUser = userViewModel.getCurrentUser().getValue();
                if (currentUser != null) {
                    postViewModel.toggleLike(post, currentUser);
                }
            }
            @Override public void onCommentClick(Post post) { onPostClick(post); }
        });
        recyclerPublications.setAdapter(postAdapter);

        recyclerGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        groupAdapter = new GroupAdapter(group -> {
            groupViewModel.selectGroup(group);
            Navigation.findNavController(view).navigate(R.id.action_global_to_groupDetailFragment);
        });
        recyclerGroups.setAdapter(groupAdapter);

        recyclerParcours.setLayoutManager(new LinearLayoutManager(getContext()));
        parcoursAdapter = new ParcoursAdapter(new ArrayList<>(), parcours -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("parcours", parcours);
            Navigation.findNavController(view).navigate(R.id.action_profileFragment_to_resultatFragment, bundle);
        });
        recyclerParcours.setAdapter(parcoursAdapter);
    }

    private void setupObservers() {
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && postAdapter != null) {
                postAdapter.updateUserId(user.getId());
            }
        });

        userViewModel.getSelectedUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                profilPseudo.setText(user.getPseudo());
                profilDesc.setText(user.getDescription());
                countGroups.setText(String.valueOf(user.getNbGroups()));
                String url = user.getProfilePictureUrl();
                Glide.with(this)
                        .load(url != null && !url.trim().isEmpty() ? url : null)
                        .placeholder(R.drawable.default_user)
                        .error(R.drawable.default_user)
                        .circleCrop()
                        .into(profilPicture);
            }
        });

        postViewModel.getUserPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                postAdapter.setPosts(posts);
                countPubs.setText(String.valueOf(posts.size()));
                int likes = 0;
                for(Post p : posts) likes += p.getLikesCount();
                countLikes.setText(String.valueOf(likes));
            }
        });

        userViewModel.getUserGroup().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                groupAdapter.setGroups(groups);
                countGroups.setText(String.valueOf(groups.size()));
            }
        });

        userViewModel.getUserParcours().observe(getViewLifecycleOwner(), parcours -> {
            if (parcours != null) {
                parcoursAdapter.setParcoursList(parcours);
                if (countParcours != null) countParcours.setText(String.valueOf(parcours.size()));
            }
        });

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}
