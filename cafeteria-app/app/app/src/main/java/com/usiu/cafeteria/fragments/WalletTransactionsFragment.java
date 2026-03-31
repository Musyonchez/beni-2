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

import com.google.android.material.appbar.MaterialToolbar;
import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.WalletTransactionAdapter;
import com.usiu.cafeteria.viewmodels.WalletViewModel;

public class WalletTransactionsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet_transactions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_transactions);
        toolbar.setNavigationOnClickListener(v ->
                ((MainActivity) requireActivity()).showDashboard());

        RecyclerView rv = view.findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        WalletTransactionAdapter adapter = new WalletTransactionAdapter();
        rv.setAdapter(adapter);

        TextView tvEmpty = view.findViewById(R.id.tv_empty_transactions);

        WalletViewModel walletViewModel = ((MainActivity) requireActivity()).walletViewModel;
        walletViewModel.getTransactions().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            tvEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }
}
