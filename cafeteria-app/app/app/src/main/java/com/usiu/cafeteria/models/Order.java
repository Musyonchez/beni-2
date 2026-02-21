package com.usiu.cafeteria.models;

import com.google.firebase.Timestamp;

import java.util.List;

public class Order {

    private String orderId;
    private String userId;
    private String studentName;
    private List<OrderItem> items;
    private String status;
    private String paymentMethod;
    private double totalAmount;
    private int estimatedWaitMin;
    private Timestamp createdAt;

    public Order() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getEstimatedWaitMin() { return estimatedWaitMin; }
    public void setEstimatedWaitMin(int estimatedWaitMin) { this.estimatedWaitMin = estimatedWaitMin; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
