package com.example.a41p.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.a41p.R;
import com.example.a41p.adapter.EventAdapter;
import com.example.a41p.data.Event;
import com.example.a41p.databinding.FragmentEventListBinding;
import com.google.android.material.snackbar.Snackbar;

import com.example.a41p.MainActivity;

/**
 * Fragment that displays a list of events.
 * It now delegates data operations to the MainActivity.
 */
public class EventListFragment extends Fragment {

    private FragmentEventListBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get a reference to the MainActivity to use its centralized methods
        MainActivity activity = (MainActivity) requireActivity();

        EventAdapter adapter = new EventAdapter(
                event -> {
                    Bundle bundle = new Bundle();
                    bundle.putLong("eventId", event.getId());
                    Navigation.findNavController(view).navigate(R.id.action_eventListFragment_to_addEditEventFragment, bundle);
                },
                event -> {
                    // Delegate deletion to MainActivity
                    activity.handleDeleteEvent(event);
                }
        );

        binding.recyclerView.setAdapter(adapter);

        // Observe the list of events provided by the MainActivity
        activity.getEvents().observe(getViewLifecycleOwner(), adapter::submitList);

        binding.fabAdd.setOnClickListener(v -> 
            Navigation.findNavController(view).navigate(R.id.action_eventListFragment_to_addEditEventFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
