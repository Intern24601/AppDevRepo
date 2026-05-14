package com.app.appdev;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.appdev.databinding.FragmentMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapsBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final List<Marker> allMarkers = new ArrayList<>();
    private final List<LostFoundItem> itemList = new ArrayList<>();
    private double pendingRadiusKm = -1;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (mMap != null) {
                        enableMyLocation();
                    }
                    if (pendingRadiusKm > 0) {
                        filterMarkersByRadius(pendingRadiusKm);
                        pendingRadiusKm = -1;
                    }
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() != null) {
            // Explicitly initialize the Maps SDK
            MapsInitializer.initialize(getContext(), MapsInitializer.Renderer.LATEST, renderer -> 
                Log.d("MapsFragment", "Maps SDK initialized with renderer: " + renderer.name())
            );
        }

        if (getActivity() != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        }

        // We manually add the SupportMapFragment to the map_container.
        // This is often more reliable than using android:name in XML when dealing with multiple transitions.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag("MAP_FRAGMENT");
        
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.map_container, mapFragment, "MAP_FRAGMENT")
                    .commit();
        }
        
        mapFragment.getMapAsync(this);

        // Set up NumberPicker (Spinner Selector)
        binding.numberPickerRadius.setMinValue(0);
        binding.numberPickerRadius.setMaxValue(100);
        binding.numberPickerRadius.setValue(0);
        
        // Custom formatting: 0 means "All"
        binding.numberPickerRadius.setFormatter(value -> {
            if (value == 0) return "All";
            return value + " km";
        });

        binding.buttonFilterRadius.setOnClickListener(v -> {
            int newVal = binding.numberPickerRadius.getValue();
            if (newVal == 0) {
                showAllMarkers();
            } else {
                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    filterMarkersByRadius(newVal);
                } else {
                    pendingRadiusKm = newVal;
                    requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadItemsFromDatabase();
        displayAllItemsOnMap();

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10));
            }
        });
    }

    private void loadItemsFromDatabase() {
        itemList.clear();
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            Cursor cursor = activity.getAllItems();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
                    double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE));
                    double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY));
                    String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI));

                    itemList.add(new LostFoundItem(id, type, name, phone, description, date, location, lat, lng, category, imageUri));
                }
                cursor.close();
            }
        }
    }

    private void displayAllItemsOnMap() {
        mMap.clear();
        allMarkers.clear();
        for (LostFoundItem item : itemList) {
            LatLng pos = new LatLng(item.getLatitude(), item.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(item.getType() + ": " + item.getName())
                    .snippet(item.getDescription()));
            if (marker != null) {
                marker.setTag(item);
                allMarkers.add(marker);
            }
        }
    }

    private void showAllMarkers() {
        for (Marker marker : allMarkers) {
            marker.setVisible(true);
        }
    }

    private void filterMarkersByRadius(double radiusKm) {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission required for radius search", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(userLocation -> {
            if (userLocation == null) {
                Toast.makeText(getContext(), "Current location not available", Toast.LENGTH_SHORT).show();
                return;
            }

            for (Marker marker : allMarkers) {
                LostFoundItem item = (LostFoundItem) marker.getTag();
                if (item != null) {
                    float[] results = new float[1];
                    Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                            item.getLatitude(), item.getLongitude(), results);
                    float distanceInMeters = results[0];
                    marker.setVisible(distanceInMeters <= radiusKm * 1000);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
