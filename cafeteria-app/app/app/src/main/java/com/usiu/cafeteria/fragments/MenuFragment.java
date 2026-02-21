package com.usiu.cafeteria.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.MenuAdapter;
import com.usiu.cafeteria.models.MenuItem;
import com.usiu.cafeteria.viewmodels.MenuViewModel;

import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {

    private MenuViewModel menuViewModel;
    private MenuAdapter   adapter;
    private String        activeCategory = "lunch";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.rv_menu);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new MenuAdapter(item -> {
            ((MainActivity) requireActivity()).cartViewModel.addItem(item);
            Snackbar.make(view, item.getName() + " added to cart", Snackbar.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_breakfast)      activeCategory = "breakfast";
            else if (id == R.id.chip_lunch)     activeCategory = "lunch";
            else if (id == R.id.chip_dinner)    activeCategory = "dinner";
            applyFilter();
        });

        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        menuViewModel.getMenuItems().observe(getViewLifecycleOwner(), items -> {
            applyFilter(items, view);
        });
    }

    private void applyFilter() {
        List<MenuItem> all = menuViewModel.getMenuItems().getValue();
        if (all != null) applyFilter(all, getView());
    }

    private void applyFilter(List<MenuItem> all, View view) {
        List<MenuItem> filtered = new ArrayList<>();
        for (MenuItem item : all) {
            if (activeCategory.equals(item.getCategory())) filtered.add(item);
        }
        adapter.submitList(filtered);

        View empty = view.findViewById(R.id.layout_empty);
        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
