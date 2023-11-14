package com.vgn.revvedup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.vgn.revvedup.databinding.ActivityMainAdminBinding;

public class MainActivityEventAdmin extends AppCompatActivity {

    ActivityMainAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new AdminHomeFragment());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new EventAdminHomeFragment());
            } else if (item.getItemId() == R.id.event) {
                replaceFragment(new EventAdminEventFragment());
            } else if (item.getItemId() == R.id.car_approval_list) {
                replaceFragment(new EventAdminCarsFragment());
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