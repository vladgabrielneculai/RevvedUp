package com.vgn.revvedup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private FirebaseDatabase database;
    private List<User> users; // List to store user data
    private UserAdapter adapter; // Adapter to display users in RecyclerView

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Initialize Firebase Database and User List
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        // Initialize RecyclerView and Adapter
        RecyclerView userRecyclerView = view.findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(users);
        userRecyclerView.setAdapter(adapter);

        // Fetch users from Firebase Realtime Database
        fetchUsers();

        return view;
    }

    private void fetchUsers() {
        DatabaseReference usersRef = database.getReference("utilizatori"); // Reference to "utilizatori" node

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear(); // Clear existing user data
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    // Extract user data from each child node and add to the list
                    User user = childSnapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }
                // Notify adapter about data change to update the view
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("UsersFragment", "Failed to fetch users:", error.toException());
            }
        });
    }
}