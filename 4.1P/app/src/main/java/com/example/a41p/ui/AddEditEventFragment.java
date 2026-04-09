package com.example.a41p.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.a41p.MainActivity;
import com.example.a41p.data.Event;
import com.example.a41p.databinding.FragmentAddEditEventBinding;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment for adding a new event or editing an existing one.
 * It now delegates the save and fetch operations to the MainActivity.
 */
public class AddEditEventFragment extends Fragment {

    private FragmentAddEditEventBinding binding;
    private final Calendar selectedCalendar = Calendar.getInstance();
    private long eventId = -1L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEditEventBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get a reference to the MainActivity to use its centralized methods
        MainActivity activity = (MainActivity) requireActivity();

        if (getArguments() != null) {
            eventId = getArguments().getLong("eventId", -1L);
        }

        if (eventId != -1L) {
            // Use MainActivity to fetch event details instead of a local ViewModel
            activity.getEventById(eventId).observe(getViewLifecycleOwner(), event -> {
                if (event != null) {
                    binding.etTitle.setText(event.getTitle());
                    binding.etCategory.setText(event.getCategory());
                    binding.etLocation.setText(event.getLocation());
                    selectedCalendar.setTimeInMillis(event.getDateTime());
                    updateDateDisplay();
                }
            });
        }

        binding.btnPickDate.setOnClickListener(v -> showDateTimePicker());
        binding.btnSave.setOnClickListener(v -> saveEvent());
    }

    /**
     * Shows a DatePickerDialog followed by a TimePickerDialog to select event timing.
     */
    private void showDateTimePicker() {
        Calendar currentCalendar = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(
                            requireContext(),
                            (view1, hourOfDay, minute) -> {
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedCalendar.set(Calendar.MINUTE, minute);
                                updateDateDisplay();
                            },
                            currentCalendar.get(Calendar.HOUR_OF_DAY),
                            currentCalendar.get(Calendar.MINUTE),
                            true
                    ).show();
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /**
     * Updates the UI to show the currently selected date and time.
     */
    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        binding.tvSelectedDate.setText("Selected Date: " + sdf.format(selectedCalendar.getTime()));
    }

    /**
     * Validates input fields and delegates the save operation to MainActivity.
     */
    private void saveEvent() {
        String title = binding.etTitle.getText().toString().trim();
        String category = binding.etCategory.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        long dateTime = selectedCalendar.getTimeInMillis();

        if (title.isEmpty()) {
            binding.tilTitle.setError("Title is required");
            return;
        }

        if (binding.tvSelectedDate.getText().toString().isEmpty()) {
            Toast.makeText(requireContext(), "Please pick a date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logic Validation: Prevent picking a date in the past for new events
        if (eventId == -1L && dateTime < System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "Cannot schedule events in the past", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(title, category, location, dateTime);
        MainActivity activity = (MainActivity) requireActivity();

        if (eventId != -1L) {
            event.setId(eventId);
            activity.handleSaveEvent(event); // Delegate to MainActivity
            Toast.makeText(requireContext(), "Event updated", Toast.LENGTH_SHORT).show();
        } else {
            activity.handleSaveEvent(event); // Delegate to MainActivity
            Toast.makeText(requireContext(), "Event saved", Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(requireView()).popBackStack();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
