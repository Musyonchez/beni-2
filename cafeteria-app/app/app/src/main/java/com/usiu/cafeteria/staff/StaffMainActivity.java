package com.usiu.cafeteria.staff;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.usiu.cafeteria.R;
import com.usiu.cafeteria.auth.LoginActivity;
import com.usiu.cafeteria.viewmodels.MenuViewModel;
import com.usiu.cafeteria.viewmodels.OrdersViewModel;

public class StaffMainActivity extends AppCompatActivity {

    public OrdersViewModel ordersViewModel;
    public MenuViewModel   menuViewModel;

    private StaffOrdersFragment  staffOrdersFragment;
    private StaffMenuFragment    staffMenuFragment;
    private StaffWalletFragment  staffWalletFragment;
    private StaffProfileFragment staffProfileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_staff_main);

        ordersViewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        menuViewModel   = new ViewModelProvider(this).get(MenuViewModel.class);

        ordersViewModel.startListeningAllActive();

        staffOrdersFragment  = new StaffOrdersFragment();
        staffMenuFragment    = new StaffMenuFragment();
        staffWalletFragment  = new StaffWalletFragment();
        staffProfileFragment = new StaffProfileFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.staff_fragment_container, staffProfileFragment, "STAFF_PROFILE")
                .add(R.id.staff_fragment_container, staffOrdersFragment,  "STAFF_ORDERS")
                .add(R.id.staff_fragment_container, staffMenuFragment,    "STAFF_MENU")
                .add(R.id.staff_fragment_container, staffWalletFragment,  "STAFF_WALLET")
                .hide(staffOrdersFragment)
                .hide(staffMenuFragment)
                .hide(staffWalletFragment)
                .commit();

        BottomNavigationView bottomNav = findViewById(R.id.staff_bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_staff_orders)  showFragment(staffOrdersFragment);
            else if (id == R.id.nav_staff_menu)    showFragment(staffMenuFragment);
            else if (id == R.id.nav_staff_wallet)  showFragment(staffWalletFragment);
            else if (id == R.id.nav_staff_profile) showFragment(staffProfileFragment);
            return true;
        });
    }

    private void showFragment(Fragment target) {
        getSupportFragmentManager().beginTransaction()
                .hide(staffOrdersFragment)
                .hide(staffMenuFragment)
                .hide(staffWalletFragment)
                .hide(staffProfileFragment)
                .show(target)
                .commit();
    }

    public void navigateToOrders() {
        BottomNavigationView nav = findViewById(R.id.staff_bottom_nav);
        nav.setSelectedItemId(R.id.nav_staff_orders);
    }

    public void navigateToMenu() {
        BottomNavigationView nav = findViewById(R.id.staff_bottom_nav);
        nav.setSelectedItemId(R.id.nav_staff_menu);
    }
}
