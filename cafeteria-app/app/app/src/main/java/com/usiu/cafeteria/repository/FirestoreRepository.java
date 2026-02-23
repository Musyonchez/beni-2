package com.usiu.cafeteria.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import com.usiu.cafeteria.models.MenuItem;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.models.PreOrder;
import com.usiu.cafeteria.models.User;
import com.usiu.cafeteria.models.WalletTransaction;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class FirestoreRepository {

    private static FirestoreRepository instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private FirestoreRepository() {
        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirestoreRepository getInstance() {
        if (instance == null) instance = new FirestoreRepository();
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Users
    // ─────────────────────────────────────────────────────────────────────────

    public Task<DocumentSnapshot> getUser(String uid) {
        return db.collection("users").document(uid).get();
    }

    public Task<Void> createUser(User user) {
        return db.collection("users")
                .document(user.getUid())
                .set(user);
    }

    public Task<QuerySnapshot> getUserByStudentId(String studentId) {
        return db.collection("users")
                .whereEqualTo("studentId", studentId)
                .limit(1)
                .get();
    }

    public Task<Void> updateFcmToken(String uid, String token) {
        return db.collection("users")
                .document(uid)
                .update("fcmToken", token);
    }

    public ListenerRegistration listenToUser(String uid,
                                             EventListener<DocumentSnapshot> listener) {
        return db.collection("users").document(uid).addSnapshotListener(listener);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menu Items
    // ─────────────────────────────────────────────────────────────────────────

    public ListenerRegistration listenToMenuItems(EventListener<QuerySnapshot> listener) {
        return db.collection("menuItems")
                .orderBy("category")
                .addSnapshotListener(listener);
    }

    public Task<Void> addMenuItem(MenuItem item) {
        DocumentReference ref = db.collection("menuItems").document();
        item.setItemId(ref.getId());
        item.setCreatedAt(Timestamp.now());
        return ref.set(item);
    }

    public Task<Void> updateMenuItem(String itemId, Map<String, Object> updates) {
        return db.collection("menuItems").document(itemId).update(updates);
    }

    public Task<Void> updateMenuItemAvailability(String itemId, boolean available) {
        return db.collection("menuItems")
                .document(itemId)
                .update("available", available);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Orders
    // ─────────────────────────────────────────────────────────────────────────

    /** Cash order — just write the order doc, no wallet transaction needed. */
    public Task<Void> placeOrderCash(Order order) {
        DocumentReference ref = db.collection("orders").document();
        order.setOrderId(ref.getId());
        return ref.set(order);
    }

    /**
     * Wallet order — atomic Firestore transaction:
     *   1. Check balance >= total
     *   2. Deduct from walletBalance
     *   3. Write order doc
     *   4. Write walletTransaction doc (type=deduction)
     * Throws FirebaseFirestoreException(ABORTED) on insufficient funds.
     */
    public Task<Void> placeOrderWithWalletDeduction(Order order) {
        String uid = auth.getCurrentUser().getUid();

        DocumentReference userRef  = db.collection("users").document(uid);
        DocumentReference orderRef = db.collection("orders").document();
        DocumentReference txRef    = db.collection("walletTransactions").document();

        order.setOrderId(orderRef.getId());

        return db.runTransaction(transaction -> {
            DocumentSnapshot userSnap = transaction.get(userRef);
            double balance = userSnap.getDouble("walletBalance");

            if (balance < order.getTotalAmount()) {
                throw new FirebaseFirestoreException(
                        "Insufficient funds",
                        FirebaseFirestoreException.Code.ABORTED);
            }

            transaction.update(userRef, "walletBalance",
                    balance - order.getTotalAmount());

            transaction.set(orderRef, order);

            WalletTransaction wt = new WalletTransaction();
            wt.setTxId(txRef.getId());
            wt.setUserId(uid);
            wt.setType("deduction");
            wt.setAmount(order.getTotalAmount());
            wt.setDescription("Order #" + order.getOrderId());
            wt.setRelatedOrderId(order.getOrderId());
            wt.setCreatedAt(Timestamp.now());
            transaction.set(txRef, wt);

            return null;
        });
    }

    /** Real-time listener for a student's own orders, newest first. */
    public ListenerRegistration listenToMyOrders(String userId,
                                                 EventListener<QuerySnapshot> listener) {
        return db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    /** Real-time listener for all active orders (staff view). */
    public ListenerRegistration listenToAllActiveOrders(EventListener<QuerySnapshot> listener) {
        return db.collection("orders")
                .whereIn("status", Arrays.asList("pending", "preparing", "ready"))
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }

    public Task<Void> updateOrderStatus(String orderId, String newStatus, String userId) {
        Task<Void> task = db.collection("orders")
                .document(orderId)
                .update("status", newStatus);

        if ("ready".equals(newStatus)) {
            task.addOnSuccessListener(unused -> notifyOrderReady(userId));
        }

        return task;
    }

    /** Fire-and-forget HTTP POST to Supabase Edge Function to send FCM push. */
    private void notifyOrderReady(String userId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String notifyUrl = com.usiu.cafeteria.BuildConfig.SUPABASE_NOTIFY_URL;
                String secret    = com.usiu.cafeteria.BuildConfig.FUNCTIONS_SECRET;
                if (notifyUrl.isEmpty()) return;

                URL url = new URL(notifyUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + secret);
                conn.setDoOutput(true);

                byte[] body = ("{\"userId\":\"" + userId + "\"}").getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                }

                conn.getResponseCode(); // execute request
                conn.disconnect();
            } catch (Exception e) {
                // Non-critical — order is already updated in Firestore
                android.util.Log.w("FirestoreRepository", "notifyOrderReady failed: " + e.getMessage());
            }
        });
    }

    /**
     * One-shot query for the count of active orders (pending + preparing).
     * CartViewModel calls .size() on the resulting QuerySnapshot.
     */
    public Task<QuerySnapshot> getActiveOrders() {
        return db.collection("orders")
                .whereIn("status", Arrays.asList("pending", "preparing"))
                .get();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pre-Orders
    // ─────────────────────────────────────────────────────────────────────────

    public Task<Void> createPreOrder(PreOrder preOrder) {
        DocumentReference ref = db.collection("preOrders").document();
        preOrder.setPreOrderId(ref.getId());
        return ref.set(preOrder);
    }

    public ListenerRegistration listenToMyPreOrders(String userId,
                                                    EventListener<QuerySnapshot> listener) {
        return db.collection("preOrders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }

    public Task<Void> cancelPreOrder(String preOrderId) {
        return db.collection("preOrders")
                .document(preOrderId)
                .update("status", "cancelled");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Wallet
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Staff top-up — atomic Firestore transaction:
     *   1. Add amount to student's walletBalance
     *   2. Write walletTransaction doc (type=topup)
     */
    public Task<Void> topUpWallet(String userId, double amount) {
        String staffId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "unknown";
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference txRef   = db.collection("walletTransactions").document();

        return db.runTransaction(transaction -> {
            DocumentSnapshot userSnap = transaction.get(userRef);
            double current = userSnap.getDouble("walletBalance");

            transaction.update(userRef, "walletBalance", current + amount);

            WalletTransaction wt = new WalletTransaction();
            wt.setTxId(txRef.getId());
            wt.setUserId(userId);
            wt.setType("topup");
            wt.setAmount(amount);
            wt.setDescription("Top-up by staff");
            wt.setRelatedOrderId("");
            wt.setStaffId(staffId);
            wt.setCreatedAt(Timestamp.now());
            transaction.set(txRef, wt);

            return null;
        });
    }

    /**
     * Staff deduct — atomic Firestore transaction:
     *   1. Subtract amount from student's walletBalance (floor at 0)
     *   2. Write walletTransaction doc (type=deduction)
     */
    public Task<Void> deductWallet(String userId, double amount) {
        String staffId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "unknown";
        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference txRef   = db.collection("walletTransactions").document();

        return db.runTransaction(transaction -> {
            DocumentSnapshot userSnap = transaction.get(userRef);
            double current = userSnap.getDouble("walletBalance");
            double newBalance = Math.max(0, current - amount);

            transaction.update(userRef, "walletBalance", newBalance);

            WalletTransaction wt = new WalletTransaction();
            wt.setTxId(txRef.getId());
            wt.setUserId(userId);
            wt.setType("deduction");
            wt.setAmount(amount);
            wt.setDescription("Manual deduction by staff");
            wt.setRelatedOrderId("");
            wt.setStaffId(staffId);
            wt.setCreatedAt(Timestamp.now());
            transaction.set(txRef, wt);

            return null;
        });
    }

    public ListenerRegistration listenToWalletTransactions(String userId,
                                                           EventListener<QuerySnapshot> listener) {
        return db.collection("walletTransactions")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }
}
