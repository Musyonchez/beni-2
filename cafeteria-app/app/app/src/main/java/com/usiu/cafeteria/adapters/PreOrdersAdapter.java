package com.usiu.cafeteria.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.OrderItem;
import com.usiu.cafeteria.models.PreOrder;

import java.util.ArrayList;
import java.util.List;

public class PreOrdersAdapter extends RecyclerView.Adapter<PreOrdersAdapter.ViewHolder> {

    public interface OnCancelListener {
        void onCancel(PreOrder preOrder);
    }

    private List<PreOrder> preOrders = new ArrayList<>();
    private final OnCancelListener listener;

    public PreOrdersAdapter(OnCancelListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PreOrder> newList) {
        this.preOrders = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_preorder, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        PreOrder po = preOrders.get(position);

        // Slot chip
        String slot = po.getMealSlot();
        h.chipSlot.setText(slot != null
                ? Character.toUpperCase(slot.charAt(0)) + slot.substring(1) : "");

        // Date
        h.tvDate.setText(po.getScheduledDate() != null ? po.getScheduledDate() : "");

        // Status chip
        h.chipStatus.setText(capitalize(po.getStatus()));
        applyStatusColor(h.chipStatus, po.getStatus());

        // Items summary
        h.tvItems.setText(buildSummary(po.getItems()));

        // Total
        h.tvTotal.setText(String.format("KES %.2f", po.getTotalAmount()));

        // Cancel button — only visible when status == "scheduled"
        if ("scheduled".equals(po.getStatus())) {
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnCancel.setOnClickListener(v -> listener.onCancel(po));
        } else {
            h.btnCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return preOrders.size(); }

    private String buildSummary(List<OrderItem> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : items) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(item.getName()).append(" ×").append(item.getQuantity());
        }
        return sb.toString();
    }

    private void applyStatusColor(Chip chip, String status) {
        if (status == null) return;
        int colorRes;
        switch (status) {
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
        Chip     chipSlot, chipStatus;
        TextView tvDate, tvItems, tvTotal;
        com.google.android.material.button.MaterialButton btnCancel;

        ViewHolder(View v) {
            super(v);
            chipSlot   = v.findViewById(R.id.chip_slot);
            tvDate     = v.findViewById(R.id.tv_date);
            chipStatus = v.findViewById(R.id.chip_preorder_status);
            tvItems    = v.findViewById(R.id.tv_preorder_items);
            tvTotal    = v.findViewById(R.id.tv_preorder_total);
            btnCancel  = v.findViewById(R.id.btn_cancel_preorder);
        }
    }
}
