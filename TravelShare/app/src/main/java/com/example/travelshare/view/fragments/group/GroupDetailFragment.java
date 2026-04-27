package com.example.travelshare.view.fragments.group;

import static com.example.travelshare.utils.NavigationUtils.showPostMenu;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapter.PostAdapter;
import com.example.travelshare.model.Post;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;

public class GroupDetailFragment extends Fragment {

    private GroupViewModel groupViewModel;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthViewModel authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        if (authViewModel.getCurrentUser() == null) {
            android.widget.Toast.makeText(getContext(), "Connectez-vous pour accéder à cette page", android.widget.Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        TextView title = view.findViewById(R.id.group_detail_name);

        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        groupViewModel.getSelectedGroup().observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                title.setText(group.getGroupName());

                // FAUT RECUP LES POSTS DES GROUPES !!!!
            }
        });

        recyclerView = view.findViewById(R.id.group_detail_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postAdapter = new PostAdapter(new PostAdapter.OnPostClickListener() {
            @Override
            public void onPostClick(Post post) {
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
        });
        recyclerView.setAdapter(postAdapter);

        view.findViewById(R.id.group_detail_manage).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_groupDetailFragment_to_groupGestionFragment);
        });
    }
}