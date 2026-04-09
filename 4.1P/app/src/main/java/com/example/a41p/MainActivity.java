package com.example.a41p;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.a41p.databinding.ActivityMainBinding;
import androidx.lifecycle.ViewModelProvider;
import com.example.a41p.data.Event;
import com.example.a41p.ui.EventViewModel;
import com.google.android.material.snackbar.Snackbar;
import androidx.lifecycle.LiveData;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // UI COMPONENTS
    // binding gives direct access to activity_main.xml views
    private ActivityMainBinding binding;

    // DATA LAYER
    // The ViewModel acts as a "master record" for event data across all screens
    private EventViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Shared ViewModel
        // when Fragments are inflated by the NavHostFragment.
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Setup View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Navigation
        setupNavigation();
    }

    /**
     * Links the Bottom Navigation UI with the Navigation Graph.
     * This automatically handles fragment switching when a menu item is clicked.
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // Automatically syncs the bottom nav selection with the current fragment
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        }
    }

    //
    // CORE OPERATIONS: Called by Fragments to interact with the database

    /**
     * OPERATION: Create or Update
     * Determines whether to insert a new record or update an existing one based on the ID.
     */
    public void handleSaveEvent(Event event) {
        if (event.getId() == 0) {
            viewModel.insert(event); // New event
        } else {
            viewModel.update(event); // Existing event
        }
    }

    /**
     * OPERATION: Delete
     * Removes an event and provides option allowing the user to undo the action.
     */
    public void handleDeleteEvent(Event event) {
        viewModel.delete(event);
        Snackbar.make(binding.getRoot(), "Event removed from list", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> viewModel.insert(event))
                .show();
    }

    /**
     * OPERATION: Read (Upcoming)
     * Returns a LiveData stream of events that automatically updates the UI when data changes.
     * Filters for events that are scheduled for the current time or later.
     */
    public LiveData<List<Event>> getEvents() {
        return viewModel.getUpcomingEvents(System.currentTimeMillis());
    }

    /**
     * OPERATION: Read (Single)
     * Used by the AddEditEventFragment to populate the form when editing an existing event.
     */
    public LiveData<Event> getEventById(long id) {
        return viewModel.getEventById(id);
    }
}
