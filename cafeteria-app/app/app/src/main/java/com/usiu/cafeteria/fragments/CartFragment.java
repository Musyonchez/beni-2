package com.usiu.cafeteria.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.adapters.CartAdapter;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.models.OrderItem;
import com.usiu.cafeteria.repository.FirestoreRepository;
import com.usiu.cafeteria.viewmodels.CartViewModel;
import com.usiu.cafeteria.viewmodels.WalletViewModel;

import java.util.List;

public class CartFragment extends Fragment {

    private CartViewModel   cartViewModel;
    private WalletViewModel walletViewModel;
    private CartAdapter     adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) requireActivity();
        cartViewModel   = activity.cartViewModel;
        walletViewModel = activity.walletViewModel;

        RecyclerView rv = view.findViewById(R.id.rv_cart);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CartAdapter(new CartAdapter.CartCallback() {
            @Override public void onQuantityChanged(String itemId, int qty) {
                cartViewModel.updateQuantity(itemId, qty);
            }
            @Override public void onRemove(String itemId) {
                cartViewModel.removeItem(itemId);
            }
        });
        rv.setAdapter(adapter);

        TextView tvSubtotal    = view.findViewById(R.id.tv_subtotal);
        TextView tvWait        = view.findViewById(R.id.tv_estimated_wait);
        View     layoutEmpty   = view.findViewById(R.id.layout_empty_cart);
        MaterialButton btnPlaceOrder = view.findViewById(R.id.btn_place_order);

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean empty = items == null || items.isEmpty();
            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            btnPlaceOrder.setEnabled(!empty);
        });

        cartViewModel.subtotal.observe(getViewLifecycleOwner(), sub ->
                tvSubtotal.setText(String.format("KES %.2f", sub)));

        cartViewModel.getEstimatedWaitMin().observe(getViewLifecycleOwner(), wait ->
                tvWait.setText(getString(R.string.label_estimated_wait, wait)));

        // Update wallet button text when balance changes
        MaterialButton btnWallet = view.findViewById(R.id.btn_pay_wallet);
        walletViewModel.getWalletBalance().observe(getViewLifecycleOwner(), balance ->
                btnWallet.setText(getString(R.string.btn_pay_wallet, balance)));

        view.findViewById(R.id.btn_browse_menu).setOnClickListener(v ->
                activity.navigateToMenu());

        btnPlaceOrder.setOnClickListener(v -> placeOrder(view));
    }

    @Override
    public void onResume() {
        super.onResume();
        cartViewModel.refreshEstimatedWait();
    }

    private void placeOrder(View view) {
        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggle_payment);
        boolean useWallet = toggle.getCheckedButtonId() == R.id.btn_pay_wallet;

        List<OrderItem> items  = cartViewModel.getCartItems().getValue();
        Double subtotal        = cartViewModel.subtotal.getValue();
        Integer waitMin        = cartViewModel.getEstimatedWaitMin().getValue();
        String uid             = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String studentName     = walletViewModel.getUserName().getValue();

        if (items == null || items.isEmpty()) return;

        Order order = new Order();
        order.setUserId(uid);
        order.setStudentName(studentName != null ? studentName : "");
        order.setItems(items);
        order.setStatus("pending");
        order.setPaymentMethod(useWallet ? "wallet" : "cash");
        order.setTotalAmount(subtotal != null ? subtotal : 0);
        order.setEstimatedWaitMin(waitMin != null ? waitMin : 5);
        order.setCreatedAt(Timestamp.now());

        if (useWallet) {
            FirestoreRepository.getInstance().placeOrderWithWalletDeduction(order)
                    .addOnSuccessListener(unused -> onOrderSuccess(view))
                    .addOnFailureListener(e -> {
                        if (e.getMessage() != null && e.getMessage().contains("Insufficient")) {
                            showInsufficientFundsDialog();
                        } else {
                            Snackbar.make(view, getString(R.string.error_generic),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    });
        } else {
            FirestoreRepository.getInstance().placeOrderCash(order)
                    .addOnSuccessListener(unused -> onOrderSuccess(view))
                    .addOnFailureListener(e -> Snackbar.make(view,
                            getString(R.string.error_generic), Snackbar.LENGTH_LONG).show());
        }
    }

    private void onOrderSuccess(View view) {
        cartViewModel.clearCart();
        Snackbar.make(view, getString(R.string.msg_order_placed), Snackbar.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).navigateToOrders();
    }

    private void showInsufficientFundsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.label_wallet_balance)
                .setMessage(R.string.msg_insufficient_funds)
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }
}
