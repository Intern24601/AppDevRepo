package com.example.a41p.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.a41p.data.Event;
import com.example.a41p.databinding.ItemEventBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapter for the RecyclerView in EventListFragment.
 * It uses ListAdapter with DiffUtil to efficiently manage and display a list of events.
 */
public class EventAdapter extends ListAdapter<Event, EventAdapter.EventViewHolder> {

    private final OnItemClickListener clickListener;
    private final OnDeleteClickListener deleteListener;

    /**
     * Interface for handling item click events (e.g., for editing).
     */
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    /**
     * Interface for handling delete button clicks.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    public EventAdapter(OnItemClickListener clickListener, OnDeleteClickListener deleteListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    /**
     * DiffUtil callback to determine how the list has changed.
     * This allows the adapter to only update the items that have actually changed.
     */
    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK = new DiffUtil.ItemCallback<Event>() {
        @Override
        public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            // Check if it's the same object based on ID
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
            // Check if the contents of the objects are identical
            return Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                    Objects.equals(oldItem.getCategory(), newItem.getCategory()) &&
                    Objects.equals(oldItem.getLocation(), newItem.getLocation()) &&
                    oldItem.getDateTime() == newItem.getDateTime();
        }
    };

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout using View Binding
        ItemEventBinding binding = ItemEventBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new EventViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        // Bind the data from the event object to the ViewHolder
        holder.bind(getItem(position));
    }

    /**
     * ViewHolder class that holds references to the views for each list item.
     */
    class EventViewHolder extends RecyclerView.ViewHolder {
        private final ItemEventBinding binding;

        public EventViewHolder(ItemEventBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Populates the views with data from the given event.
         */
        public void bind(Event event) {
            binding.tvTitle.setText(event.getTitle());
            binding.tvCategory.setText(event.getCategory());
            binding.tvLocation.setText(event.getLocation());

            // Format the date and time for display
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            binding.tvDate.setText(sdf.format(new Date(event.getDateTime())));

            // Set up click listeners
            binding.getRoot().setOnClickListener(v -> clickListener.onItemClick(event));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onDeleteClick(event));
        }
    }
}
