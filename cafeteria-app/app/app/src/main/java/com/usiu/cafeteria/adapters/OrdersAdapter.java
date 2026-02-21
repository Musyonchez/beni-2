package com.usiu.cafeteria.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.models.OrderItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    private List<Order> orders = new ArrayList<>();
    private final List<Boolean> expanded = new ArrayList<>();

    public void submitList(List<Order> newOrders) {
        this.orders = newOrders != null ? new ArrayList<>(newOrders) : new ArrayList<>();
        expanded.clear();
        for (int i = 0; i < this.orders.size(); i++) expanded.add(false);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Order order = orders.get(position);

        // Order ID snippet
        String id = order.getOrderId();
        h.tvOrderId.setText("Order #" + (id != null && id.length() > 6
                ? id.substring(0, 6).toUpperCase() : id));

        // Status chip
        h.chipStatus.setText(capitalize(order.getStatus()));
        applyStatusColor(h.chipStatus, order.getStatus());

        // Items summary (collapsed)
        h.tvItemsSummary.setText(buildItemsSummary(order.getItems()));

        // Full items (expanded)
        h.tvItemsFull.setText(buildItemsFull(order.getItems()));
        boolean isExpanded = position < expanded.size() && expanded.get(position);
        h.tvItemsSummary.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        h.tvItemsFull.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Total
        h.tvTotal.setText(String.format("KES %.2f", order.getTotalAmount()));

        // Time
        if (order.getCreatedAt() != null) {
            h.tvTime.setText(DATE_FMT.format(order.getCreatedAt().toDate()));
        }

        // Expand/collapse on tap
        h.itemView.setOnClickListener(v -> {
            if (position < expanded.size()) {
                expanded.set(position, !expanded.get(position));
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() { return orders.size(); }

    private String buildItemsSummary(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(item.getName()).append(" ×").append(item.getQuantity());
        }
        return sb.toString();
    }

    private String buildItemsFull(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            sb.append(item.getName())
              .append(" ×").append(item.getQuantity())
              .append("  KES ").append(String.format("%.2f", item.getPrice() * item.getQuantity()))
              .append("\n");
        }
        return sb.toString().trim();
    }

    private void applyStatusColor(Chip chip, String status) {
        if (status == null) return;
        int colorRes;
        switch (status) {
            case "pending":   colorRes = R.color.amber_100; break;
            case "preparing": colorRes = R.color.blue_100;  break;
            case "ready":     colorRes = R.color.green_100; break;
            case "confirmed": colorRes = R.color.green_100; break;
            case "cancelled": colorRes = R.color.red_100;   break;
            default:          colorRes = R.color.grey_100;  break;
        }
        chip.setChipBackgroundColorResource(colorRes);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvItemsSummary, tvItemsFull, tvTotal, tvTime;
        Chip     chipStatus;

        ViewHolder(View v) {
            super(v);
            tvOrderId      = v.findViewById(R.id.tv_order_id);
            chipStatus     = v.findViewById(R.id.chip_status);
            tvItemsSummary = v.findViewById(R.id.tv_items_summary);
            tvItemsFull    = v.findViewById(R.id.tv_items_full);
            tvTotal        = v.findViewById(R.id.tv_total);
            tvTime         = v.findViewById(R.id.tv_time);
        }
    }
}
