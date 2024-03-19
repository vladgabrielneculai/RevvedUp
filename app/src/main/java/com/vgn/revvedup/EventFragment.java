package com.vgn.revvedup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

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

public class EventFragment extends Fragment {

    // TODO: If the user is "admin": display the events from the database
    // TODO: If the user is "organizator": display his events
    // TODO: If the user is "participant": display the events based on the recommandation algorithm

    private FirebaseDatabase database;
    private List<Event> events;
    private EventAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        //Initialize Firebase Database and Event List
        database = FirebaseDatabase.getInstance();
        events = new ArrayList<>();

        RecyclerView eventRecyclerView = view.findViewById(R.id.eventRecyclerView);
        SearchView searchView = view.findViewById(R.id.searchView);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(events);
        eventRecyclerView.setAdapter(adapter);

        fetchEvents();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fetchEvents(newText);
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

}