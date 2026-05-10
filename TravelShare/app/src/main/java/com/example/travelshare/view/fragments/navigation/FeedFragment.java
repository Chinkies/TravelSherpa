package com.example.travelshare.view.fragments.navigation;

import static com.example.travelshare.utils.NavigationUtils.showPostMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapter.PostAdapter;
import com.example.travelshare.model.Post;
import com.example.travelshare.viewmodel.PostViewModel;

public class FeedFragment extends Fragment {

    private PostViewModel postViewModel;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        recyclerView = view.findViewById(R.id.feed_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(new PostAdapter.OnPostClickListener() {
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
        });
        recyclerView.setAdapter(postAdapter);

        ProgressBar progressBar = view.findViewById(R.id.feed_progress_bar);
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);

        postViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.setPosts(posts);
        });

        postViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Erreur : " + error, Toast.LENGTH_SHORT).show();
            }
        });

        postViewModel.loadFeed();
    }
}