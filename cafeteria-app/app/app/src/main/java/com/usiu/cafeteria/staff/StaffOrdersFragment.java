package com.usiu.cafeteria.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.StaffOrdersAdapter;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.repository.FirestoreRepository;

public class StaffOrdersFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StaffMainActivity activity = (StaffMainActivity) requireActivity();

        RecyclerView rv = view.findViewById(R.id.rv_staff_orders);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        View tvEmpty = view.findViewById(R.id.tv_empty_staff_orders);

        StaffOrdersAdapter adapter = new StaffOrdersAdapter(this::onOrderAction);
        rv.setAdapter(adapter);

        activity.ordersViewModel.getAllActiveOrders().observe(getViewLifecycleOwner(), orders -> {
            adapter.submitList(orders);
            tvEmpty.setVisibility(orders == null || orders.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });
    }

    private void onOrderAction(Order order, String newStatus) {
        if ("collected".equals(newStatus) && "cash".equals(order.getPaymentMethod())) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.confirm_cash_collected)
                    .setPositiveButton(R.string.btn_confirm, (d, w) ->
                            updateStatus(order.getOrderId(), newStatus, order.getUserId()))
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        } else {
            updateStatus(order.getOrderId(), newStatus, order.getUserId());
        }
    }

    private void updateStatus(String orderId, String newStatus, String userId) {
        FirestoreRepository.getInstance().updateOrderStatus(orderId, newStatus, userId)
                .addOnFailureListener(e ->
                        Snackbar.make(requireView(),
                                getString(R.string.error_generic),
                                Snackbar.LENGTH_SHORT).show());
    }
}
