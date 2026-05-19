package com.example.travelshare.view.fragments.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.model.User;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class NotificationSettingsFragment extends Fragment {

    private UserViewModel userViewModel;
    private PostViewModel postViewModel;
    private AuthViewModel authViewModel;

    private ChipGroup tagsChipGroup;
    private RecyclerView usersRecycler, groupsRecycler, lieuxRecycler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        Toolbar toolbar = view.findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        tagsChipGroup = view.findViewById(R.id.settings_tags_chip_group);
        usersRecycler = view.findViewById(R.id.settings_followed_users_recycler);
        groupsRecycler = view.findViewById(R.id.settings_followed_groups_recycler);
        lieuxRecycler = view.findViewById(R.id.settings_favorite_lieux_recycler);

        setupTags();
        setupFollowedLists();
    }

    private void setupTags() {
        postViewModel.loadOfficialTags();
        postViewModel.getOfficialTags().observe(getViewLifecycleOwner(), tags -> {
            if (tags != null) {
                tagsChipGroup.removeAllViews();
                User user = userViewModel.getCurrentUser().getValue();
                List<String> followedTags = (user != null) ? user.getFollowedTags() : null;

                for (String tag : tags) {
                    Chip chip = new Chip(getContext());
                    chip.setText(tag);
                    chip.setCheckable(true);
                    if (followedTags != null && followedTags.contains(tag)) {
                        chip.setChecked(true);
                    }

                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (user != null) {
                            userViewModel.toggleFollowTag(user.getId(), tag, isChecked);
                        }
                    });
                    tagsChipGroup.addView(chip);
                }
            }
        });
    }

    private void setupFollowedLists() {
        // Pour simplifier l'implémentation dans le cadre de cet exercice,
        // on pourrait créer des adaptateurs génériques ou utiliser des TextView dynamiques.
        // Ici, on va juste observer les données de l'utilisateur.
        
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                // Mise à jour des listes si nécessaire
                // (L'implémentation complète nécessiterait de charger les objets User/Group/Lieu correspondant aux IDs)
            }
        });
        
        // Configuration basique des LayoutManagers
        usersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        lieuxRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
