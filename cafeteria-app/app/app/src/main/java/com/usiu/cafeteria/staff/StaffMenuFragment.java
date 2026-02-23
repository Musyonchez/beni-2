package com.usiu.cafeteria.staff;

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
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.StaffMenuAdapter;
import com.usiu.cafeteria.models.MenuItem;
import com.usiu.cafeteria.repository.FirestoreRepository;
import com.usiu.cafeteria.viewmodels.MenuViewModel;

import java.util.ArrayList;
import java.util.List;

public class StaffMenuFragment extends Fragment {

    private MenuViewModel    menuViewModel;
    private StaffMenuAdapter adapter;
    private String           activeCategory = "all";

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.rv_staff_menu);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        View tvEmpty = view.findViewById(R.id.tv_empty_staff_menu);

        adapter = new StaffMenuAdapter((itemId, available) ->
                FirestoreRepository.getInstance()
                        .updateMenuItemAvailability(itemId, available)
                        .addOnFailureListener(e ->
                                Snackbar.make(requireView(),
                                        getString(R.string.error_generic),
                                        Snackbar.LENGTH_SHORT).show()));
        rv.setAdapter(adapter);

        ChipGroup chipGroup = view.findViewById(R.id.chip_group_staff_filter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if      (id == R.id.chip_staff_breakfast) activeCategory = "breakfast";
            else if (id == R.id.chip_staff_lunch)     activeCategory = "lunch";
            else if (id == R.id.chip_staff_dinner)    activeCategory = "dinner";
            else                                      activeCategory = "all";
            applyFilter();
        });

        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);
        menuViewModel.getMenuItems().observe(getViewLifecycleOwner(),
                items -> applyFilter(items, tvEmpty));
    }

    private void applyFilter() {
        List<MenuItem> all = menuViewModel.getMenuItems().getValue();
        View tvEmpty = getView() != null ? getView().findViewById(R.id.tv_empty_staff_menu) : null;
        if (all != null) applyFilter(all, tvEmpty);
    }

    private void applyFilter(List<MenuItem> all, View tvEmpty) {
        List<MenuItem> filtered;
        if ("all".equals(activeCategory)) {
            filtered = new ArrayList<>(all);
        } else {
            filtered = new ArrayList<>();
            for (MenuItem item : all) {
                if (activeCategory.equals(item.getCategory())) filtered.add(item);
            }
        }
        adapter.submitList(filtered);
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
