package com.usiu.cafeteria.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class OrdersViewModel extends ViewModel {

    private final MutableLiveData<List<Order>> myOrders =
            new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Order>> allActiveOrders =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration myOrdersListener;
    private ListenerRegistration allActiveListener;

    // ── Student: listen to own orders ────────────────────────────────────────

    public void startListeningMyOrders() {
        if (myOrdersListener != null) return; // already listening
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myOrdersListener = FirestoreRepository.getInstance()
                .listenToMyOrders(uid, (snap, e) -> {
                    if (snap != null) {
                        List<Order> list = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc
                                : snap.getDocuments()) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) list.add(order);
                        }
                        myOrders.setValue(list);
                    }
                });
    }

    // ── Staff: listen to all active orders ───────────────────────────────────

    public void startListeningAllActive() {
        if (allActiveListener != null) return; // already listening
        allActiveListener = FirestoreRepository.getInstance()
                .listenToAllActiveOrders((snap, e) -> {
                    if (snap != null) {
                        List<Order> list = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc
                                : snap.getDocuments()) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) list.add(order);
                        }
                        allActiveOrders.setValue(list);
                    }
                });
    }

    public LiveData<List<Order>> getMyOrders() { return myOrders; }

    public LiveData<List<Order>> getAllActiveOrders() { return allActiveOrders; }

    @Override
    protected void onCleared() {
        if (myOrdersListener  != null) myOrdersListener.remove();
        if (allActiveListener != null) allActiveListener.remove();
    }
}
