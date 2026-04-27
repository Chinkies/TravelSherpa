package com.example.travelshare.view.fragments.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.travelshare.R;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AuthCreationFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth_creation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        TextInputEditText inputEmail = view.findViewById(R.id.auth_creation_text_email_input);
        TextInputEditText inputPassword = view.findViewById(R.id.auth_creation_password_input);
        TextInputEditText inputConfirm = view.findViewById(R.id.auth_creation_text_confirmation_input);
        TextInputEditText inputPseudo = view.findViewById(R.id.auth_creation_pseudo_input);

        authViewModel.getUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                Toast.makeText(getContext(), "Compte créé !", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            }
        });

        authViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), "Erreur : " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        view.findViewById(R.id.auth_creation_button_inscription).setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            String confirm = inputConfirm.getText().toString().trim();
            String pseudo = inputPseudo.getText().toString().trim();

            if (pseudo.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pseudo.length() < 3) {
                Toast.makeText(getContext(), "Pseudo trop court !", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(getContext(), "Les mots de passe ne correspondent pas !", Toast.LENGTH_SHORT).show();
            }

            if (password.length() < 8) {
                Toast.makeText(getContext(), "Le mot de passe doit faire au minimum 8 caractères !", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.register(email, password, pseudo);
        });
    }
}