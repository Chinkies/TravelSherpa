package com.example.travelshare.view.fragments.group;

import static com.example.travelshare.utils.NavigationUtils.showPostMenu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapter.PostAdapter;
import com.example.travelshare.model.Post;
import com.example.travelshare.model.User;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;

import java.util.Map;

public class GroupDetailFragment extends Fragment {

    private GroupViewModel groupViewModel;
    private PostViewModel postViewModel;
    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        if (authViewModel.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Connectez-vous pour accéder à cette page", android.widget.Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        TextView title = view.findViewById(R.id.group_detail_name);

        recyclerView = view.findViewById(R.id.group_detail_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(null, new PostAdapter.OnPostClickListener() {
            @Override
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
                showPostMenu(requireContext(), v, post);
            }

            @Override
            public void onLikeClick(Post post) {
                User currentUser = userViewModel.getCurrentUser().getValue();
                if (currentUser != null) {
                    postViewModel.toggleLike(post, currentUser);
                } else {
                    Toast.makeText(getContext(), "Veuillez patienter...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCommentClick(Post post) {
                onPostClick(post);
            }
        });
        recyclerView.setAdapter(postAdapter);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                postAdapter.updateUserId(user.getId());
            }
        });

        groupViewModel.getSelectedGroup().observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                title.setText(group.getGroupName());
                postViewModel.loadGroupPosts(group.getId());

                String currentUserId = authViewModel.getCurrentUser().getUid();
                View btnManage = view.findViewById(R.id.group_detail_manage);

                Map<String, Boolean> members = group.getMembers();
                if (members.containsKey(currentUserId) && members.get(currentUserId)) {
                    btnManage.setVisibility(View.VISIBLE);
                } else {
                    btnManage.setVisibility(View.GONE);
                }
            }
        });

        postViewModel.getGroupPost().observe(getViewLifecycleOwner(), posts -> {
            postAdapter.setPosts(posts);
        });

        view.findViewById(R.id.group_detail_manage).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_groupDetailFragment_to_groupGestionFragment);
        });
    }
}
