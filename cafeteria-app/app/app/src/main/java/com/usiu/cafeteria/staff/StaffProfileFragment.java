package com.usiu.cafeteria.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.auth.LoginActivity;
import com.usiu.cafeteria.repository.FirestoreRepository;

public class StaffProfileFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_staff_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvName  = view.findViewById(R.id.tv_staff_name);
        TextView tvEmail = view.findViewById(R.id.tv_staff_email);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            // load name from Firestore
            FirestoreRepository.getInstance().getUser(user.getUid())
                    .addOnSuccessListener(snap -> {
                        if (snap != null && snap.exists()) {
                            String name = snap.getString("name");
                            tvName.setText(name != null ? name : user.getEmail());
                        }
                    });
        }

        view.findViewById(R.id.btn_staff_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}
