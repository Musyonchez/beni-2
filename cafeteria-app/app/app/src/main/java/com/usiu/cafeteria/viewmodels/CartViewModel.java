package com.usiu.cafeteria.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.usiu.cafeteria.models.MenuItem;
import com.usiu.cafeteria.models.OrderItem;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {

    private static final int AVG_PREP_TIME_PER_ORDER = 5;

    private final MutableLiveData<List<OrderItem>> cartItems =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<Integer> estimatedWaitMin = new MutableLiveData<>(5);

    public final LiveData<Double> subtotal = Transformations.map(cartItems, items -> {
        double total = 0;
        if (items != null) {
            for (OrderItem item : items) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    });

    public LiveData<List<OrderItem>> getCartItems() { return cartItems; }

    public LiveData<Integer> getEstimatedWaitMin() { return estimatedWaitMin; }

    // ── Cart mutations ────────────────────────────────────────────────────────

    public void addItem(MenuItem menuItem) {
        List<OrderItem> current = new ArrayList<>(currentList());
        for (OrderItem item : current) {
            if (item.getItemId().equals(menuItem.getItemId())) {
                item.setQuantity(item.getQuantity() + 1);
                cartItems.setValue(current);
                return;
            }
        }
        OrderItem newItem = new OrderItem();
        newItem.setItemId(menuItem.getItemId());
        newItem.setName(menuItem.getName());
        newItem.setPrice(menuItem.getPrice());
        newItem.setQuantity(1);
        current.add(newItem);
        cartItems.setValue(current);
    }

    public void removeItem(String itemId) {
        List<OrderItem> current = new ArrayList<>(currentList());
        current.removeIf(item -> item.getItemId().equals(itemId));
        cartItems.setValue(current);
    }

    public void updateQuantity(String itemId, int quantity) {
        if (quantity <= 0) {
            removeItem(itemId);
            return;
        }
        List<OrderItem> current = new ArrayList<>(currentList());
        for (OrderItem item : current) {
            if (item.getItemId().equals(itemId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        cartItems.setValue(current);
    }

    public void clearCart() {
        cartItems.setValue(new ArrayList<>());
    }

    public int getItemCount() {
        int count = 0;
        for (OrderItem item : currentList()) count += item.getQuantity();
        return count;
    }

    // ── Estimated wait time ───────────────────────────────────────────────────

    /** Call from CartFragment.onResume() — one-shot, not a real-time listener. */
    public void refreshEstimatedWait() {
        FirestoreRepository.getInstance().getActiveOrders()
                .addOnSuccessListener(snap -> {
                    int count = snap.size();
                    int wait  = Math.max(count, 1) * AVG_PREP_TIME_PER_ORDER;
                    estimatedWaitMin.setValue(wait);
                });
    }

    // ─────────────────────────────────────────────────────────────────────────

    private List<OrderItem> currentList() {
        List<OrderItem> list = cartItems.getValue();
        return list != null ? list : new ArrayList<>();
    }
}
