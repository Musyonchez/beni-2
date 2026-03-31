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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.PreOrdersAdapter;
import com.usiu.cafeteria.models.PreOrder;
import com.usiu.cafeteria.repository.FirestoreRepository;
import com.usiu.cafeteria.viewmodels.MenuViewModel;
import com.usiu.cafeteria.viewmodels.PreOrdersViewModel;
import com.usiu.cafeteria.MainActivity;

public class PreOrdersFragment extends Fragment {

    private PreOrdersViewModel preOrdersViewModel;
    private MenuViewModel      menuViewModel;
    private PreOrdersAdapter   adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preorders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = view.findViewById(R.id.rv_preorders);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PreOrdersAdapter(this::showCancelDialog);
        rv.setAdapter(adapter);

        View tvEmpty = view.findViewById(R.id.layout_empty_preorders);

        preOrdersViewModel = new ViewModelProvider(this).get(PreOrdersViewModel.class);
        preOrdersViewModel.getMyPreOrders().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        menuViewModel = new ViewModelProvider(this).get(MenuViewModel.class);

        FloatingActionButton fab = view.findViewById(R.id.fab_new_preorder);
        fab.setOnClickListener(v -> openNewPreOrderSheet());
    }

    private void openNewPreOrderSheet() {
        NewPreOrderBottomSheet sheet = new NewPreOrderBottomSheet();
        sheet.setMenuItems(menuViewModel.getMenuItems().getValue());
        String name = ((MainActivity) requireActivity()).walletViewModel
                .getUserName().getValue();
        sheet.setStudentName(name);
        sheet.show(getChildFragmentManager(), "new_preorder");
    }

    private void showCancelDialog(PreOrder preOrder) {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.confirm_cancel_preorder)
                .setPositiveButton(R.string.btn_confirm, (d, w) ->
                        cancelPreOrder(preOrder.getPreOrderId()))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void cancelPreOrder(String preOrderId) {
        FirestoreRepository.getInstance().cancelPreOrder(preOrderId)
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(),
                                getString(R.string.error_generic),
                                Snackbar.LENGTH_SHORT).show());
    }
}
