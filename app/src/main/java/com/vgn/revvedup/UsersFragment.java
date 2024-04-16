package com.vgn.revvedup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

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
    private List<User> users;
    private UsersAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Initialize Firebase Database and User List
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        RecyclerView userRecyclerView = view.findViewById(R.id.userRecyclerView);
        SearchView searchView = view.findViewById(R.id.searchView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsersAdapter(users);
        userRecyclerView.setAdapter(adapter);

        fetchUsers();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fetchUsers(newText);
                return false;
            }
        });

        adapter.setOnItemClickListener(user -> {
            DatabaseReference usersRef = database.getReference("users");
            String username = user.getUsername(); // presupunând că fiecare mașină are un cheie unic în baza de date

            // Șterge mașina din baza de date
            usersRef.child(username).removeValue().addOnSuccessListener(aVoid -> {
                // Ștergere reușită
                Toast.makeText(getActivity(), "Utilizatorul a fost șters cu succes", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Întâmpinare erori în timpul ștergerii
                Toast.makeText(getActivity(), "Eroare la ștergerea utilizatorului: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        return view;
    }

    private void fetchUsers() {
        DatabaseReference usersRef = database.getReference("users"); // Reference to "users" node

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    User user = childSnapshot.getValue(User.class);
                    if (user != null) {
                        users.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("UsersFragment", "Failed to fetch users:", error.toException());
            }
        });
    }

    private void fetchUsers(String searchTerm) {
        DatabaseReference usersRef = database.getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    User user = childSnapshot.getValue(User.class);
                    if (user != null) {
                        if (user.getFname().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                user.getRole().toLowerCase().contains(searchTerm.toLowerCase()) || user.getLname().toLowerCase().contains(searchTerm.toLowerCase())) {
                            users.add(user);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("UsersFragment", "Failed to fetch users:", error.toException());
            }
        });
    }
}