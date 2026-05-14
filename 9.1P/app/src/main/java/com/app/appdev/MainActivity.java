package com.app.appdev;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.app.appdev.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

/**
 * MainActivity serves as the central orchestration hub for the Lost and Found application.
 * 
 * It manages:
 * 1. Navigation: Links the toolbar with the Jetpack Navigation component and manages fragment transitions.
 * 2. Data Persistence: Provides centralized CRUD operations for the SQLite database through dbHelper.
 * 3. UI Framework: Handles edge-to-edge layout, window insets, and view binding initialization.
 * 
 * DESIGN PATTERN: Centralized Data Layer
 * By centralizing database logic here, fragments remain decoupled from the data layer. 
 * Fragments communicate with this activity to perform "Master Record" operations, ensuring 
 * consistency across the app.
 */
public class MainActivity extends AppCompatActivity {

    // UI COMPONENTS
    // binding gives direct access to activity_main.xml views without the overhead of findViewById
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    // DATA LAYER
    // dbHelper manages the SQLite database connection and underlying schema
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Step 1: Enable modern Edge-to-Edge display (translucent bars)
        EdgeToEdge.enable(this);

        // Step 2: Initialize the SQLite database connection
        dbHelper = new DatabaseHelper(this);

        // Step 3: Setup View Binding for efficient UI access
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Step 4: Handle Window Insets (padding) to prevent UI overlap with status/nav bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Step 5: Configure the Toolbar as the primary Action Bar
        setSupportActionBar(binding.toolbar);

        // Step 6: Setup the Jetpack Navigation system
        setupNavigation();
    }

    /**
     * setupNavigation()
     * Links the Navigation component with the Toolbar.
     * This ensures the title in the Action Bar updates automatically as the user navigates 
     * between fragments defined in nav_graph.xml.
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            // Build configuration using the root of the navigation graph
            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            
            // Link Navigation controller with Action Bar for automatic back buttons and title updates
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    //
    // CORE OPERATIONS: Centralized methods for Fragments to interact with the database
    //
    // These methods provide a high-level API for data management. By having fragments call 
    // these methods, we avoid duplicate database code and maintain a single source of truth.
    //

    /**
     * OPERATION: Create
     * Persists a new LostFoundItem into the SQLite database.
     * @param item The item object containing all user input details.
     * @return The ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insertItem(LostFoundItem item) {
        return dbHelper.insertItem(
                item.getType(),
                item.getName(),
                item.getPhone(),
                item.getDescription(),
                item.getDate(),
                item.getLocation(),
                item.getLatitude(),
                item.getLongitude(),
                item.getCategory(),
                item.getImageUri()
        );
    }

    /**
     * OPERATION: Read (All)
     * Retrieves all adverts currently stored in the database.
     * @return A Cursor pointing to the result set.
     */
    public Cursor getAllItems() {
        return dbHelper.getAllItems();
    }

    /**
     * OPERATION: Read (By Category)
     * Filters adverts based on the selected category string (e.g., "Electronics").
     * @param category The string name of the category to filter by.
     * @return A Cursor containing only items matching that category.
     */
    public Cursor getItemsByCategory(String category) {
        return dbHelper.getItemsByCategory(category);
    }

    /**
     * OPERATION: Delete
     * Removes a specific advert from the database using its unique ID.
     * @param id The primary key ID of the item to delete.
     */
    public void deleteItem(int id) {
        dbHelper.deleteItem(id);
    }

    /**
     * OPERATION: Update
     * Updates an existing LostFoundItem record with new details.
     * @param item The item object containing the updated fields and the original ID.
     * @return The number of rows affected (should be 1).
     */
    public int updateItem(LostFoundItem item) {
        return dbHelper.updateItem(
                item.getId(),
                item.getType(),
                item.getName(),
                item.getPhone(),
                item.getDescription(),
                item.getDate(),
                item.getLocation(),
                item.getLatitude(),
                item.getLongitude(),
                item.getCategory(),
                item.getImageUri()
        );
    }

    /**
     * NAVIGATION: Show on Map
     * Triggers a navigation transition to the MapsFragment.
     * This allows any fragment to request a switch to the map view.
     */
    public void showOnMap() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            navHostFragment.getNavController().navigate(R.id.action_global_MapsFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main.xml; this adds items like "Settings" to the action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle Action Bar item clicks (like the Settings menu)
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handles the "Up" button (back arrow) in the Toolbar.
        // It delegates the back navigation to the NavController.
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}