package com.vgn.revvedup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class HomeFragment extends Fragment {

    private TextView usernameTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        usernameTextView = view.findViewById(R.id.username);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference userRef = database.getReference("users");
            userRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String fnameFromDb = userSnapshot.child("fname").getValue(String.class);
                            usernameTextView.setText(String.format("Salut, %s!", fnameFromDb));

                            String role = userSnapshot.child("role").getValue(String.class);
                            loadUserSpecificFragment(Objects.requireNonNull(role));
                        }
                    } else {
                        usernameTextView.setText(R.string.username);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    usernameTextView.setText(R.string.error);
                }
            });
        }

        return view;
    }

    private void loadUserSpecificFragment(String role) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment = null;

        switch (role) {
            case "Admin":
                // Încarcă conținut specific pentru admin în FragmentContainerView
                fragment = new AdminHomeFragment(); // presupunând că există un AdminFragment definit
                break;
            case "Organizator":
                // Încarcă conținut specific pentru event admin în FragmentContainerView
                fragment = new EventAdminHomeFragment(); // presupunând că există un EventAdminFragment definit
                break;
            case "Participant":
                // Încarcă conținut specific pentru participant în FragmentContainerView
                fragment = new ParticipantHomeFragment(); // presupunând că există un ParticipantFragment definit
                break;
        }

        if (fragment != null) {
            fragmentTransaction.replace(R.id.user_specific_content_container, fragment);
            fragmentTransaction.commit();
        }
    }
}