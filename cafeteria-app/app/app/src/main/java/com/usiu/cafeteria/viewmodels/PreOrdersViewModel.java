package com.usiu.cafeteria.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import com.usiu.cafeteria.models.PreOrder;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class PreOrdersViewModel extends ViewModel {

    private final MutableLiveData<List<PreOrder>> myPreOrders =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration listener;

    public PreOrdersViewModel() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listener = FirestoreRepository.getInstance()
                .listenToMyPreOrders(uid, (snap, e) -> {
                    if (snap != null) {
                        List<PreOrder> list = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc
                                : snap.getDocuments()) {
                            PreOrder preOrder = doc.toObject(PreOrder.class);
                            if (preOrder != null) list.add(preOrder);
                        }
                        myPreOrders.setValue(list);
                    }
                });
    }

    public LiveData<List<PreOrder>> getMyPreOrders() { return myPreOrders; }

    @Override
    protected void onCleared() {
        if (listener != null) listener.remove();
    }
}
