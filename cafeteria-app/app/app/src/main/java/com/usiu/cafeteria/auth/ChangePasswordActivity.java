package com.usiu.cafeteria.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.usiu.cafeteria.R;
import com.usiu.cafeteria.repository.FirestoreRepository;
import com.usiu.cafeteria.staff.StaffMainActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etNewPassword     = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSave           = findViewById(R.id.btn_save_password);

        btnSave.setOnClickListener(v -> savePassword());
    }

    private void savePassword() {
        String newPw  = text(etNewPassword);
        String confirm = text(etConfirmPassword);

        if (newPw.length() < 6) {
            snack("Password must be at least 6 characters");
            return;
        }
        if (!newPw.equals(confirm)) {
            snack("Passwords do not match");
            return;
        }

        btnSave.setEnabled(false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        user.updatePassword(newPw)
                .addOnSuccessListener(unused ->
                        FirestoreRepository.getInstance()
                                .clearFirstLogin(user.getUid())
                                .addOnSuccessListener(v2 -> {
                                    startActivity(new Intent(this, StaffMainActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> proceed()))
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    snack(e.getMessage() != null ? e.getMessage() : "Failed to update password");
                });
    }

    /** Proceed to StaffMainActivity even if Firestore update failed (non-critical). */
    private void proceed() {
        startActivity(new Intent(this, StaffMainActivity.class));
        finish();
    }

    private void snack(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
