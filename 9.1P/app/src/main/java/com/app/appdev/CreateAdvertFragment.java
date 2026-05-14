package com.app.appdev;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.app.appdev.databinding.FragmentCreateAdvertBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateAdvertFragment extends Fragment {

    private FragmentCreateAdvertBinding binding;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private FusedLocationProviderClient fusedLocationClient;
    private LostFoundItem existingItem;
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private ArrayAdapter<String> predictionsAdapter;
    private List<com.google.android.libraries.places.api.model.AutocompletePrediction> predictions;
    private final android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    private boolean isProgrammaticChange = false;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.imageViewPreview.setImageURI(selectedImageUri);
                    binding.imageViewPreview.setVisibility(View.VISIBLE);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateAdvertBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (getArguments() != null) {
            existingItem = (LostFoundItem) getArguments().getSerializable("item");
        }

        // Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), Config.GOOGLE_MAPS_API_KEY);
        }

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Places Client
        placesClient = Places.createClient(getContext());
        sessionToken = AutocompleteSessionToken.newInstance();
        
        // Set up predictions adapter for autocomplete suggestions
        predictionsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        
        // Set up the autocomplete search EditText with TextWatcher
        binding.editTextLocationSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isProgrammaticChange) return;

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                if (s.length() > 2) { // Only search if 3+ characters entered
                    searchRunnable = () -> fetchPlacePredictions(s.toString());
                    searchHandler.postDelayed(searchRunnable, 500); // 500ms debounce
                } else if (TextUtils.isEmpty(s)) {
                    predictionsAdapter.clear();
                    binding.listViewPredictions.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Set up the predictions ListView
        binding.listViewPredictions.setAdapter(predictionsAdapter);
        binding.listViewPredictions.setOnItemClickListener((parent, itemView, position, id) -> {
            if (predictions != null && position < predictions.size()) {
                fetchPlaceDetails(predictions.get(position));
            }
        });

        // Set up Category Spinner
        String[] categories = {"Electronics", "Pets", "Wallets", "Clothing", "Keys", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);

        // Date Picker
        binding.editTextDate.setOnClickListener(v -> showDatePickerDialog());

        if (existingItem != null) {
            setupEditMode();
        }

        binding.buttonUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        binding.buttonGetCurrentLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        binding.buttonSave.setOnClickListener(v -> {
            String type = binding.radioLost.isChecked() ? "Lost" : "Found";
            String name = binding.editTextName.getText().toString();
            String phone = binding.editTextPhone.getText().toString();
            String description = binding.editTextDescription.getText().toString();
            String date = binding.editTextDate.getText().toString();
            String location = binding.editTextLocation.getText().toString();
            String category = binding.spinnerCategory.getSelectedItem().toString();
            String imageUri = selectedImageUri != null ? selectedImageUri.toString() : 
                              (existingItem != null ? existingItem.getImageUri() : "");

            if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields and select a location", Toast.LENGTH_SHORT).show();
                return;
            }

            LostFoundItem itemToSave = new LostFoundItem(
                existingItem != null ? existingItem.getId() : 0,
                type, name, phone, description, date, location,
                currentLatitude, currentLongitude, category, imageUri
            );

            long result;
            if (existingItem != null) {
                result = ((MainActivity) getActivity()).updateItem(itemToSave);
            } else {
                result = ((MainActivity) getActivity()).insertItem(itemToSave);
            }

            if (result != -1) {
                Toast.makeText(getContext(), existingItem != null ? "Advert Updated!" : "Advert Saved!", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            } else {
                Toast.makeText(getContext(), "Error saving advert", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPlacePredictions(String input) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(input)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            predictions = response.getAutocompletePredictions();
            predictionsAdapter.clear();
            if (predictions.isEmpty()) {
                binding.listViewPredictions.setVisibility(View.GONE);
                return;
            }
            
            for (com.google.android.libraries.places.api.model.AutocompletePrediction prediction : predictions) {
                predictionsAdapter.add(prediction.getFullText(null).toString());
            }
            predictionsAdapter.notifyDataSetChanged();
            binding.listViewPredictions.setVisibility(View.VISIBLE);
        }).addOnFailureListener((exception) -> {
            Log.e("Places", "Prediction error: " + exception.getMessage());
            if (exception instanceof com.google.android.gms.common.api.ApiException) {
                com.google.android.gms.common.api.ApiException apiException = (com.google.android.gms.common.api.ApiException) exception;
                Log.e("Places", "Status code: " + apiException.getStatusCode());
            }
        });
    }

    private void fetchPlaceDetails(com.google.android.libraries.places.api.model.AutocompletePrediction prediction) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID, 
                Place.Field.NAME, 
                Place.Field.LAT_LNG, 
                Place.Field.ADDRESS
        );
        
        FetchPlaceRequest request = FetchPlaceRequest.builder(prediction.getPlaceId(), placeFields)
                .setSessionToken(sessionToken)
                .build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            
            // Set values
            String placeName = place.getName();
            
            isProgrammaticChange = true;
            binding.editTextLocation.setText(placeName);
            binding.editTextLocationSearch.setText(placeName);
            isProgrammaticChange = false;
            
            if (place.getLatLng() != null) {
                currentLatitude = place.getLatLng().latitude;
                currentLongitude = place.getLatLng().longitude;
            }
            
            // UI cleanup
            binding.listViewPredictions.setVisibility(View.GONE);
            predictionsAdapter.clear();
            hideKeyboard();
            
            // Refresh session token for next session
            sessionToken = AutocompleteSessionToken.newInstance();
            
            Toast.makeText(getContext(), "Location selected: " + placeName, Toast.LENGTH_SHORT).show();
        }).addOnFailureListener((exception) -> {
            Log.e("Places", "Place details error: " + exception.getMessage());
            Toast.makeText(getContext(), "Failed to get location details", Toast.LENGTH_SHORT).show();
        });
    }

    private void hideKeyboard() {
        View view = this.getView();
        if (view != null && getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void setupEditMode() {
        binding.buttonSave.setText("UPDATE ADVERT");
        if (existingItem.getType().equals("Lost")) {
            binding.radioLost.setChecked(true);
        } else {
            binding.radioFound.setChecked(true);
        }
        binding.editTextName.setText(existingItem.getName());
        binding.editTextPhone.setText(existingItem.getPhone());
        binding.editTextDescription.setText(existingItem.getDescription());
        binding.editTextDate.setText(existingItem.getDate());
        binding.editTextLocation.setText(existingItem.getLocation());
        binding.editTextLocationSearch.setText(existingItem.getLocation());

        currentLatitude = existingItem.getLatitude();
        currentLongitude = existingItem.getLongitude();
        
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) binding.spinnerCategory.getAdapter();
        int position = adapter.getPosition(existingItem.getCategory());
        binding.spinnerCategory.setSelection(position);

        if (existingItem.getImageUri() != null && !existingItem.getImageUri().isEmpty()) {
            binding.imageViewPreview.setImageURI(Uri.parse(existingItem.getImageUri()));
            binding.imageViewPreview.setVisibility(View.VISIBLE);
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), location -> {
                        if (location != null) {
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            String locName = String.format(Locale.getDefault(), "Current GPS (%.4f, %.4f)", currentLatitude, currentLongitude);
                            binding.editTextLocation.setText(locName);
                            binding.editTextLocationSearch.setText(locName);
                        } else {
                            Toast.makeText(getContext(), "GPS location not found", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (SecurityException e) {
            Log.e("Location", "Permission Error: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            binding.editTextDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setCurrentDate(Calendar calendar) {
        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        binding.editTextDate.setText(date);
    }
}
