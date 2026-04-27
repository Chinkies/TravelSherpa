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
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;

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

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        String visitedUserId = (getArguments() != null) ? getArguments().getString("userId") : currentUserId;
        boolean isMyProfile = visitedUserId.equals(currentUserId);

        ImageView profilPicture = view.findViewById(R.id.profil_picture);
        TextView profilPseudo = view.findViewById(R.id.profil_pseudo);
        TextView profilDesc = view.findViewById(R.id.profil_description);
        TextView countPubs = view.findViewById(R.id.profile_count_publications);
        TextView countGroups = view.findViewById(R.id.profile_count_groups);
        TextView countLikes = view.findViewById(R.id.profile_count_likes);
        Button btnAction = view.findViewById(R.id.button_modification);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        RecyclerView recyclerPublications = view.findViewById(R.id.recycler_publications);
        RecyclerView recyclerGroups = view.findViewById(R.id.recycler_groups);


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

        setupRecyclerViews(recyclerPublications, recyclerGroups, view);

        setupObservers(profilPseudo, profilDesc, profilPicture, countPubs, countGroups, countLikes);

        userViewModel.loadUser(visitedUserId);
    }

    private void setupRecyclerViews(RecyclerView recyclerPubs, RecyclerView recyclerGrps, View view) {
        recyclerPubs.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(Post post) {
                Bundle b = new Bundle();
                b.putString("postId", post.getId());
                Navigation.findNavController(view).navigate(R.id.action_global_to_postDetailFragment, b);
            }
            @Override public void onProfileClick(String id) { }
            @Override public void onMoreClick(View v, Post post) {
                NavigationUtils.showPostMenu(requireContext(), v, post);
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
                pubs.setText(String.valueOf(user.getNbPublications()));
                grps.setText(String.valueOf(user.getNbGroups()));
                likes.setText(String.valueOf(user.getNbLikes()));

                String url = user.getProfilePictureUrl();
                Glide.with(this)
                        .load(url != null && !url.trim().isEmpty() ? url : null)
                        .placeholder(R.drawable.default_user)
                        .error(R.drawable.default_user)
                        .circleCrop()
                        .into(pic);
            }
        });

        userViewModel.getUserPost().observe(getViewLifecycleOwner(), posts -> postAdapter.setPosts(posts));
        userViewModel.getUserGroup().observe(getViewLifecycleOwner(), groups -> groupAdapter.setGroups(groups));

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }
}