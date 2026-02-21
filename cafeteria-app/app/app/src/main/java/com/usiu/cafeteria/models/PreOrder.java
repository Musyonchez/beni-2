package com.usiu.cafeteria.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class PreOrder {

    private String preOrderId;
    private String userId;
    private String studentName;
    private String mealSlot;
    private List<OrderItem> items;
    private double totalAmount;
    private String scheduledDate;
    private boolean recurring;
    private List<String> recurringDays;
    private String status;
    private Timestamp createdAt;

    public PreOrder() {}

    public String getPreOrderId() { return preOrderId; }
    public void setPreOrderId(String preOrderId) { this.preOrderId = preOrderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getMealSlot() { return mealSlot; }
    public void setMealSlot(String mealSlot) { this.mealSlot = mealSlot; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }

    public List<String> getRecurringDays() { return recurringDays; }
    public void setRecurringDays(List<String> recurringDays) { this.recurringDays = recurringDays; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
