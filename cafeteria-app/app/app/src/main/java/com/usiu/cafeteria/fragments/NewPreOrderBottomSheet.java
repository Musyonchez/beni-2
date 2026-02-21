package com.usiu.cafeteria.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.MenuItem;
import com.usiu.cafeteria.models.OrderItem;
import com.usiu.cafeteria.models.PreOrder;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewPreOrderBottomSheet extends BottomSheetDialogFragment {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private List<MenuItem> availableMenuItems = new ArrayList<>();
    private final List<OrderItem> selectedItems = new ArrayList<>();
    private String selectedDate = "";
    private String studentName  = "";

    private TextView     tvSelectedItems, tvTotal, btnPickDate;
    private RadioGroup   rgMealSlot;
    private SwitchMaterial switchRecurring;
    private ChipGroup    chipGroupDays;

    public void setMenuItems(List<MenuItem> items) {
        if (items != null) availableMenuItems = items;
    }

    public void setStudentName(String name) {
        this.studentName = name != null ? name : "";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_new_preorder_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        rgMealSlot      = view.findViewById(R.id.rg_meal_slot);
        btnPickDate     = view.findViewById(R.id.btn_pick_date);
        tvSelectedItems = view.findViewById(R.id.tv_selected_items);
        tvTotal         = view.findViewById(R.id.tv_preorder_total);
        switchRecurring = view.findViewById(R.id.switch_recurring);
        chipGroupDays   = view.findViewById(R.id.chip_group_days);

        MaterialButton btnSelectItems = view.findViewById(R.id.btn_select_items);
        MaterialButton btnSchedule    = view.findViewById(R.id.btn_schedule);

        // Default date = today
        selectedDate = DATE_FMT.format(Calendar.getInstance().getTime());
        btnPickDate.setText(selectedDate);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSelectItems.setOnClickListener(v -> showItemSelector());
        btnSchedule.setOnClickListener(v -> schedulePreOrder());

        switchRecurring.setOnCheckedChangeListener((btn, checked) ->
                chipGroupDays.setVisibility(checked ? View.VISIBLE : View.GONE));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            cal.set(year, month, day);
            selectedDate = DATE_FMT.format(cal.getTime());
            btnPickDate.setText(selectedDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showItemSelector() {
        if (availableMenuItems.isEmpty()) {
            Toast.makeText(requireContext(), "Menu not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[availableMenuItems.size()];
        boolean[] checked = new boolean[availableMenuItems.size()];
        for (int i = 0; i < availableMenuItems.size(); i++) {
            names[i] = availableMenuItems.get(i).getName()
                    + " — KES " + String.format("%.2f", availableMenuItems.get(i).getPrice());
        }
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.btn_add_to_cart))
                .setMultiChoiceItems(names, checked, (dialog, which, isChecked) ->
                        checked[which] = isChecked)
                .setPositiveButton(getString(R.string.btn_ok), (dialog, which) -> {
                    selectedItems.clear();
                    for (int i = 0; i < availableMenuItems.size(); i++) {
                        if (checked[i]) {
                            MenuItem mi = availableMenuItems.get(i);
                            OrderItem oi = new OrderItem();
                            oi.setItemId(mi.getItemId());
                            oi.setName(mi.getName());
                            oi.setPrice(mi.getPrice());
                            oi.setQuantity(1);
                            selectedItems.add(oi);
                        }
                    }
                    updateSummary();
                })
                .setNegativeButton(getString(R.string.btn_cancel), null)
                .show();
    }

    private void updateSummary() {
        if (selectedItems.isEmpty()) {
            tvSelectedItems.setText("");
            tvTotal.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (OrderItem item : selectedItems) {
            sb.append(item.getName()).append("\n");
            total += item.getPrice() * item.getQuantity();
        }
        tvSelectedItems.setText(sb.toString().trim());
        tvTotal.setText(String.format("Total: KES %.2f", total));
    }

    private void schedulePreOrder() {
        if (selectedItems.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please select at least one item", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        String mealSlot = rgMealSlot.getCheckedRadioButtonId() == R.id.rb_lunch
                ? "lunch" : "dinner";

        boolean recurring = switchRecurring.isChecked();
        List<String> recurringDays = new ArrayList<>();
        if (recurring) {
            int[] chipIds = {R.id.chip_mon, R.id.chip_tue, R.id.chip_wed,
                             R.id.chip_thu, R.id.chip_fri, R.id.chip_sat, R.id.chip_sun};
            String[] dayNames = {"monday", "tuesday", "wednesday",
                                 "thursday", "friday", "saturday", "sunday"};
            for (int i = 0; i < chipIds.length; i++) {
                Chip chip = requireView().findViewById(chipIds[i]);
                if (chip != null && chip.isChecked()) recurringDays.add(dayNames[i]);
            }
        }

        double total = 0;
        for (OrderItem item : selectedItems) total += item.getPrice() * item.getQuantity();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        PreOrder preOrder = new PreOrder();
        preOrder.setUserId(uid);
        preOrder.setStudentName(studentName);
        preOrder.setMealSlot(mealSlot);
        preOrder.setItems(new ArrayList<>(selectedItems));
        preOrder.setTotalAmount(total);
        preOrder.setScheduledDate(selectedDate);
        preOrder.setRecurring(recurring);
        preOrder.setRecurringDays(recurringDays);
        preOrder.setStatus("scheduled");
        preOrder.setCreatedAt(Timestamp.now());

        FirestoreRepository.getInstance().createPreOrder(preOrder)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(requireContext(),
                            getString(R.string.msg_preorder_scheduled),
                            Toast.LENGTH_LONG).show();
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                getString(R.string.error_generic),
                                Toast.LENGTH_SHORT).show());
    }
}
