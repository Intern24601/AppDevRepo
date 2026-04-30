package com.example.a71p;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.material.snackbar.Snackbar;

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

import com.example.a71p.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

/**
 * MainActivity serves as the central orchestration hub for the Lost and Found application.
 * 
 * It manages:
 * 1. Navigation: Links the toolbar with the Jetpack Navigation component.
 * 2. Data Persistence: Provides centralized CRUD operations for the SQLite database.
 * 3. UI Framework: Handles edge-to-edge layout and view binding initialization.
 * 
 * By centralizing database logic here, fragments remain decoupled from the data layer
 * and can communicate with the "Master Record" through public OPERATION methods.
 */
public class MainActivity extends AppCompatActivity {

    // UI COMPONENTS
    // binding gives direct access to activity_main.xml views without findViewById
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;

    // DATA LAYER
    // dbHelper manages the SQLite database connection
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable modern Edge-to-Edge display
        EdgeToEdge.enable(this);

        // INITIALIZE DATA LAYER
        dbHelper = new DatabaseHelper(this);

        // SETUP VIEW BINDING
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // HANDLE WINDOW INSETS (Edge-to-Edge Padding)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // CONFIGURE TOOLBAR
        setSupportActionBar(binding.toolbar);

        // SETUP NAVIGATION SYSTEM
        setupNavigation();
    }

    /**
     * Links the Navigation component with the Toolbar.
     * This ensures the title in the Action Bar updates automatically as the user navigates.
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            
            // Build configuration using the root of the navigation graph
            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            
            // Link Navigation controller with Action Bar
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    //
    // CORE OPERATIONS: Centralized methods for Fragments to interact with the database
    //
    // These methods provide a high-level API for data management, keeping Fragment
    // code clean and focused on UI logic.
    //

    /**
     * OPERATION: Create
     * Persists a new LostFoundItem into the database.
     * Returns the ID of the newly inserted row, or -1 if an error occurred.
     */
    public long insertItem(LostFoundItem item) {
        return dbHelper.insertItem(
                item.getType(),
                item.getName(),
                item.getPhone(),
                item.getDescription(),
                item.getDate(),
                item.getLocation(),
                item.getCategory(),
                item.getImageUri()
        );
    }

    /**
     * OPERATION: Read (All)
     * Retrieves all adverts currently stored in the database, sorted by date (newest first).
     */
    public Cursor getAllItems() {
        return dbHelper.getAllItems();
    }

    /**
     * OPERATION: Read (By Category)
     * Filters adverts based on the selected category string.
     */
    public Cursor getItemsByCategory(String category) {
        return dbHelper.getItemsByCategory(category);
    }

    /**
     * OPERATION: Delete
     * Removes a specific advert from the database using its unique identifier.
     */
    public void deleteItem(int id) {
        dbHelper.deleteItem(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}