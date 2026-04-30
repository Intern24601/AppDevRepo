# Merged Java Code for 7.1P

## CreateAdvertFragment.java
```java
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
```

---

## DatabaseHelper.java
```java
package com.example.a71p;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lost_found.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ITEMS = "items";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TYPE = "type"; // "Lost" or "Found"
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_IMAGE_URI = "image_uri";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_PHONE + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_IMAGE_URI + " TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public long insertItem(String type, String name, String phone, String description, String date, String location, String category, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_IMAGE_URI, imageUri);

        return db.insert(TABLE_ITEMS, null, values);
    }

    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ITEMS, null, null, null, null, null, COLUMN_DATE + " DESC");
    }

    public Cursor getItemsByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ITEMS, null, COLUMN_CATEGORY + "=?", new String[]{category}, null, null, COLUMN_DATE + " DESC");
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ITEMS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
```

---

## HomeFragment.java
```java
package com.example.a71p;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.a71p.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonCreateAdvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_CreateAdvertFragment);
            }
        });

        binding.buttonShowItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(HomeFragment.this)
                        .navigate(R.id.action_HomeFragment_to_ItemListFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

---

## ItemListFragment.java
```java
package com.example.a71p;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a71p.databinding.FragmentItemListBinding;
import java.util.ArrayList;
import java.util.List;

public class ItemListFragment extends Fragment {

    private FragmentItemListBinding binding;
    private DatabaseHelper dbHelper;
    private ItemAdapter adapter;
    private List<LostFoundItem> itemList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentItemListBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewItems.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(itemList, item -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("item", item);
            NavHostFragment.findNavController(ItemListFragment.this)
                    .navigate(R.id.action_ItemListFragment_to_RemoveItemFragment, bundle);
        });
        binding.recyclerViewItems.setAdapter(adapter);

        // Set up Filter Spinner
        String[] categories = {"All", "Electronics", "Pets", "Wallets", "Clothing", "Keys", "Other"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(spinnerAdapter);

        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = categories[position];
                loadItems(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void filterList(String text) {
        List<LostFoundItem> filteredList = new ArrayList<>();
        for (LostFoundItem item : itemList) {
            if (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                item.getDescription().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.updateList(filteredList);
    }

    private void loadItems(String category) {
        itemList.clear();
        Cursor cursor;
        
        // Delegate read operations to MainActivity
        MainActivity mainActivity = (MainActivity) getActivity();
        if (category.equals("All")) {
            cursor = mainActivity.getAllItems();
        } else {
            cursor = mainActivity.getItemsByCategory(category);
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                LostFoundItem item = new LostFoundItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI))
                );
                itemList.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Inner class for Adapter
    private static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
        private List<LostFoundItem> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(LostFoundItem item);
        }

        ItemAdapter(List<LostFoundItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        public void updateList(List<LostFoundItem> newList) {
            this.items = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LostFoundItem item = items.get(position);
            holder.nameText.setText(item.getType() + ": " + item.getName());
            holder.dateText.setText("Date: " + item.getDate());
            holder.locationText.setText("Location: " + item.getLocation());

            if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
                try {
                    holder.itemImage.setImageURI(Uri.parse(item.getImageUri()));
                } catch (Exception e) {
                    holder.itemImage.setImageResource(android.R.drawable.ic_menu_report_image);
                }
            } else {
                holder.itemImage.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, dateText, locationText;
            ImageView itemImage;
            ViewHolder(View view) {
                super(view);
                nameText = view.findViewById(R.id.textView_item_name);
                dateText = view.findViewById(R.id.textView_item_date);
                locationText = view.findViewById(R.id.textView_item_location);
                itemImage = view.findViewById(R.id.imageView_item);
            }
        }
    }
}
```

---

## LostFoundItem.java
```java
package com.example.a71p;

import java.io.Serializable;

public class LostFoundItem implements Serializable {
    private int id;
    private String type;
    private String name;
    private String phone;
    private String description;
    private String date;
    private String location;
    private String category;
    private String imageUri;

    public LostFoundItem(int id, String type, String name, String phone, String description, String date, String location, String category, String imageUri) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.date = date;
        this.location = location;
        this.category = category;
        this.imageUri = imageUri;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getImageUri() { return imageUri; }
}
```

---

## MainActivity.java
```java
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
```

---

## RemoveItemFragment.java
```java
package com.example.a71p;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.a71p.databinding.FragmentRemoveItemBinding;

public class RemoveItemFragment extends Fragment {

    private FragmentRemoveItemBinding binding;
    private DatabaseHelper dbHelper;
    private LostFoundItem item;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRemoveItemBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        if (getArguments() != null) {
            item = (LostFoundItem) getArguments().getSerializable("item");
        }
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (item != null) {
            String details = item.getType() + " " + item.getName() + "\n\n" +
                             item.getDate() + "\n\n" +
                             "At " + item.getLocation();
            binding.textViewDetails.setText(details);
        }

        binding.buttonRemove.setOnClickListener(v -> {
            if (item != null) {
                // Delegate delete operation to MainActivity
                ((MainActivity) getActivity()).deleteItem(item.getId());
                Toast.makeText(getContext(), "Item Removed", Toast.LENGTH_SHORT).show();
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```
