package com.example.a71p;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.example.a71p.databinding.FragmentCreateAdvertBinding;
import java.util.Calendar;
import java.util.Locale;

public class CreateAdvertFragment extends Fragment {

    private FragmentCreateAdvertBinding binding;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateAdvertBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up Category Spinner
        String[] categories = {"Electronics", "Pets", "Wallets", "Clothing", "Keys", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);

        // Auto-select current date in ISO format (YYYY-MM-DD)
        Calendar calendar = Calendar.getInstance();
        setCurrentDate(calendar);

        // Set up Date Picker Dialog
        binding.editTextDate.setOnClickListener(v -> showDatePickerDialog());

        binding.buttonUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        binding.buttonSave.setOnClickListener(v -> {
            String type = binding.radioLost.isChecked() ? "Lost" : "Found";
            String name = binding.editTextName.getText().toString();
            String phone = binding.editTextPhone.getText().toString();
            String description = binding.editTextDescription.getText().toString();
            String date = binding.editTextDate.getText().toString();
            String location = binding.editTextLocation.getText().toString();
            String category = binding.spinnerCategory.getSelectedItem().toString();
            String imageUri = selectedImageUri != null ? selectedImageUri.toString() : "";

            if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Delegate save operation to MainActivity
            LostFoundItem newItem = new LostFoundItem(0, type, name, phone, description, date, location, category, imageUri);
            long id = ((MainActivity) getActivity()).insertItem(newItem);

            if (id != -1) {
                Toast.makeText(getContext(), "Advert Saved!", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
            } else {
                Toast.makeText(getContext(), "Error saving advert", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Shows a DatePickerDialog and updates the date EditText with the selected date in ISO format.
     */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, monthOfYear, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, monthOfYear, dayOfMonth);
            setCurrentDate(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Formats the given calendar to ISO 8601 (YYYY-MM-DD) and sets it to the date EditText.
     */
    private void setCurrentDate(Calendar calendar) {
        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        binding.editTextDate.setText(date);
    }
}
