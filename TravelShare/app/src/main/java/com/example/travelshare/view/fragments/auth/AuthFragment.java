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
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class AuthFragment extends Fragment {

    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        TextInputEditText inputEmail = view.findViewById(R.id.auth_text_email_input);
        TextInputEditText inputPassword = view.findViewById(R.id.auth_password_input);

        // On n'observe plus authViewModel.getUser() pour quitter l'écran, 
        // on attend que le profil Firestore soit chargé via UserViewModel
        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                Toast.makeText(getContext(), "Connexion réussie !", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            }
        });

        authViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), "Erreur : " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        view.findViewById(R.id.auth_button_create_account).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_login_to_register);
        });

        view.findViewById(R.id.auth_button_connection).setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.login(email, password);
        });
    }
}
