package com.app.appdev;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.app.appdev.databinding.FragmentRemoveItemBinding;

public class RemoveItemFragment extends Fragment {

    private FragmentRemoveItemBinding binding;
    private DatabaseHelper dbHelper;
    private LostFoundItem item;
    private boolean canEdit = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRemoveItemBinding.inflate(inflater, container, false);
        dbHelper = new DatabaseHelper(getContext());
        if (getArguments() != null) {
            item = (LostFoundItem) getArguments().getSerializable("item");
            canEdit = getArguments().getBoolean("canEdit", false);
        }
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide Edit/Remove buttons if not allowed
        if (!canEdit) {
            binding.buttonEdit.setVisibility(View.GONE);
            binding.buttonRemove.setVisibility(View.GONE);
        }

        if (item != null) {
            String details = item.getType() + " " + item.getName() + "\n\n" +
                             item.getDate() + "\n\n" +
                             "At " + item.getLocation();
            binding.textViewDetails.setText(details);
        }

        binding.buttonRemove.setOnClickListener(v -> {
            if (item != null && getActivity() != null) {
                // Delegate delete operation to MainActivity
                ((MainActivity) getActivity()).deleteItem(item.getId());
                Toast.makeText(getContext(), "Item Removed", Toast.LENGTH_SHORT).show();
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.buttonEdit.setOnClickListener(v -> {
            if (item != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("item", item);
                NavHostFragment.findNavController(RemoveItemFragment.this)
                        .navigate(R.id.action_global_CreateAdvertFragment, bundle);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
