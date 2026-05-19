package com.example.travelshare.view.fragments.group;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.travelshare.R;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;

public class GroupCreateFragment extends Fragment {

    private GroupViewModel groupViewModel;
    private Uri selectedImageUri;
    private ImageView imgPreview;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgPreview.setImageURI(uri);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_create, container, false);
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

        EditText editName = view.findViewById(R.id.group_create_name_input);
        EditText editDesc = view.findViewById(R.id.group_create_desc_input);
        imgPreview = view.findViewById(R.id.group_create_picture);

        imgPreview.setOnClickListener(v -> pickImage.launch("image/*"));

        view.findViewById(R.id.group_create_btn_create).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String desc = editDesc.getText().toString().trim();
            String userId = authViewModel.getCurrentUser().getUid();

            if (!name.isEmpty()) {
                groupViewModel.createNewGroup(requireContext(), name, desc, userId, selectedImageUri);
            } else {
                Toast.makeText(getContext(), "Le nom est requis", Toast.LENGTH_SHORT).show();
            }
        });

        groupViewModel.getGroupCreated().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Groupe créé avec succès !", Toast.LENGTH_SHORT).show();

                androidx.navigation.NavOptions navOptions = new androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.groupCreateFragment, true)
                        .build();

                Navigation.findNavController(view).navigate(
                        R.id.action_groupCreateFragment_to_groupDetailFragment,
                        null,
                        navOptions
                );
                groupViewModel.resetGroupCreated();
            }
        });
    }
}
