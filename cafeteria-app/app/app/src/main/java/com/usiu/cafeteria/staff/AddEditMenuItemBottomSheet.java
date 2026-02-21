package com.usiu.cafeteria.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.MenuItem;
import com.usiu.cafeteria.repository.FirestoreRepository;

import java.util.HashMap;
import java.util.Map;

public class AddEditMenuItemBottomSheet extends BottomSheetDialogFragment {

    private MenuItem existingItem;

    public void setItem(MenuItem item) {
        this.existingItem = item;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_add_edit_menu_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView          tvTitle       = view.findViewById(R.id.tv_sheet_title);
        TextInputEditText etName        = view.findViewById(R.id.et_item_name);
        TextInputEditText etDesc        = view.findViewById(R.id.et_description);
        TextInputEditText etPrice       = view.findViewById(R.id.et_price);
        TextInputEditText etImageUrl    = view.findViewById(R.id.et_image_url);
        RadioGroup        rgCategory    = view.findViewById(R.id.rg_category);
        MaterialSwitch    switchAvail   = view.findViewById(R.id.switch_item_available);
        MaterialButton    btnSave       = view.findViewById(R.id.btn_save_item);

        boolean isEdit = existingItem != null;
        tvTitle.setText(isEdit ? "Edit Item" : "Add Item");

        if (isEdit) {
            etName.setText(existingItem.getName());
            etDesc.setText(existingItem.getDescription());
            etPrice.setText(String.valueOf(existingItem.getPrice()));
            etImageUrl.setText(existingItem.getImageUrl());
            switchAvail.setChecked(existingItem.isAvailable());
            switch (existingItem.getCategory()) {
                case "breakfast": rgCategory.check(R.id.rb_breakfast); break;
                case "dinner":    rgCategory.check(R.id.rb_dinner);    break;
                default:          rgCategory.check(R.id.rb_lunch);     break;
            }
        } else {
            rgCategory.check(R.id.rb_lunch);
        }

        btnSave.setOnClickListener(v -> {
            String name     = etName.getText()     != null ? etName.getText().toString().trim()     : "";
            String desc     = etDesc.getText()     != null ? etDesc.getText().toString().trim()     : "";
            String priceStr = etPrice.getText()    != null ? etPrice.getText().toString().trim()    : "";
            String imageUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";
            boolean available = switchAvail.isChecked();

            if (name.isEmpty() || priceStr.isEmpty()) return;

            double price;
            try { price = Double.parseDouble(priceStr); }
            catch (NumberFormatException e) { return; }

            int checkedId = rgCategory.getCheckedRadioButtonId();
            String category;
            if      (checkedId == R.id.rb_breakfast) category = "breakfast";
            else if (checkedId == R.id.rb_dinner)    category = "dinner";
            else                                     category = "lunch";

            if (isEdit) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("name",        name);
                updates.put("description", desc);
                updates.put("price",       price);
                updates.put("imageUrl",    imageUrl);
                updates.put("category",    category);
                updates.put("available",   available);
                FirestoreRepository.getInstance()
                        .updateMenuItem(existingItem.getItemId(), updates)
                        .addOnSuccessListener(u -> dismiss())
                        .addOnFailureListener(e ->
                                Snackbar.make(requireView(),
                                        getString(R.string.error_generic),
                                        Snackbar.LENGTH_SHORT).show());
            } else {
                MenuItem item = new MenuItem();
                item.setName(name);
                item.setDescription(desc);
                item.setPrice(price);
                item.setImageUrl(imageUrl);
                item.setCategory(category);
                item.setAvailable(available);
                FirestoreRepository.getInstance().addMenuItem(item)
                        .addOnSuccessListener(u -> dismiss())
                        .addOnFailureListener(e ->
                                Snackbar.make(requireView(),
                                        getString(R.string.error_generic),
                                        Snackbar.LENGTH_SHORT).show());
            }
        });
    }
}
