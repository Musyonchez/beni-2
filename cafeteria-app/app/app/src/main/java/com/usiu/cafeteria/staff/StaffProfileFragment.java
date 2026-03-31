package com.usiu.cafeteria.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.auth.LoginActivity;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffProfileFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StaffMainActivity activity = (StaffMainActivity) requireActivity();

        TextView tvName           = view.findViewById(R.id.tv_staff_name);
        TextView tvDate           = view.findViewById(R.id.tv_date);
        TextView tvActiveOrdersBig = view.findViewById(R.id.tv_active_orders_big);
        TextView tvPendingCount   = view.findViewById(R.id.tv_pending_count);
        TextView tvMenuItems      = view.findViewById(R.id.tv_menu_items);
        View     cardPendingAlert = view.findViewById(R.id.card_pending_alert);
        TextView tvPendingLabel   = view.findViewById(R.id.tv_pending_label);

        // Today's date
        tvDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                .format(new Date()));

        // Staff name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirestoreRepository.getInstance().getUser(user.getUid())
                    .addOnSuccessListener(snap -> {
                        if (snap != null && snap.exists()) {
                            String name = snap.getString("name");
                            tvName.setText(getString(R.string.label_greeting,
                                    name != null ? name : user.getEmail()));
                        }
                    });
        }

        // Active orders — populate summary card + stats
        activity.ordersViewModel.getAllActiveOrders().observe(getViewLifecycleOwner(), list -> {
            int total = list == null ? 0 : list.size();
            tvActiveOrdersBig.setText(String.valueOf(total));

            int pending = countPending(list);
            tvPendingCount.setText(String.valueOf(pending));

            if (pending > 0) {
                cardPendingAlert.setVisibility(View.VISIBLE);
                tvPendingLabel.setText(pending + " pending");
            } else {
                cardPendingAlert.setVisibility(View.GONE);
            }
        });

        // Menu items count
        activity.menuViewModel.getMenuItems().observe(getViewLifecycleOwner(), list ->
                tvMenuItems.setText(list == null ? "0" : String.valueOf(list.size())));

        // Logout
        view.findViewById(R.id.btn_staff_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        // Quick actions
        view.findViewById(R.id.btn_go_orders).setOnClickListener(v ->
                activity.navigateToOrders());
        view.findViewById(R.id.btn_go_incoming).setOnClickListener(v ->
                activity.navigateToOrders());
        view.findViewById(R.id.btn_go_menu_nav).setOnClickListener(v ->
                activity.navigateToMenu());
    }

    private int countPending(List<Order> list) {
        if (list == null) return 0;
        int count = 0;
        for (Order o : list) {
            if ("pending".equals(o.getStatus())) count++;
        }
        return count;
    }
}
