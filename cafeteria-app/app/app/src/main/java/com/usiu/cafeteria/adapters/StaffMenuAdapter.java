package com.usiu.cafeteria.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.MenuItem;

import java.util.Locale;

public class StaffMenuAdapter extends ListAdapter<MenuItem, StaffMenuAdapter.VH> {

    public interface StaffMenuCallback {
        void onAvailabilityChanged(String itemId, boolean available);
        void onEdit(MenuItem item);
    }

    private final StaffMenuCallback callback;

    private static final DiffUtil.ItemCallback<MenuItem> DIFF = new DiffUtil.ItemCallback<MenuItem>() {
        @Override public boolean areItemsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) {
            return a.getItemId().equals(b.getItemId());
        }
        @Override public boolean areContentsTheSame(@NonNull MenuItem a, @NonNull MenuItem b) {
            return a.isAvailable() == b.isAvailable()
                    && a.getPrice() == b.getPrice()
                    && a.getName().equals(b.getName());
        }
    };

    public StaffMenuAdapter(StaffMenuCallback callback) {
        super(DIFF);
        this.callback = callback;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_menu, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvCategory, tvPrice;
        final MaterialSwitch switchAvailable;
        final MaterialButton btnEdit;

        VH(View v) {
            super(v);
            tvName          = v.findViewById(R.id.tv_staff_item_name);
            tvCategory      = v.findViewById(R.id.tv_staff_item_category);
            tvPrice         = v.findViewById(R.id.tv_staff_item_price);
            switchAvailable = v.findViewById(R.id.switch_available);
            btnEdit         = v.findViewById(R.id.btn_edit_item);
        }

        void bind(MenuItem item) {
            tvName.setText(item.getName());
            tvCategory.setText(item.getCategory());
            tvPrice.setText(String.format(Locale.getDefault(), "KES %.2f", item.getPrice()));

            // Detach listener before setting state to avoid spurious callbacks
            switchAvailable.setOnCheckedChangeListener(null);
            switchAvailable.setChecked(item.isAvailable());
            switchAvailable.setOnCheckedChangeListener((btn, checked) ->
                    callback.onAvailabilityChanged(item.getItemId(), checked));

            btnEdit.setOnClickListener(v -> callback.onEdit(item));
        }
    }
}
