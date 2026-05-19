package com.example.travelshare.view.fragments.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapter.NotificationAdapter;
import com.example.travelshare.model.Notification;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.NotificationViewModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private NotificationViewModel notificationViewModel;
    private AuthViewModel authViewModel;
    private NotificationAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar loader;
    private TextView emptyText;

    public NotificationsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        recyclerView = view.findViewById(R.id.notifications_recycler);
        loader = view.findViewById(R.id.notifications_loader);
        emptyText = view.findViewById(R.id.notifications_empty_text);

        setupRecyclerView();
        setupObservers();

        FirebaseUser currentUser = authViewModel.getCurrentUser();
        if (currentUser != null) {
            notificationViewModel.loadNotifications(currentUser.getUid());
        } else {
            emptyText.setText(R.string.hello_blank_fragment);
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notification -> {
            notificationViewModel.markAsRead(notification.getId());
            
            if (notification.getPostId() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("postId", notification.getPostId());
                Navigation.findNavController(requireView()).navigate(R.id.action_global_to_postDetailFragment, bundle);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        notificationViewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                adapter.setNotifications(new ArrayList<>());
            } else {
                emptyText.setVisibility(View.GONE);
                adapter.setNotifications(notifications);
            }
        });

        notificationViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            loader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }
}
