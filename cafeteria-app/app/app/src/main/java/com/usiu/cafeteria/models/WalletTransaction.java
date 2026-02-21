package com.usiu.cafeteria.models;

import com.google.firebase.Timestamp;

public class WalletTransaction {

    private String txId;
    private String userId;
    private String type;
    private double amount;
    private String description;
    private String relatedOrderId;
    private Timestamp createdAt;

    public WalletTransaction() {}

    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(String relatedOrderId) { this.relatedOrderId = relatedOrderId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
