package com.vgn.revvedup;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventsFragment extends Fragment {

    // TODO: If the user is "admin": display the events from the database
    // TODO: If the user is "organizator": display his events
    // TODO: If the user is "participant": display the events based on the recommandation algorithm

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private List<Event> events;

    private EventsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        //Initialize Firebase Database and Event List
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        events = new ArrayList<>();

        RecyclerView eventRecyclerView = view.findViewById(R.id.eventRecyclerView);
        SearchView searchView = view.findViewById(R.id.searchView);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (user != null) {
            String userEmail = user.getEmail();
            DatabaseReference usersRef = database.getReference("users");
            usersRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String roleFromDb = userSnapshot.child("role").getValue(String.class);
                            // Verificare pentru a preveni NullPointerException
                            if (roleFromDb != null) {
                                // Inițializare adapter și setare RecyclerView
                                adapter = new EventsAdapter(events, roleFromDb);
                                eventRecyclerView.setAdapter(adapter);

                                // Switch pe rolul utilizatorului pentru a afișa evenimentele corespunzătoare
                                switch (roleFromDb) {
                                    case "Participant":
                                    case "Admin":
                                        fetchEvents();
                                        break;
                                    case "Organizator":
                                        fetchEventAdminEvents(userEmail);
                                        break;
                                }

                                adapter.setOnItemClickListener(new EventsAdapter.OnItemClickListener() {
                                    @Override
                                    public void onDetailsClick(Event event) {
                                        // Deschideți EventDetailsActivity și trimiteți detalii despre eveniment
                                        Intent intent = new Intent(getActivity(), EventDetails.class);
                                        intent.putExtra("name", event.getName());
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onDeleteClick(Event event) {
                                        DatabaseReference carsRef = database.getReference("events");
                                        String eventName = event.getName(); // presupunând că fiecare mașină are un cheie unic în baza de date

                                        // Șterge mașina din baza de date
                                        carsRef.child(eventName).removeValue().addOnSuccessListener(aVoid -> {
                                            // Ștergere reușită
                                            Toast.makeText(getActivity(), "Evenimentul a fost șters cu succes", Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(e -> {
                                            // Întâmpinare erori în timpul ștergerii
                                            Toast.makeText(getActivity(), "Eroare la ștergerea evenimentului : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onLikeEvent(Event event) {

                                    }

                                    @Override
                                    public void onAddCarToEvent(Event event) {

                                    }

                                });
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String userEmail = user.getEmail();
                if (userEmail != null) {
                    DatabaseReference usersRef = database.getReference("users");
                    usersRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    String roleFromDb = userSnapshot.child("role").getValue(String.class);
                                    switch (Objects.requireNonNull(roleFromDb)) {
                                        case "Admin":
                                        case "Participant":
                                            fetchEvents(newText);
                                            break;
                                        case "Organizator":
                                            fetchEventAdminEvents(userEmail, newText);
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
                return false;
            }
        });

        return view;
    }

    private void fetchEvents() {
        DatabaseReference eventsRef = database.getReference("events");

        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Event event = childSnapshot.getValue(Event.class);
                    if (event != null) {
                        events.add(event);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("EventFragment", "Failed to fetch events:", error.toException());
            }
        });
    }

    private void fetchEvents(String searchTerm) {
        DatabaseReference eventsRef = database.getReference("events");

        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Event event = childSnapshot.getValue(Event.class);
                    if (event != null && event.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                        events.add(event);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("EventFragment", "Failed to fetch events:", error.toException());
            }
        });
    }

    private void fetchEventAdminEvents(String userEmail) {
        DatabaseReference carsRef = database.getReference("events");

        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null && event.getEventOwner().equals(userEmail)) {
                        events.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("CarsFragment", "Failed to fetch user cars:", error.toException());
            }
        });
    }

    private void fetchEventAdminEvents(String userEmail, String searchTerm) {
        DatabaseReference carsRef = database.getReference("events");

        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                events.clear();
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null && event.getEventOwner().equals(userEmail) && (event.getName().toLowerCase().contains(searchTerm.toLowerCase()) || event.getLocation().toLowerCase().contains(searchTerm.toLowerCase()))) {
                        events.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("CarsFragment", "Failed to fetch user cars:", error.toException());
            }
        });
    }


}