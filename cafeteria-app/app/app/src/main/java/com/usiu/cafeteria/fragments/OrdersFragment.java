package com.usiu.cafeteria.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.OrdersAdapter;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.repository.FirestoreRepository;
import com.usiu.cafeteria.viewmodels.OrdersViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OrdersFragment extends Fragment {

    private OrdersAdapter activeAdapter, historyAdapter;
    private RecyclerView  rvActive, rvHistory;
    private TextView      tvEmpty;
    private int           activeTab = 0;
    private List<Order>   allHistory = new ArrayList<>();
    private int           historyFilter = 0; // 0=today, 1=week, 2=month, 3=all

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rvActive  = view.findViewById(R.id.rv_active);
        rvHistory = view.findViewById(R.id.rv_history);
        tvEmpty   = view.findViewById(R.id.layout_empty_orders);

        activeAdapter  = new OrdersAdapter(this::onCancelOrder);
        historyAdapter = new OrdersAdapter(this::onCancelOrder);

        rvActive.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActive.setAdapter(activeAdapter);
        rvHistory.setAdapter(historyAdapter);

        ChipGroup chipGroup = view.findViewById(R.id.chip_group_history_filter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_filter_today)       historyFilter = 0;
            else if (id == R.id.chip_filter_week)   historyFilter = 1;
            else if (id == R.id.chip_filter_month)  historyFilter = 2;
            else                                    historyFilter = 3;
            applyHistoryFilter();
        });

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                activeTab = tab.getPosition();
                refreshTabVisibility();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        OrdersViewModel vm = ((MainActivity) requireActivity()).ordersViewModel;
        vm.getMyOrders().observe(getViewLifecycleOwner(), orders -> {
            List<Order> active  = new ArrayList<>();
            List<Order> history = new ArrayList<>();
            for (Order o : orders) {
                if ("collected".equals(o.getStatus()) || "cancelled".equals(o.getStatus())) {
                    history.add(o);
                } else {
                    active.add(o);
                }
            }
            activeAdapter.submitList(active);
            allHistory = history;
            applyHistoryFilter();
            refreshTabVisibility();
        });
    }

    private void onCancelOrder(Order order) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cancel Order")
                .setMessage("Cancel order #" + (order.getOrderId() != null && order.getOrderId().length() > 6
                        ? order.getOrderId().substring(0, 6).toUpperCase()
                        : order.getOrderId()) + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirestoreRepository.getInstance().cancelOrder(order.getOrderId())
                            .addOnSuccessListener(unused -> {
                                Snackbar.make(requireView(), "Order cancelled", Snackbar.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("OrdersFragment", "Failed to cancel order", e);
                                Snackbar.make(requireView(), "Failed to cancel order", Snackbar.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void applyHistoryFilter() {
        List<Order> filtered = new ArrayList<>();
        long startOfDay = getStartOfDay();
        long startOfWeek = startOfDay - 6L * 24 * 60 * 60 * 1000;
        long startOfMonth = getStartOfMonth();

        for (Order o : allHistory) {
            if (o.getCreatedAt() == null) continue;
            long ts = o.getCreatedAt().toDate().getTime();
            if (historyFilter == 0 && ts >= startOfDay) filtered.add(o);
            else if (historyFilter == 1 && ts >= startOfWeek) filtered.add(o);
            else if (historyFilter == 2 && ts >= startOfMonth) filtered.add(o);
            else if (historyFilter == 3) filtered.add(o);
        }
        historyAdapter.submitList(filtered);
    }

    private long getStartOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /** Called from MainActivity dashboard shortcut to jump directly to History tab. */
    public void showHistoryTab() {
        if (getView() == null) return;
        TabLayout tabs = getView().findViewById(R.id.tab_layout);
        TabLayout.Tab history = tabs != null ? tabs.getTabAt(1) : null;
        if (history != null) history.select();
    }

    private void refreshTabVisibility() {
        View filterBar = getView().findViewById(R.id.scroll_history_filter);
        if (activeTab == 0) {
            rvActive.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            filterBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(activeAdapter.getItemCount() == 0
                    ? View.VISIBLE : View.GONE);
        } else {
            rvActive.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            filterBar.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(historyAdapter.getItemCount() == 0
                    ? View.VISIBLE : View.GONE);
        }
    }
}
