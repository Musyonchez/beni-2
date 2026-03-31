package com.usiu.cafeteria;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import com.usiu.cafeteria.auth.LoginActivity;
import com.usiu.cafeteria.fragments.CartFragment;
import com.usiu.cafeteria.fragments.MenuFragment;
import com.usiu.cafeteria.fragments.OrdersFragment;
import com.usiu.cafeteria.fragments.PreOrdersFragment;
import com.usiu.cafeteria.fragments.ProfileWalletFragment;
import com.usiu.cafeteria.fragments.WalletTransactionsFragment;
import com.usiu.cafeteria.viewmodels.CartViewModel;
import com.usiu.cafeteria.viewmodels.OrdersViewModel;
import com.usiu.cafeteria.viewmodels.WalletViewModel;

public class MainActivity extends AppCompatActivity {

    // Shared ViewModels — activity scope so all fragments share the same instance
    public CartViewModel   cartViewModel;
    public WalletViewModel walletViewModel;
    public OrdersViewModel ordersViewModel;

    private MenuFragment               menuFragment;
    private CartFragment               cartFragment;
    private OrdersFragment             ordersFragment;
    private PreOrdersFragment          preOrdersFragment;
    private ProfileWalletFragment      profileWalletFragment;
    private WalletTransactionsFragment walletTxFragment;

    private Fragment activeFragment;
    private boolean  walletTxVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Guard: require login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialise shared ViewModels
        cartViewModel   = new ViewModelProvider(this).get(CartViewModel.class);
        walletViewModel = new ViewModelProvider(this).get(WalletViewModel.class);
        ordersViewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        ordersViewModel.startListeningMyOrders();

        // Create fragments once
        menuFragment          = new MenuFragment();
        cartFragment          = new CartFragment();
        ordersFragment        = new OrdersFragment();
        preOrdersFragment     = new PreOrdersFragment();
        profileWalletFragment = new ProfileWalletFragment();
        walletTxFragment      = new WalletTransactionsFragment();

        // Add all fragments; show Dashboard (profile) initially
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, profileWalletFragment, "profile")
                    .add(R.id.fragment_container, walletTxFragment,      "wallet_tx")
                    .add(R.id.fragment_container, menuFragment,          "menu")
                    .add(R.id.fragment_container, cartFragment,          "cart")
                    .add(R.id.fragment_container, ordersFragment,        "orders")
                    .add(R.id.fragment_container, preOrdersFragment,     "preorders")
                    .hide(walletTxFragment)
                    .hide(menuFragment)
                    .hide(cartFragment)
                    .hide(ordersFragment)
                    .hide(preOrdersFragment)
                    .commit();
            activeFragment = profileWalletFragment;
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_menu)      showFragment(menuFragment);
            else if (id == R.id.nav_cart)      showFragment(cartFragment);
            else if (id == R.id.nav_orders)    showFragment(ordersFragment);
            else if (id == R.id.nav_preorders) showFragment(preOrdersFragment);
            else if (id == R.id.nav_profile)   showFragment(profileWalletFragment);
            return true;
        });
    }

    private void showFragment(Fragment target) {
        if (target == activeFragment && !walletTxVisible) return;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(activeFragment);
        if (walletTxVisible) ft.hide(walletTxFragment);
        ft.show(target);
        ft.commit();
        activeFragment = target;
        walletTxVisible = false;
    }

    /** Open the wallet transactions sub-page (stays within Dashboard tab). */
    public void showWalletTransactions() {
        getSupportFragmentManager().beginTransaction()
                .hide(profileWalletFragment)
                .show(walletTxFragment)
                .commit();
        walletTxVisible = true;
    }

    /** Return from wallet transactions back to dashboard. */
    public void showDashboard() {
        getSupportFragmentManager().beginTransaction()
                .hide(walletTxFragment)
                .show(profileWalletFragment)
                .commit();
        walletTxVisible = false;
    }

    @Override
    public void onBackPressed() {
        if (walletTxVisible) {
            showDashboard();
            return;
        }
        super.onBackPressed();
    }

    /** Called by CartFragment after a successful order — navigate to Orders tab. */
    public void navigateToOrders() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_orders);
    }

    /** Called by CartFragment to navigate to Menu tab. */
    public void navigateToMenu() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_menu);
    }

    /** Called by Dashboard to jump to Orders > History tab. */
    public void navigateToOrderHistory() {
        ordersFragment.showHistoryTab();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_orders);
    }

    /** Called by Dashboard to jump to Pre-orders tab. */
    public void navigateToPreOrders() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_preorders);
    }
}
