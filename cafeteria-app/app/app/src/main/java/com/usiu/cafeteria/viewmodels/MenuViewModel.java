package com.usiu.cafeteria.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.ListenerRegistration;

import com.usiu.cafeteria.models.MenuItem;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class MenuViewModel extends ViewModel {

    private final MutableLiveData<List<MenuItem>> menuItems =
            new MutableLiveData<>(new ArrayList<>());

    private ListenerRegistration listener;

    public MenuViewModel() {
        listener = FirestoreRepository.getInstance()
                .listenToMenuItems((snap, e) -> {
                    if (snap != null) {
                        List<MenuItem> list = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc
                                : snap.getDocuments()) {
                            MenuItem item = doc.toObject(MenuItem.class);
                            if (item != null) list.add(item);
                        }
                        menuItems.setValue(list);
                    }
                });
    }

    public LiveData<List<MenuItem>> getMenuItems() { return menuItems; }

    @Override
    protected void onCleared() {
        if (listener != null) listener.remove();
    }
}
