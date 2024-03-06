package com.vgn.revvedup;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vgn.revvedup.databinding.ActivityMainBinding;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        //Firebase instances
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //Verification of the user role in order to display the menu accordingly
        if (user != null) {
            DatabaseReference userRef = database.getReference("utilizatori");
            userRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String roleFromDb = userSnapshot.child("role").getValue(String.class);

                            switch (Objects.requireNonNull(roleFromDb)) {
                                case "Admin":
                                    binding.bottomNavigationView.inflateMenu(R.menu.bottom_menu_admin);
                                    binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                                        if (item.getItemId() == R.id.home) {
                                            replaceFragment(new HomeFragment());
                                        } else if (item.getItemId() == R.id.event) {
                                            replaceFragment(new EventFragment());
                                        } else if (item.getItemId() == R.id.more) {
                                            replaceFragment(new MoreFragment());
                                        } else if (item.getItemId() == R.id.cars) {
                                            replaceFragment(new CarsFragment());
                                        } else if (item.getItemId() == R.id.users) {
                                            replaceFragment(new UsersFragment());
                                        }
                                        return true;
                                    });
                                    break;
                                case "Organizator":
                                    binding.bottomNavigationView.inflateMenu(R.menu.bottom_menu_event_admin);
                                    binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                                        if (item.getItemId() == R.id.home) {
                                            replaceFragment(new HomeFragment());
                                        } else if (item.getItemId() == R.id.event) {
                                            replaceFragment(new EventFragment());
                                        } else if (item.getItemId() == R.id.add) {
                                            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                                            startActivity(intent);
                                        } else if (item.getItemId() == R.id.car_approval_list) {
                                            replaceFragment(new CarsFragment());
                                        } else if (item.getItemId() == R.id.more) {
                                            replaceFragment(new MoreFragment());
                                        }
                                        return true;
                                    });
                                    break;
                                case "Participant":
                                    binding.bottomNavigationView.inflateMenu(R.menu.bottom_menu_participant);
                                    binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                                        if (item.getItemId() == R.id.home) {
                                            replaceFragment(new HomeFragment());
                                        } else if (item.getItemId() == R.id.event) {
                                            replaceFragment(new EventFragment());
                                        } else if (item.getItemId() == R.id.add) {
                                            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                                            startActivity(intent);
                                        } else if (item.getItemId() == R.id.my_car) {
                                            replaceFragment(new CarsFragment());
                                        } else if (item.getItemId() == R.id.more) {
                                            replaceFragment(new MoreFragment());
                                        }
                                        return true;
                                    });
                                    break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}