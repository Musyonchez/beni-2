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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.auth.LoginActivity;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.repository.FirestoreRepository;
import com.usiu.cafeteria.viewmodels.OrdersViewModel;
import com.usiu.cafeteria.viewmodels.WalletViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileWalletFragment extends Fragment {

    // Progress tracker dot views
    private View dotPending, dotPreparing, dotReady, dotCollected;
    private View line01, line12, line23;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) requireActivity();
        WalletViewModel walletViewModel = activity.walletViewModel;
        OrdersViewModel ordersViewModel = activity.ordersViewModel;

        // Header views
        TextView tvName      = view.findViewById(R.id.tv_user_name);
        TextView tvStudentId = view.findViewById(R.id.tv_student_id);
        TextView tvDate      = view.findViewById(R.id.tv_date);

        // Wallet views
        TextView tvBalance = view.findViewById(R.id.tv_wallet_balance);

        // Stat views
        TextView tvTotalOrders  = view.findViewById(R.id.tv_total_orders);
        TextView tvActiveOrders = view.findViewById(R.id.tv_active_orders);

        // Active order tracker views
        View cardActiveOrder = view.findViewById(R.id.card_active_order);
        TextView tvOrderId   = view.findViewById(R.id.tv_order_id);
        TextView tvOrderTotal = view.findViewById(R.id.tv_order_total);
        dotPending   = view.findViewById(R.id.dot_pending);
        dotPreparing = view.findViewById(R.id.dot_preparing);
        dotReady     = view.findViewById(R.id.dot_ready);
        dotCollected = view.findViewById(R.id.dot_collected);
        line01 = view.findViewById(R.id.line_01);
        line12 = view.findViewById(R.id.line_12);
        line23 = view.findViewById(R.id.line_23);

        // Today's date
        String today = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                .format(new Date());
        tvDate.setText(today);

        // Name (greeting)
        walletViewModel.getUserName().observe(getViewLifecycleOwner(),
                name -> tvName.setText(getString(R.string.label_greeting, name)));

        // Student ID (one-shot Firestore read)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirestoreRepository.getInstance().getUser(user.getUid())
                    .addOnSuccessListener(snap -> {
                        if (snap != null && snap.exists()) {
                            String sid = snap.getString("studentId");
                            if (sid != null && !sid.isEmpty()) {
                                tvStudentId.setText(
                                        getString(R.string.label_student_id_display, sid));
                            }
                        }
                    });
        }

        // Wallet balance
        walletViewModel.getWalletBalance().observe(getViewLifecycleOwner(),
                balance -> tvBalance.setText(String.format("KES %.2f", balance)));

        // Orders stats + active order tracker
        ordersViewModel.getMyOrders().observe(getViewLifecycleOwner(), list -> {
            if (list == null) {
                tvTotalOrders.setText("0");
                tvActiveOrders.setText("0");
                cardActiveOrder.setVisibility(View.GONE);
                return;
            }

            tvTotalOrders.setText(String.valueOf(list.size()));

            Order firstActive = findFirstActive(list);
            long activeCount = countActive(list);
            tvActiveOrders.setText(String.valueOf(activeCount));

            if (firstActive != null) {
                cardActiveOrder.setVisibility(View.VISIBLE);
                String shortId = "#" + firstActive.getOrderId()
                        .substring(0, Math.min(6, firstActive.getOrderId().length()))
                        .toUpperCase();
                tvOrderId.setText(shortId);
                tvOrderTotal.setText(String.format("KES %.2f", firstActive.getTotalAmount()));
                applyProgressDots(firstActive.getStatus());
            } else {
                cardActiveOrder.setVisibility(View.GONE);
            }
        });

        // Buttons
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        view.findViewById(R.id.btn_view_transactions).setOnClickListener(v ->
                activity.showWalletTransactions());

        view.findViewById(R.id.btn_go_menu).setOnClickListener(v ->
                activity.navigateToMenu());

        view.findViewById(R.id.btn_go_order_history).setOnClickListener(v ->
                activity.navigateToOrderHistory());

        view.findViewById(R.id.btn_go_preorders).setOnClickListener(v ->
                activity.navigateToPreOrders());
    }

    private Order findFirstActive(List<Order> orders) {
        for (Order o : orders) {
            String s = o.getStatus();
            if (!"collected".equals(s) && !"cancelled".equals(s)) return o;
        }
        return null;
    }

    private long countActive(List<Order> orders) {
        long count = 0;
        for (Order o : orders) {
            String s = o.getStatus();
            if (!"collected".equals(s) && !"cancelled".equals(s)) count++;
        }
        return count;
    }

    /**
     * Updates the 4 progress dots and 3 connecting lines based on the order status.
     *   done = gold filled, active = navy filled, todo = grey outline
     * Status pipeline: pending → preparing → ready → collected
     */
    private void applyProgressDots(String status) {
        // stage index: 0=pending, 1=preparing, 2=ready, 3=collected
        int activeStage;
        switch (status == null ? "" : status) {
            case "preparing":  activeStage = 1; break;
            case "ready":      activeStage = 2; break;
            case "collected":  activeStage = 3; break;
            default:           activeStage = 0; break; // pending / unknown
        }

        View[] dots  = { dotPending, dotPreparing, dotReady, dotCollected };
        View[] lines = { line01, line12, line23 };

        int colorNavy   = requireContext().getColor(R.color.navy);
        int colorGrey   = 0xFFBDBDBD;
        int colorNavyLine = colorNavy;

        for (int i = 0; i < dots.length; i++) {
            if (i < activeStage) {
                dots[i].setBackgroundResource(R.drawable.shape_dot_done);
            } else if (i == activeStage) {
                dots[i].setBackgroundResource(R.drawable.shape_dot_active);
            } else {
                dots[i].setBackgroundResource(R.drawable.shape_dot_todo);
            }
        }

        for (int i = 0; i < lines.length; i++) {
            // line i connects dot[i] to dot[i+1]; colour navy if dot[i+1] is done/active
            lines[i].setBackgroundColor(i < activeStage ? colorNavyLine : colorGrey);
        }
    }
}
