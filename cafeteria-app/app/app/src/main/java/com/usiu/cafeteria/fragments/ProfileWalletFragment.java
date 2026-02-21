package com.usiu.cafeteria.fragments;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.WalletTransactionAdapter;
import com.usiu.cafeteria.auth.LoginActivity;
import com.usiu.cafeteria.viewmodels.WalletViewModel;

public class ProfileWalletFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WalletViewModel walletViewModel =
                ((MainActivity) requireActivity()).walletViewModel;

        TextView tvName    = view.findViewById(R.id.tv_user_name);
        TextView tvEmail   = view.findViewById(R.id.tv_user_email);
        TextView tvBalance = view.findViewById(R.id.tv_wallet_balance);
        View     tvEmptyTx = view.findViewById(R.id.tv_empty_transactions);

        RecyclerView rv = view.findViewById(R.id.rv_transactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        WalletTransactionAdapter adapter = new WalletTransactionAdapter();
        rv.setAdapter(adapter);

        walletViewModel.getUserName().observe(getViewLifecycleOwner(),
                name -> tvName.setText(name));

        walletViewModel.getUserEmail().observe(getViewLifecycleOwner(),
                email -> tvEmail.setText(email));

        walletViewModel.getWalletBalance().observe(getViewLifecycleOwner(),
                balance -> tvBalance.setText(String.format("KES %.2f", balance)));

        walletViewModel.getTransactions().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            tvEmptyTx.setVisibility(list == null || list.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}
