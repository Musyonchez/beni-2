package com.usiu.cafeteria.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.repository.FirestoreRepository;
import com.usiu.cafeteria.staff.StaffMainActivity;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already signed in, skip login screen
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            navigateByRole(auth.getCurrentUser().getUid());
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin   = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tv_register_link).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email    = text(etEmail);
        String password = text(etPassword);

        if (email.isEmpty() || password.isEmpty()) {
            showSnackbar(getString(R.string.error_generic));
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    navigateByRole(uid);
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    showSnackbar(getString(R.string.error_generic));
                });
    }

    private void navigateByRole(String uid) {
        FirestoreRepository.getInstance().getUser(uid)
                .addOnSuccessListener(snap -> {
                    String role = snap.getString("role");
                    if ("staff".equals(role)) {
                        startActivity(new Intent(this, StaffMainActivity.class));
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Fallback — send to student nav
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message,
                Snackbar.LENGTH_LONG).show();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
