package com.usiu.cafeteria.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import com.usiu.cafeteria.models.WalletTransaction;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class WalletViewModel extends ViewModel {

    private final MutableLiveData<Double> walletBalance = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<WalletTransaction>> transactions =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration userListener;
    private ListenerRegistration txListener;

    public WalletViewModel() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        startListening(uid);
    }

    private void startListening(String uid) {
        // Listen to wallet balance via the user document
        userListener = FirestoreRepository.getInstance().listenToUser(uid, (snap, e) -> {
            if (snap != null && snap.exists()) {
                Double balance = snap.getDouble("walletBalance");
                walletBalance.setValue(balance != null ? balance : 0.0);
            }
        });

        // Listen to transaction history
        txListener = FirestoreRepository.getInstance()
                .listenToWalletTransactions(uid, (snap, e) -> {
                    if (snap != null) {
                        List<WalletTransaction> list = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                            WalletTransaction tx = doc.toObject(WalletTransaction.class);
                            if (tx != null) list.add(tx);
                        }
                        transactions.setValue(list);
                    }
                });
    }

    public LiveData<Double> getWalletBalance() { return walletBalance; }

    public LiveData<List<WalletTransaction>> getTransactions() { return transactions; }

    @Override
    protected void onCleared() {
        if (userListener != null) userListener.remove();
        if (txListener   != null) txListener.remove();
    }
}
