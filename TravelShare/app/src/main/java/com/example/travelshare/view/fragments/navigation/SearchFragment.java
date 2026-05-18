package com.example.travelshare.view.fragments.navigation;

import static com.example.travelshare.utils.NavigationUtils.showPostMenu;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelshare.R;
import com.example.travelshare.adapter.PostAdapter;
import com.example.travelshare.model.Post;
import com.example.travelshare.viewmodel.AuthViewModel;
import com.example.travelshare.viewmodel.PostViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private TextInputEditText searchInput;
    private ImageButton btnMap, btnMic;
    private ChipGroup chipGroupTags;
    private RecyclerView recyclerResults;
    private MapView searchMapView;
    private MyLocationNewOverlay myLocationOverlay;

    private PostViewModel postViewModel;
    private AuthViewModel authViewModel;
    private PostAdapter postAdapter;

    private List<Post> loadedPosts = new ArrayList<>();
    private boolean isMapViewVisible = false;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String lastSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        initViews(view);
        setupRecyclerView(view);
        setupMapView();
        setupFilters();
        observeData();

        postViewModel.loadFeed();
        postViewModel.loadOfficialTags();
    }

    private void initViews(View view) {
        searchInput = view.findViewById(R.id.search_text_search);
        btnMap = view.findViewById(R.id.search_btn_map);
        btnMic = view.findViewById(R.id.search_btn_mic);
        chipGroupTags = view.findViewById(R.id.search_chip_group);
        recyclerResults = view.findViewById(R.id.search_recycler_result);
        searchMapView = view.findViewById(R.id.search_map_view);

        btnMap.setOnClickListener(v -> toggleMapView());
        btnMic.setOnClickListener(v -> Toast.makeText(getContext(), "Recherche vocale bientôt disponible", Toast.LENGTH_SHORT).show());
    }

    private void setupRecyclerView(View view) {
        FirebaseUser currentUser = authViewModel.getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : "";

        postAdapter = new PostAdapter(currentUserId.isEmpty() ? null : currentUserId, new PostAdapter.OnPostClickListener() {
            @Override public void onPostClick(Post post) { navigateToPostDetail(post); }
            @Override public void onProfileClick(String userId) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", userId);
                Navigation.findNavController(view).navigate(R.id.action_global_to_profileFragment, bundle);
            }
            @Override public void onMoreClick(View v, Post post) { showPostMenu(requireContext(), view, post); }
            @Override public void onLikeClick(Post post) {
                if (currentUserId.isEmpty()) Toast.makeText(getContext(), "Veuillez vous connecter pour liker", Toast.LENGTH_SHORT).show();
                else postViewModel.toggleLike(post, currentUserId);
            }
            @Override public void onCommentClick(Post post) { onPostClick(post); }
        });

        recyclerResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerResults.setAdapter(postAdapter);
    }

    private void setupMapView() {
        searchMapView.setMultiTouchControls(true);
        searchMapView.getController().setZoom(6.0);
        searchMapView.getController().setCenter(new GeoPoint(46.603354, 1.888334));

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), searchMapView);
        myLocationOverlay.enableMyLocation();
        searchMapView.getOverlays().add(myLocationOverlay);
    }

    private void setupFilters() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> applyFilters();
                searchHandler.postDelayed(searchRunnable, 500);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroupTags.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null) {
                    String selectedTag = chip.getText().toString();
                    if (selectedTag.equals("Tout")) postViewModel.loadFeed();
                    else postViewModel.loadPostsByTag(selectedTag);
                }
            }
        });
    }

    private void observeData() {
        postViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                loadedPosts = posts;
                applyFilters();
            }
        });

        postViewModel.getOfficialTags().observe(getViewLifecycleOwner(), tags -> {
            if (tags != null) {
                chipGroupTags.removeAllViews();
                addChipToGroup("Tout", true);
                for (String tag : tags) addChipToGroup(tag, false);
            }
        });
    }

    private void addChipToGroup(String text, boolean isChecked) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setChecked(isChecked);
        chipGroupTags.addView(chip);
    }

    private void applyFilters() {
        final String constraint = (searchInput.getText() != null) ? searchInput.getText().toString().trim().toLowerCase() : "";
        final Context context = getContext();
        if (context == null) return;
        
        lastSearchQuery = constraint;

        if (constraint.isEmpty()) {
            postAdapter.setPosts(new ArrayList<>(loadedPosts));
            if (isMapViewVisible) updateMapMarkers(loadedPosts, null);
            return;
        }

        new Thread(() -> {
            List<Post> filteredList = new ArrayList<>();
            GeoPoint searchCenter = null;
            String searchLocality = null;

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(constraint, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    searchCenter = new GeoPoint(addr.getLatitude(), addr.getLongitude());
                    searchLocality = addr.getLocality() != null ? addr.getLocality().toLowerCase() : null;
                }
            } catch (IOException ignored) {}

            final GeoPoint finalCenter = searchCenter;
            final String finalLocality = searchLocality;

            for (Post post : loadedPosts) {
                boolean matches = false;
                if (post.getDescription() != null && post.getDescription().toLowerCase().contains(constraint)) matches = true;
                else if (post.getAuthorName() != null && post.getAuthorName().toLowerCase().contains(constraint)) matches = true;
                else if (post.getIndication() != null && post.getIndication().toLowerCase().contains(constraint)) matches = true;
                else if (post.getTags() != null) {
                    for (String t : post.getTags()) { if (t.toLowerCase().contains(constraint)) { matches = true; break; } }
                }
                if (!matches && finalLocality != null && post.getIndication() != null) {
                    if (post.getIndication().toLowerCase().contains(finalLocality)) matches = true;
                }
                if (!matches && finalCenter != null && post.getLocation() != null) {
                    float[] dist = new float[1];
                    Location.distanceBetween(finalCenter.getLatitude(), finalCenter.getLongitude(),
                            post.getLocation().getLatitude(), post.getLocation().getLongitude(), dist);
                    if (dist[0] < 80000) matches = true; 
                }
                if (matches) filteredList.add(post);
            }

            if (!constraint.equals(lastSearchQuery)) return;

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded()) return;
                postAdapter.setPosts(filteredList);
                if (isMapViewVisible) updateMapMarkers(filteredList, finalCenter);
            });
        }).start();
    }

    private void updateMapMarkers(List<Post> posts, GeoPoint searchCenter) {
        if (searchMapView == null) return;
        
        searchMapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);
        List<GeoPoint> pointsToZoom = new ArrayList<>();

        if (searchCenter != null) {
            pointsToZoom.add(searchCenter);
            Marker searchMarker = new Marker(searchMapView);
            searchMarker.setPosition(searchCenter);
            searchMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            searchMarker.setIcon(ResourcesCompat.getDrawable(getResources(), android.R.drawable.ic_menu_mylocation, null));
            searchMarker.setTitle("Recherche : " + searchInput.getText().toString());
            searchMapView.getOverlays().add(searchMarker);
        }

        int count = 0;
        for (Post post : posts) {
            if (post.getLocation() != null) {
                GeoPoint gp = new GeoPoint(post.getLocation().getLatitude(), post.getLocation().getLongitude());
                pointsToZoom.add(gp);

                Marker marker = new Marker(searchMapView);
                marker.setPosition(gp);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                marker.setTitle(post.getAuthorName());
                String snippet = (post.getIndication() != null && !post.getIndication().isEmpty()) ? post.getIndication() : post.getDescription();
                marker.setSnippet(snippet);
                marker.setOnMarkerClickListener((m, mapView) -> {
                    navigateToPostDetail(post);
                    return true;
                });
                searchMapView.getOverlays().add(marker);
                if (++count > 100) break;
            }
        }

        if (!pointsToZoom.isEmpty()) {
            searchMapView.post(() -> {
                if (!isAdded()) return;
                try {
                    if (pointsToZoom.size() == 1) {
                        searchMapView.getController().setZoom(11.0);
                        searchMapView.getController().animateTo(pointsToZoom.get(0));
                    } else {
                        BoundingBox box = BoundingBox.fromGeoPoints(pointsToZoom);
                        // Sécurité : si la zone est trop petite (points quasi identiques), on utilise un zoom fixe
                        if (box.getLatitudeSpan() < 0.01 && box.getLongitudeSpan() < 0.01) {
                            searchMapView.getController().setZoom(11.0);
                            searchMapView.getController().animateTo(new GeoPoint(box.getCenterLatitude(), box.getCenterLongitude()));
                        } else {
                            searchMapView.zoomToBoundingBox(box, true, 150);
                        }
                    }
                } catch (Exception ignored) {}
            });
        }
        searchMapView.invalidate();
    }

    private void toggleMapView() {
        isMapViewVisible = !isMapViewVisible;
        if (isMapViewVisible) {
            recyclerResults.setVisibility(View.GONE);
            searchMapView.setVisibility(View.VISIBLE);
            btnMap.setImageResource(android.R.drawable.ic_menu_sort_by_size);
            updateMapMarkers(postAdapter.getPosts(), null);
        } else {
            searchMapView.setVisibility(View.GONE);
            recyclerResults.setVisibility(View.VISIBLE);
            btnMap.setImageResource(R.drawable.adresse);
        }
    }

    private void navigateToPostDetail(Post post) {
        postViewModel.selectPost(post);
        Bundle bundle = new Bundle();
        bundle.putString("postId", post.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_global_to_postDetailFragment, bundle);
    }

    @Override public void onResume() {
        super.onResume();
        searchMapView.onResume();
        if (myLocationOverlay != null) myLocationOverlay.enableMyLocation();
    }

    @Override public void onPause() {
        super.onPause();
        searchMapView.onPause();
        if (myLocationOverlay != null) myLocationOverlay.disableMyLocation();
    }
}
