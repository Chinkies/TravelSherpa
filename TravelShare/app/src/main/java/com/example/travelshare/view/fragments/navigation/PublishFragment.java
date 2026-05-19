package com.example.travelshare.view.fragments.navigation;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.travelshare.R;
import com.example.travelshare.adapter.GroupAdapter;
import com.example.travelshare.adapter.SelectedGroupAdapter;
import com.example.travelshare.model.Group;
import com.example.travelshare.model.Post;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.GroupViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.example.travelshare.viewmodel.UserViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PublishFragment extends Fragment {

    private ImageView imgPicture;
    private TextInputEditText inputDescription, inputDate, inputLocalisation, inputIndication;
    private RadioGroup groupRadio;
    private ChipGroup chipGroup;
    private RecyclerView recyclerGroups;
    private Uri selectecImageUri;
    private SelectedGroupAdapter selectedGroupAdapter;
    private PostViewModel postViewModel;
    private GroupViewModel groupViewModel;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;

    private MaterialButton btnPublish, btnAddGroup;
    private ImageButton btnMicDescription, btnMicIndication;

    private ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectecImageUri = uri;
                    imgPicture.setImageURI(uri);
                    imgPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
    );

    private ActivityResultLauncher<Intent> voiceLauncherDescription = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        inputDescription.setText(matches.get(0));
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> voiceLauncherIndication = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        inputIndication.setText(matches.get(0));
                    }
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_publish, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        groupViewModel = new ViewModelProvider(requireActivity()).get(GroupViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        if (authViewModel.getCurrentUser() == null) {
            android.widget.Toast.makeText(getContext(), "Connectez-vous pour accéder à cette page", android.widget.Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        FirebaseUser user = authViewModel.getCurrentUser();
        if (user != null) {
            groupViewModel.fetchGroupsForUser(user.getUid());
        }

        postViewModel.resetPostCreated();
        groupViewModel.fetchGroups();

        imgPicture = view.findViewById(R.id.publish_btn_photo);
        inputDescription = view.findViewById(R.id.publish_description_input);
        inputDate = view.findViewById(R.id.publish_date_input);
        inputLocalisation = view.findViewById(R.id.publish_localisation_input);
        inputIndication = view.findViewById(R.id.publish_indication_input);
        groupRadio = view.findViewById(R.id.publish_group_visibility);
        recyclerGroups = view.findViewById(R.id.publish_group_recycler);
        btnPublish = view.findViewById(R.id.publish_button_publish);
        btnAddGroup = view.findViewById(R.id.publish_group_add);
        chipGroup = view.findViewById(R.id.publish_chip_group);
        btnMicDescription = view.findViewById(R.id.publish_btn_mic_description);
        btnMicIndication = view.findViewById(R.id.publish_btn_mic_indication);

        view.findViewById(R.id.publish_card_btn_photo).setOnClickListener(
                v -> pickImage.launch("image/*")
        );

        inputDate.setFocusable(false);
        inputDate.setOnClickListener(v -> showDatePicker());

        btnMicDescription.setOnClickListener(v -> startVoiceRecognition(voiceLauncherDescription));
        btnMicIndication.setOnClickListener(v -> startVoiceRecognition(voiceLauncherIndication));

        setupGroupsRecyclerView();

        selectedGroupAdapter = new SelectedGroupAdapter();
        recyclerGroups.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerGroups.setAdapter(selectedGroupAdapter);

        postViewModel.getPostCreated().observe(getViewLifecycleOwner(), created -> {
            if (created) {
                Toast.makeText(getContext(), "Publication réussie !", Toast.LENGTH_SHORT).show();
                postViewModel.resetPostCreated();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        btnAddGroup.setOnClickListener(v -> showAddGroupSheet());
        btnPublish.setOnClickListener(v -> publishPost());

        postViewModel.loadOfficialTags();

        postViewModel.getOfficialTags().observe(getViewLifecycleOwner(), tags -> {
            if (tags != null) {
                chipGroup.removeAllViews();
                for (String tag : tags) {
                    Chip chip = new Chip(getContext());
                    chip.setText(tag);
                    chip.setCheckable(true);
                    chip.setClickable(true);
                    chipGroup.addView(chip);
                }
            }
        });

        postViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnPublish.setEnabled(!isLoading);
        });

        postViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                inputLocalisation.setError(error);
            }
        });
    }

    private void startVoiceRecognition(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...");
        try {
            launcher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "La reconnaissance vocale n'est pas disponible sur cet appareil", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker(){
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.FRANCE, "%02d/%02d/%d", dayOfMonth, month + 1, year);
            inputDate.setText(date);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupGroupsRecyclerView() {
        recyclerGroups.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void publishPost() {
        String description = inputDescription.getText().toString();
        String indication = inputIndication.getText().toString();
        String address = inputLocalisation.getText().toString();

        com.example.travelshare.model.User firestoreUser = userViewModel.getCurrentUser().getValue();

        if (firestoreUser == null) {
            Toast.makeText(getContext(), "Données utilisateur non chargées, réessayez...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectecImageUri == null || description.isEmpty()) {
            Toast.makeText(getContext(), "Image et description requises", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> selectedGroupIds = new ArrayList<>();
        for (Group g : selectedGroupAdapter.getSelectedGroups()) {
            selectedGroupIds.add(g.getId());
        }

        Post newPost = new Post(
                firestoreUser.getId(),
                firestoreUser.getPseudo(),
                description,
                null,
                indication,
                "",
                firestoreUser.getProfilePictureUrl(),
                groupRadio.getCheckedRadioButtonId() == R.id.publish_radio_public,
                selectedGroupIds,
                getSelectedTags()
        );

        postViewModel.publishPost(newPost, selectecImageUri, address, requireContext());
    }

    private void showAddGroupSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.item_add_member, null);
        bottomSheetDialog.setContentView(sheetView);

        RecyclerView recyclerResults = sheetView.findViewById(R.id.item_group_results_recycler);
        EditText searchInput = sheetView.findViewById(R.id.item_group_search_input);

        GroupAdapter searchAdapter = new GroupAdapter(group -> {
            selectedGroupAdapter.addGroup(group);
            bottomSheetDialog.dismiss();
        });

        recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerResults.setAdapter(searchAdapter);

        groupViewModel.getAllGroups().observe(getViewLifecycleOwner(), groups -> {
            searchAdapter.setGroups(groups);
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        bottomSheetDialog.show();
    }

    private List<String> getSelectedTags() {
        List<String> selectedTags = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedTags.add(chip.getText().toString());
            }
        }
        return selectedTags;
    }
}
