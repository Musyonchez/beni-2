package com.usiu.cafeteria.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface CartCallback {
        void onQuantityChanged(String itemId, int newQuantity);
        void onRemove(String itemId);
    }

    private List<OrderItem> items = new ArrayList<>();
    private final CartCallback callback;

    public CartAdapter(CartCallback callback) {
        this.callback = callback;
    }

    public void submitList(List<OrderItem> newItems) {
        this.items = newItems != null ? new ArrayList<>(newItems) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        OrderItem item = items.get(position);

        h.tvName.setText(item.getName());
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));
        h.tvLineTotal.setText(String.format("KES %.2f", item.getPrice() * item.getQuantity()));

        h.btnPlus.setOnClickListener(v ->
                callback.onQuantityChanged(item.getItemId(), item.getQuantity() + 1));

        h.btnMinus.setOnClickListener(v ->
                callback.onQuantityChanged(item.getItemId(), item.getQuantity() - 1));

        h.btnRemove.setOnClickListener(v ->
                callback.onRemove(item.getItemId()));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvName, tvQuantity, tvLineTotal;
        ImageButton btnPlus, btnMinus, btnRemove;

        ViewHolder(View v) {
            super(v);
            tvName      = v.findViewById(R.id.tv_cart_name);
            tvQuantity  = v.findViewById(R.id.tv_quantity);
            tvLineTotal = v.findViewById(R.id.tv_line_total);
            btnPlus     = v.findViewById(R.id.btn_plus);
            btnMinus    = v.findViewById(R.id.btn_minus);
            btnRemove   = v.findViewById(R.id.btn_remove);
        }
    }
}
