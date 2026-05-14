package com.app.appdev;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.app.appdev.databinding.FragmentPickLocationBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PickLocationFragment extends Fragment implements OnMapReadyCallback {

    private FragmentPickLocationBinding binding;
    private GoogleMap mMap;
    private Marker currentMarker;
    private LatLng selectedLatLng;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPickLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        binding.buttonConfirmLocation.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Bundle result = new Bundle();
                result.putDouble("lat", selectedLatLng.latitude);
                result.putDouble("lng", selectedLatLng.longitude);
                getParentFragmentManager().setFragmentResult("locationKey", result);
                NavHostFragment.findNavController(this).popBackStack();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Default to a central point (or user's last known location)
        LatLng defaultPos = new LatLng(-37.8136, 144.9631); // Melbourne default
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultPos, 10));

        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            if (currentMarker != null) {
                currentMarker.remove();
            }
            currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
