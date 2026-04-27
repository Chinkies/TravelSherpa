package com.example.travelshare.view.fragments.group;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapter.GroupAdapter;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;

public class GroupListFragment extends Fragment {

    private GroupViewModel groupViewModel;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_list, container, false);
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

        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);

        recyclerView = view.findViewById(R.id.group_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupAdapter = new GroupAdapter(clickedGroup -> {
            groupViewModel.selectGroup(clickedGroup);

            Navigation.findNavController(view).navigate(R.id.action_groupsFragment_to_groupDetailFragment);
        });
        recyclerView.setAdapter(groupAdapter);

        groupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            if (groups != null) {
                groupAdapter.setGroups(groups);
            }
        });

        groupViewModel.fetchGroups();

        view.findViewById(R.id.group_list_create).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_groupsFragment_to_groupCreateFragment);
        });
    }
}