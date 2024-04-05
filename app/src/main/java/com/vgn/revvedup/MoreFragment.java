package com.vgn.revvedup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MoreFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference userRef;

    Button modifyProfile, logoutButton;
    TextView usernameTextView, roleTextView;
    ImageView profileImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        //Declaration of XML Layout components
        modifyProfile = view.findViewById(R.id.modifyProfile);
        logoutButton = view.findViewById(R.id.disconnect);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        roleTextView = view.findViewById(R.id.roleTextView);
        profileImageView = view.findViewById(R.id.profileImageView);

        //Firebase instances
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            userRef = database.getReference("users");
            userRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String fnameFromDb = userSnapshot.child("fname").getValue(String.class);
                            String lnameFromDb = userSnapshot.child("lname").getValue(String.class);
                            String roleFromDb = userSnapshot.child("role").getValue(String.class);
                            String profileImageUrl = userSnapshot.child("imagePath").getValue(String.class);
                            usernameTextView.setText(String.format("%s %s", fnameFromDb, lnameFromDb));
                            roleTextView.setText(String.format("%s", roleFromDb));

                            // Load the profile image if available
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                // Use Glide or Picasso library to load the image into ImageView
                                Glide.with(MoreFragment.this).load(profileImageUrl).into(profileImageView);
                            }
                        }
                    } else {
                        usernameTextView.setText(R.string.username);
                        roleTextView.setText(R.string.role);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    usernameTextView.setText(R.string.error);
                    roleTextView.setText(R.string.error);
                }
            });
        }

        modifyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ModifyProfile.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}
