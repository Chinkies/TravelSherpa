package com.example.travelshare.view.fragments.navigation;

import static com.example.travelshare.utils.NavigationUtils.showPostMenu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.travelshare.R;
import com.example.travelshare.adapter.PostAdapter;
import com.example.travelshare.model.Post;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private TextInputEditText searchInput;
    private ImageButton btnMap, btnMic;
    private ChipGroup chipGroupTags;
    private RecyclerView recyclerResults;

    private PostViewModel postViewModel;
    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;
    private PostAdapter postAdapter;

    private List<Post> loadedPost = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        searchInput = view.findViewById(R.id.search_text_search);
        btnMap = view.findViewById(R.id.search_btn_map);
        btnMic = view.findViewById(R.id.search_btn_mic);
        chipGroupTags = view.findViewById(R.id.search_chip_group);
        recyclerResults = view.findViewById(R.id.search_recycler_result);

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        postAdapter = new PostAdapter(currentUserId.isEmpty() ? null : currentUserId ,new PostAdapter.OnPostClickListener() {
            public void onPostClick(Post post) {
                postViewModel.selectPost(post);
                Bundle bundle = new Bundle();
                bundle.putString("postId", post.getId());
                Navigation.findNavController(view).navigate(R.id.action_global_to_postDetailFragment, bundle);
            }

            @Override
            public void onProfileClick(String userId) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                Navigation.findNavController(view).navigate(R.id.action_global_to_profileFragment, bundle);
            }

            @Override
            public void onMoreClick(View v, Post post) {
                showPostMenu(requireContext(), view, post);
            }

            @Override
            public void onLikeClick(Post post) {
                if (currentUserId.isEmpty()) {
                    Toast.makeText(getContext(), "Veuillez vous connecter pour liker une publication", Toast.LENGTH_SHORT).show();
                } else {
                    postViewModel.toggleLike(post, currentUserId);
                }
            }

            @Override
            public void onCommentClick(Post post) {
                onPostClick(post);
            }
        });

        recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerResults.setAdapter(postAdapter);

        postViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                loadedPost = posts;
                applyFilters();
            }
        });

        chipGroupTags.setSingleSelection(true);
        chipGroupTags.setSelectionRequired(true);

        postViewModel.loadOfficialTags();
        postViewModel.getOfficialTags().observe(getViewLifecycleOwner(), tags -> {
            if (tags != null) {
                chipGroupTags.removeAllViews();

                addChipToGroup("Tout", true);

                for (String tag : tags) {
                    addChipToGroup(tag, false);
                }
            }
        });

        chipGroupTags.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    String selectedTag = chip.getText().toString();
                    if (selectedTag.equals("Tout")) {
                        postViewModel.loadFeed();
                    } else {
                        postViewModel.loadPostsByTag(selectedTag);
                    }
                }
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
        });

        btnMap.setOnClickListener(v -> Toast.makeText(getContext(), "Ajouter Map", Toast.LENGTH_SHORT).show());
        btnMic.setOnClickListener(v -> Toast.makeText(getContext(), "Ajouter Mic", Toast.LENGTH_SHORT).show());
        postViewModel.loadFeed();
    }

    private void addChipToGroup(String text, boolean isCheckedByDefault) {
        Chip chip = new Chip(requireContext());
        chip.setId(View.generateViewId());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);

        chip.setCheckedIconVisible(true);

        if (isCheckedByDefault) {
            chip.setChecked(true);
        }

        chipGroupTags.addView(chip);
    }
    private void applyFilters() {
        String constraint = (searchInput.getText() != null) ? searchInput.getText().toString().trim().toLowerCase() : "";

        if (constraint.isEmpty()) {
            postAdapter.setPosts(loadedPost);
            return;
        }

        List<Post> filteredList = new ArrayList<>();
        for (Post post : loadedPost) {
            if (post.getDescription() != null && post.getDescription().toLowerCase().contains(constraint)) {
                filteredList.add(post);
            }
        }
        postAdapter.setPosts(filteredList);
    }
}