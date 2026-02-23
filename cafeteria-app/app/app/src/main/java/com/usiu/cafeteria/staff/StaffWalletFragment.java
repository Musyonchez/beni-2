package com.usiu.cafeteria.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.repository.FirestoreRepository;

public class StaffWalletFragment extends Fragment {

    private String foundUserId;
    private double foundBalance;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextInputEditText etStudentId = view.findViewById(R.id.et_student_id);
        MaterialButton    btnFind     = view.findViewById(R.id.btn_find_student);
        View              cardInfo    = view.findViewById(R.id.card_student_info);
        TextView          tvName      = view.findViewById(R.id.tv_found_name);
        TextView          tvBalance   = view.findViewById(R.id.tv_found_balance);
        View              tilAmount   = view.findViewById(R.id.til_amount);
        TextInputEditText etAmount    = view.findViewById(R.id.et_amount);
        MaterialButton    btnTopUp    = view.findViewById(R.id.btn_confirm_topup);
        MaterialButton    btnDeduct   = view.findViewById(R.id.btn_confirm_deduct);

        btnFind.setOnClickListener(v -> {
            String studentId = etStudentId.getText() != null
                    ? etStudentId.getText().toString().trim() : "";
            if (studentId.isEmpty()) return;

            FirestoreRepository.getInstance().getUserByStudentId(studentId)
                    .addOnSuccessListener(snap -> {
                        if (snap == null || snap.isEmpty()) {
                            Snackbar.make(requireView(),
                                    getString(R.string.msg_student_not_found),
                                    Snackbar.LENGTH_SHORT).show();
                            hideStudentUI(cardInfo, tilAmount, btnTopUp, btnDeduct);
                            foundUserId = null;
                            return;
                        }

                        com.google.firebase.firestore.DocumentSnapshot doc =
                                snap.getDocuments().get(0);
                        foundUserId   = doc.getId();
                        String name   = doc.getString("name");
                        Double bal    = doc.getDouble("walletBalance");
                        foundBalance  = bal != null ? bal : 0.0;

                        tvName.setText(name);
                        tvBalance.setText(String.format("Balance: KES %.2f", foundBalance));

                        cardInfo.setVisibility(View.VISIBLE);
                        tilAmount.setVisibility(View.VISIBLE);
                        btnTopUp.setVisibility(View.VISIBLE);
                        btnDeduct.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e ->
                            Snackbar.make(requireView(),
                                    getString(R.string.error_generic),
                                    Snackbar.LENGTH_SHORT).show());
        });

        btnTopUp.setOnClickListener(v -> {
            if (foundUserId == null) return;
            String amtStr = etAmount.getText() != null
                    ? etAmount.getText().toString().trim() : "";
            if (amtStr.isEmpty()) return;

            double amount;
            try { amount = Double.parseDouble(amtStr); }
            catch (NumberFormatException e) { return; }
            if (amount <= 0) return;

            final double topUpAmount = amount;
            FirestoreRepository.getInstance().topUpWallet(foundUserId, topUpAmount)
                    .addOnSuccessListener(unused -> {
                        foundBalance += topUpAmount;
                        tvBalance.setText(String.format("Balance: KES %.2f", foundBalance));
                        etAmount.setText("");
                        Snackbar.make(requireView(),
                                getString(R.string.msg_topup_success, foundBalance),
                                Snackbar.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e ->
                            Snackbar.make(requireView(),
                                    getString(R.string.error_generic),
                                    Snackbar.LENGTH_SHORT).show());
        });

        btnDeduct.setOnClickListener(v -> {
            if (foundUserId == null) return;
            String amtStr = etAmount.getText() != null
                    ? etAmount.getText().toString().trim() : "";
            if (amtStr.isEmpty()) return;

            double amount;
            try { amount = Double.parseDouble(amtStr); }
            catch (NumberFormatException e) { return; }
            if (amount <= 0) return;

            final double deductAmount = amount;
            FirestoreRepository.getInstance().deductWallet(foundUserId, deductAmount)
                    .addOnSuccessListener(unused -> {
                        foundBalance = Math.max(0, foundBalance - deductAmount);
                        tvBalance.setText(String.format("Balance: KES %.2f", foundBalance));
                        etAmount.setText("");
                        Snackbar.make(requireView(),
                                getString(R.string.msg_deduct_success, foundBalance),
                                Snackbar.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e ->
                            Snackbar.make(requireView(),
                                    getString(R.string.error_generic),
                                    Snackbar.LENGTH_SHORT).show());
        });
    }

    private void hideStudentUI(View cardInfo, View tilAmount, View btnTopUp, View btnDeduct) {
        cardInfo.setVisibility(View.GONE);
        tilAmount.setVisibility(View.GONE);
        btnTopUp.setVisibility(View.GONE);
        btnDeduct.setVisibility(View.GONE);
    }
}
