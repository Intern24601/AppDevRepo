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
