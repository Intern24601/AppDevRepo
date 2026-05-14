package com.example.a71p;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.a71p.databinding.FragmentMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
    private List<Marker> allMarkers = new ArrayList<>();
    private List<LostFoundItem> itemList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.buttonFilterRadius.setOnClickListener(v -> {
            String radiusStr = binding.editTextRadius.getText().toString();
            if (radiusStr.isEmpty()) {
                showAllMarkers();
            } else {
                try {
                    double radiusKm = Double.parseDouble(radiusStr);
                    filterMarkersByRadius(radiusKm);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid radius", Toast.LENGTH_SHORT).show();
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
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10));
                }
            });
        }
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
