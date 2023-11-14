package com.vgn.revvedup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.vgn.revvedup.databinding.ActivityMainAdminBinding;

public class MainActivityUser extends AppCompatActivity {

    ActivityMainAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new AdminHomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new UserHomeFragment());
            } else if (item.getItemId() == R.id.event) {
                replaceFragment(new UserEventFragment());
            } else if (item.getItemId() == R.id.favorites) {
                replaceFragment(new UserFavoritesFragment());
            } else if (item.getItemId() == R.id.more) {
                replaceFragment(new MoreFragment());
            }
            return true;
        });

    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}