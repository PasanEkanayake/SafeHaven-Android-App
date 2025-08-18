package com.example.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.annotations.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Locations extends AppCompatActivity {

    private MapView mapView;
    private MapLibreMap mapboxMap;
    private RecyclerView recyclerView;
    private LocationsAdapter locationsAdapter;
    private DatabaseReference databaseRef;
    private List<LocationModel> locationsList = new ArrayList<>();

    private EditText searchEditText;
    private ImageView btnBack;
    private ChipGroup chipGroupCategories;
    private MaterialButtonToggleGroup toggleMapList;
    private FloatingActionButton fabLocate;

    private String currentCategory = "All";
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        org.maplibre.android.MapLibre.getInstance(this);
        setContentView(R.layout.activity_locations);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Locations.this, Home.class);
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        // Navigation handling
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_guides) {
                startActivity(new Intent(getApplicationContext(), SurvivalGuides.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_recover) {
                startActivity(new Intent(getApplicationContext(), Recover.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_contacts) {
                startActivity(new Intent(getApplicationContext(), Contacts.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        findViewById(R.id.settings).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), Settings.class));
            overridePendingTransition(0, 0);
        });

        // Initialize views
        mapView = findViewById(R.id.mapView);
        recyclerView = findViewById(R.id.locationsRecycler);
        searchEditText = findViewById(R.id.searchEditText);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        toggleMapList = findViewById(R.id.toggleMapList);
        fabLocate = findViewById(R.id.fabLocate);

        // Setup MapLibre
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapLibreMap mapboxMapInstance) {
                mapboxMap = mapboxMapInstance;
                mapboxMap.setStyle("https://demotiles.maplibre.org/style.json", style -> {
                    // Map ready -> load Firebase data
                    loadLocationsFromFirebase();
                });
            }
        });

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationsAdapter = new LocationsAdapter(new ArrayList<>());
        recyclerView.setAdapter(locationsAdapter);

        // Setup Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference("locations");

        // Search listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString();
                applyFilters();
            }
        });

        // Chip group listener
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentCategory = chip.getText().toString();
                applyFilters();
            }
        });

        // Map/List toggle
        toggleMapList.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (checkedId == R.id.btnMap && isChecked) {
                mapView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else if (checkedId == R.id.btnList && isChecked) {
                mapView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Floating button (recenter to first location)
        fabLocate.setOnClickListener(v -> {
            if (mapboxMap != null && !locationsList.isEmpty()) {
                LocationModel first = locationsList.get(0);
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(first.getLatitude(), first.getLongitude()), 12));
            }
        });
    }

    private void loadLocationsFromFirebase() {
        databaseRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locationsList.clear();
                if (mapboxMap != null) {
                    mapboxMap.clear(); // clear old markers
                }
                for (DataSnapshot locSnapshot : snapshot.getChildren()) {
                    LocationModel location = locSnapshot.getValue(LocationModel.class);
                    if (location != null) {
                        locationsList.add(location);
                    }
                }
                applyFilters(); // apply search + filter to update UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error if needed
            }
        });
    }

    private void applyFilters() {
        List<LocationModel> filtered = new ArrayList<>();
        if (mapboxMap != null) mapboxMap.clear();

        for (LocationModel loc : locationsList) {
            boolean matchesQuery = loc.getName().toLowerCase().contains(currentQuery.toLowerCase());
            boolean matchesCategory = currentCategory.equals("All") ||
                    loc.getType().equalsIgnoreCase(currentCategory);

            if (matchesQuery && matchesCategory) {
                filtered.add(loc);
                // Add marker to map
                if (mapboxMap != null) {
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                            .title(loc.getName())
                            .snippet(loc.getType()));
                }
            }
        }

        locationsAdapter.updateList(filtered);
    }

    // MapView lifecycle
    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}