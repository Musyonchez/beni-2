package com.usiu.cafeteria.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.WalletTransaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletTransactionAdapter
        extends RecyclerView.Adapter<WalletTransactionAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    private List<WalletTransaction> transactions = new ArrayList<>();

    public void submitList(List<WalletTransaction> newList) {
        this.transactions = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wallet_tx, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        WalletTransaction tx = transactions.get(position);

        boolean isTopUp = "topup".equals(tx.getType());

        h.ivIcon.setImageResource(isTopUp ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
        h.tvDescription.setText(tx.getDescription());
        h.tvAmount.setText(String.format("%sKES %.2f",
                isTopUp ? "+" : "-", tx.getAmount()));
        h.tvAmount.setTextColor(h.tvAmount.getContext().getColor(
                isTopUp ? R.color.topup_green : R.color.deduction_red));

        if (tx.getCreatedAt() != null) {
            h.tvDate.setText(DATE_FMT.format(tx.getCreatedAt().toDate()));
        }
    }

    @Override
    public int getItemCount() { return transactions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView  tvDescription, tvDate, tvAmount;

        ViewHolder(View v) {
            super(v);
            ivIcon        = v.findViewById(R.id.iv_tx_icon);
            tvDescription = v.findViewById(R.id.tv_tx_description);
            tvDate        = v.findViewById(R.id.tv_tx_date);
            tvAmount      = v.findViewById(R.id.tv_tx_amount);
        }
    }
}
