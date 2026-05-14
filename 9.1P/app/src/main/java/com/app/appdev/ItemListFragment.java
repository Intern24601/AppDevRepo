package com.app.appdev;

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
import com.app.appdev.databinding.FragmentItemListBinding;
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
            bundle.putBoolean("canEdit", false);
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
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE)),
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
