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
import com.google.android.material.chip.Chip;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.Order;
import com.usiu.cafeteria.models.OrderItem;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class StaffOrdersAdapter extends ListAdapter<Order, StaffOrdersAdapter.VH> {

    public interface OrderActionCallback {
        void onAction(Order order, String newStatus);
    }

    private final OrderActionCallback callback;

    private static final DiffUtil.ItemCallback<Order> DIFF = new DiffUtil.ItemCallback<Order>() {
        @Override public boolean areItemsTheSame(@NonNull Order a, @NonNull Order b) {
            return a.getOrderId().equals(b.getOrderId());
        }
        @Override public boolean areContentsTheSame(@NonNull Order a, @NonNull Order b) {
            return a.getStatus().equals(b.getStatus());
        }
    };

    public StaffOrdersAdapter(OrderActionCallback callback) {
        super(DIFF);
        this.callback = callback;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_staff_order, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position));
    }

    class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvItems, tvTotal, tvTime;
        final Chip chipPayment;
        final MaterialButton btnAction;
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        VH(View v) {
            super(v);
            tvName      = v.findViewById(R.id.tv_student_name);
            tvItems     = v.findViewById(R.id.tv_order_items);
            tvTotal     = v.findViewById(R.id.tv_order_total);
            tvTime      = v.findViewById(R.id.tv_order_time);
            chipPayment = v.findViewById(R.id.chip_payment);
            btnAction   = v.findViewById(R.id.btn_order_action);
        }

        void bind(Order order) {
            tvName.setText(order.getStudentName());

            StringBuilder sb = new StringBuilder();
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(item.getQuantity()).append("\u00d7 ").append(item.getName());
                }
            }
            tvItems.setText(sb.toString());

            tvTotal.setText(String.format(Locale.getDefault(), "KES %.2f", order.getTotalAmount()));

            if (order.getCreatedAt() != null) {
                tvTime.setText(sdf.format(order.getCreatedAt().toDate()));
            }

            boolean isCash = "cash".equals(order.getPaymentMethod());
            chipPayment.setText(isCash ? "Cash" : "Wallet");
            chipPayment.setChipBackgroundColorResource(
                    isCash ? R.color.amber_100 : R.color.blue_100);

            bindActionButton(order);
        }

        void bindActionButton(Order order) {
            btnAction.setVisibility(View.GONE);
            switch (order.getStatus()) {
                case "pending":
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText(R.string.btn_start_preparing);
                    btnAction.setOnClickListener(v -> callback.onAction(order, "preparing"));
                    break;
                case "preparing":
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText(R.string.btn_mark_ready);
                    btnAction.setOnClickListener(v -> callback.onAction(order, "ready"));
                    break;
                case "ready":
                    if ("cash".equals(order.getPaymentMethod())) {
                        btnAction.setVisibility(View.VISIBLE);
                        btnAction.setText(R.string.btn_mark_collected);
                        btnAction.setOnClickListener(v -> callback.onAction(order, "collected"));
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
