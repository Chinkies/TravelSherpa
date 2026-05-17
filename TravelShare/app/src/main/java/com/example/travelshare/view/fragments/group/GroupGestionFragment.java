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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.travelshare.R;
import com.example.travelshare.adapter.UserAdapter;
import com.example.travelshare.model.User;
import com.example.travelshare.repository.FireStoreCallBack;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class GroupGestionFragment extends Fragment {

    private GroupViewModel groupViewModel;
    private RecyclerView recyclerViewAdmin;
    private RecyclerView recyclerViewMember;
    private UserAdapter userAdapterAdmin;
    private UserAdapter userAdapterMember;

    private EditText editName;
    private EditText editDesc;
    private ImageView groupImage;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    groupImage.setImageURI(uri);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_gestion, container, false);
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

        editName = view.findViewById(R.id.group_modify_name_input);
        editDesc = view.findViewById(R.id.group_modify_desc_input);
        groupImage = view.findViewById(R.id.group_modify_picture);

        groupViewModel.getSelectedGroup().observe(getViewLifecycleOwner(), group -> {
            if (group != null) {
                if (editName != null) editName.setText(group.getGroupName());
                if (editDesc != null) editDesc.setText(group.getDescription());

                if (group.getImageUrl() != null && !group.getImageUrl().isEmpty()){
                    Glide.with(this).load(group.getImageUrl()).into(groupImage);
                }
            }
        });

        view.findViewById(R.id.group_modify_edit_image).setOnClickListener(v -> {
            mGetContent.launch("image/*");
        });

        view.findViewById(R.id.group_modify_btn_delete_group)
                .setOnClickListener(v -> showDeleteGroupDialog());

        view.findViewById(R.id.group_modify_button_save).setOnClickListener(v -> {
            saveChanges();
        });

        view.findViewById(R.id.group_modify_button_add_member).setOnClickListener(v -> {
            showAddMemberSheet();
        });

        recyclerViewAdmin = view.findViewById(R.id.group_gestion_recycler_admin);
        recyclerViewAdmin.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewMember = view.findViewById(R.id.group_gestion_recycler_members);
        recyclerViewMember.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapterAdmin = new UserAdapter(R.layout.item_admin, new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(User user) {
                showDeleteAdminConfirmation(user);
            }

            @Override
            public void onPromote(User user) {}
        });

        recyclerViewAdmin.setAdapter(userAdapterAdmin);

        userAdapterMember = new UserAdapter(R.layout.item_member, new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(User user) {
                showDeleteMemberConfirmation(user);
            }

            @Override
            public void onPromote(User user) {
                showPromotionConfirmation(user);
            }
        });

        recyclerViewMember.setAdapter(userAdapterMember);

        groupViewModel.getAdmins().observe(getViewLifecycleOwner(), users -> {
            userAdapterAdmin.setUsers(users);
        });
        groupViewModel.getMembers().observe(getViewLifecycleOwner(), users -> {
            userAdapterMember.setUsers(users);
        });

        groupViewModel.fetchMembersDetails();
    }

    private void saveChanges() {
        String name = editName.getText().toString();
        String desc = editDesc.getText().toString();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Le nom est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        groupViewModel.updateGroupInfo(name, desc, (selectedImageUri != null ? selectedImageUri : null));
        Toast.makeText(getContext(), "Changement Sauvegardés !", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteGroupDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Suppression du groupe")
                .setMessage("Voulez-vous vraiment supprimer ce groupe définitivement ?")
                .setPositiveButton("Supprimer", (d, w) -> {
                    groupViewModel.deleteCurrentGroup(new FireStoreCallBack<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Navigation.findNavController(getView()).navigateUp();
                        }
                        @Override public void onFailure(String e) {}
                    });
                })
                .setNegativeButton("Annuler", null).show();
    }

    private void showDeleteAdminConfirmation(User user){
        new AlertDialog.Builder(requireContext()).setTitle("Retirer l'admin")
                .setMessage("Voulez-vous retirer les droits admin de " + user.getPseudo() + " ?")
                .setPositiveButton("Rétrograder en membre", (dialog, which) -> {
                    groupViewModel.demoteToMember(user);
                })
                .setNegativeButton("Supprimer du groupe", (dialog, which) -> {
                    groupViewModel.removeMember(user);
                })
                .setNeutralButton("Annuler", null).show();
    }
    private void showDeleteMemberConfirmation(User user){
        new AlertDialog.Builder(requireContext()).setTitle("Supprimer un Membre")
                .setMessage("Voulez-vous retirer " + user.getPseudo()
                + " du groupe ?").setPositiveButton("Supprimer", (dialog, which) -> {
                    groupViewModel.removeMember(user);
                }).setNegativeButton("Annuler", null).show();
    }

    private void showPromotionConfirmation(User user){
        new AlertDialog.Builder(requireContext()).setTitle("Promouvoir")
                .setMessage("Promouvoir " + user.getPseudo() + " admin ?")
                .setPositiveButton("Oui", (dialog, which) -> groupViewModel.promoteToAdmin(user))
                .setNegativeButton("Annuler", null).show();
    }

    private void showAddMemberSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());

        View view = getLayoutInflater().inflate(R.layout.item_add_member, null);
        bottomSheetDialog.setContentView(view);

        RecyclerView recyclerView = view.findViewById(R.id.item_group_results_recycler);
        EditText searchInput = view.findViewById(R.id.item_group_search_input);

        UserAdapter searchAdapter = new UserAdapter(R.layout.item_member, new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(User user) { }

            @Override
            public void onPromote(User user) {
                groupViewModel.addMember(user);
                bottomSheetDialog.dismiss();
                android.widget.Toast.makeText(getContext(), user.getPseudo() + " ajouté !", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        searchAdapter.setSearchMode(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(searchAdapter);

        groupViewModel.getSearchResults().observe(getViewLifecycleOwner(), users -> {
            searchAdapter.setUsers(users);
        });

        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    groupViewModel.searchUsers(s.toString());
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        bottomSheetDialog.show();
    }
}