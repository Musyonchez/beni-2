package com.usiu.cafeteria.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.OrdersAdapter;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.viewmodels.OrdersViewModel;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private OrdersAdapter activeAdapter, historyAdapter;
    private RecyclerView  rvActive, rvHistory;
    private TextView      tvEmpty;
    private int           activeTab = 0;

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
        tvEmpty   = view.findViewById(R.id.tv_empty_orders);

        activeAdapter  = new OrdersAdapter();
        historyAdapter = new OrdersAdapter();

        rvActive.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvActive.setAdapter(activeAdapter);
        rvHistory.setAdapter(historyAdapter);

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
                if ("collected".equals(o.getStatus())) history.add(o);
                else                                   active.add(o);
            }
            activeAdapter.submitList(active);
            historyAdapter.submitList(history);
            refreshTabVisibility();
        });
    }

    private void refreshTabVisibility() {
        if (activeTab == 0) {
            rvActive.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            tvEmpty.setVisibility(activeAdapter.getItemCount() == 0
                    ? View.VISIBLE : View.GONE);
        } else {
            rvActive.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(historyAdapter.getItemCount() == 0
                    ? View.VISIBLE : View.GONE);
        }
    }
}
