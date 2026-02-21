package com.usiu.cafeteria.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.usiu.cafeteria.MainActivity;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.models.User;
import com.usiu.cafeteria.repository.FirestoreRepository;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etStudentId, etEmail, etPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        etName      = findViewById(R.id.et_name);
        etStudentId = findViewById(R.id.et_student_id);
        etEmail     = findViewById(R.id.et_email);
        etPassword  = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> attemptRegister());

        findViewById(R.id.tv_login_link).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name      = text(etName);
        String studentId = text(etStudentId);
        String email     = text(etEmail);
        String password  = text(etPassword);

        if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showSnackbar(getString(R.string.error_generic));
            return;
        }

        btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    createUserDoc(firebaseUser.getUid(), name, studentId, email);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    showSnackbar(getString(R.string.error_generic));
                });
    }

    private void createUserDoc(String uid, String name, String studentId, String email) {
        User user = new User();
        user.setUid(uid);
        user.setName(name);
        user.setStudentId(studentId);
        user.setEmail(email);
        user.setRole("student");
        user.setWalletBalance(0.0);

        FirestoreRepository.getInstance().createUser(user)
                .addOnSuccessListener(unused -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    showSnackbar(getString(R.string.error_generic));
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
