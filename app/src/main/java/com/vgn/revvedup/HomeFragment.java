package com.vgn.revvedup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    // TODO: If the user is "admin": display charts with statistics about no of events, no of cars, no of users, etc (think more about it)
    // TODO: If the user is "participant": display a chart with no of accepts and no of rejects
    // TODO: If the user is "organizator": display a chart with no of likes and dislikes of the event, no of people that registered their car, etc (think more about it)

    private TextView usernameTextView;

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Declaration of XML Layout components
        usernameTextView = view.findViewById(R.id.username);

        //Firebase instances
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference userRef = database.getReference("utilizatori");
            userRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String fnameFromDb = userSnapshot.child("fname").getValue(String.class);
                            usernameTextView.setText(String.format("Salut, %s!", fnameFromDb));
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
}