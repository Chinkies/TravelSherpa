package com.example.travelshare.view.fragments.auth;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.UserViewModel;

public class ProfileModifyFragment extends Fragment {

    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;

    private EditText inputName, inputDescription;
    private ImageView profileImage;
    private ProgressBar progressBar;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(this).load(uri).circleCrop().into(profileImage);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile__modify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        userViewModel.clearError();

        inputName = view.findViewById(R.id.profile_modify_name_input);
        inputDescription = view.findViewById(R.id.profile_modify_desc_input);
        profileImage = view.findViewById(R.id.profile_modify_picture);
        progressBar = view.findViewById(R.id.profile_modify_progress);

        if (getArguments() != null) {
            inputName.setText(getArguments().getString("currentName"));
            inputDescription.setText(getArguments().getString("currentDesc"));
        }

        if (userViewModel.getSelectedUser().getValue() != null) {
            Glide.with(this)
                    .load(userViewModel.getSelectedUser().getValue().getProfilePictureUrl())
                    .placeholder(R.drawable.default_user)
                    .circleCrop()
                    .into(profileImage);
        }

        view.findViewById(R.id.btn_save_profile).setOnClickListener(v -> {
            saveData(view);
        });

        profileImage.setOnClickListener(v ->
                galleryLauncher.launch("image/*")
        );

        userViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    private void saveData(View view) {
        String newName = inputName.getText().toString().trim();
        String newDesc = inputDescription.getText().toString().trim();
        String uid = authViewModel.getCurrentUser().getUid();

        if (newName.isEmpty()) {
            inputName.setError("Le pseudo ne peut pas être vide");
            return;
        }

        if (selectedImageUri != null) {
            userViewModel.updateProfileWithImage(requireContext(), uid, newName, newDesc, selectedImageUri);
        } else {
            userViewModel.updateMyProfile(uid, newName, newDesc);
        }

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error == null) {
                Toast.makeText(getContext(), "Profil mis à jour !", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            } else {
                Toast.makeText(getContext(), "Erreur : " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
