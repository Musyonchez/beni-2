package com.usiu.cafeteria.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    public interface OnAddToCartListener {
        void onAddToCart(MenuItem item);
    }

    private List<MenuItem> items = new ArrayList<>();
    private final OnAddToCartListener listener;

    public MenuAdapter(OnAddToCartListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MenuItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MenuItem item = items.get(position);

        h.tvName.setText(item.getName());
        h.tvDescription.setText(item.getDescription());
        h.tvPrice.setText(String.format("KES %.2f", item.getPrice()));

        Glide.with(h.ivImage.getContext())
                .load(item.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(h.ivImage);

        if (item.isAvailable()) {
            h.chipUnavailable.setVisibility(View.GONE);
            h.btnAddToCart.setEnabled(true);
            h.btnAddToCart.setOnClickListener(v -> listener.onAddToCart(item));
        } else {
            h.chipUnavailable.setVisibility(View.VISIBLE);
            h.btnAddToCart.setEnabled(false);
            h.btnAddToCart.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView    ivImage;
        TextView     tvName, tvDescription, tvPrice;
        Chip         chipUnavailable;
        MaterialButton btnAddToCart;

        ViewHolder(View v) {
            super(v);
            ivImage        = v.findViewById(R.id.iv_item_image);
            tvName         = v.findViewById(R.id.tv_item_name);
            tvDescription  = v.findViewById(R.id.tv_item_description);
            tvPrice        = v.findViewById(R.id.tv_item_price);
            chipUnavailable = v.findViewById(R.id.chip_unavailable);
            btnAddToCart   = v.findViewById(R.id.btn_add_to_cart);
        }
    }
}
