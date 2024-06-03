package com.vgn.revvedup;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
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
                                        fetchRecommendedEvents();
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
                                        DatabaseReference eventsRef = database.getReference("events");
                                        String eventName = event.getName();

                                        eventsRef.child(eventName).removeValue().addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getActivity(), "Evenimentul a fost șters cu succes", Toast.LENGTH_SHORT).show();
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(getActivity(), "Eroare la ștergerea evenimentului : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                                    }

                                    @Override
                                    public void onLikeEvent(Event event) {
                                        DatabaseReference eventRef = database.getReference("events").child(event.getName());
                                        DatabaseReference likesRef = eventRef.child("noLikes");
                                        DatabaseReference userLikesRef = eventRef.child("userLikes").child(user.getUid());

                                        userLikesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                boolean alreadyLiked = snapshot.exists();

                                                if (alreadyLiked) {
                                                    // Utilizatorul a apreciat deja evenimentul, deci elimină like-ul
                                                    likesRef.runTransaction(new Transaction.Handler() {
                                                        @NonNull
                                                        @Override
                                                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                            Long likes = currentData.getValue(Long.class);
                                                            if (likes != null) {
                                                                currentData.setValue(likes - 1);
                                                            }
                                                            return Transaction.success(currentData);
                                                        }

                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                                            if (committed) {
                                                                userLikesRef.removeValue();
                                                                Toast.makeText(getActivity(), "Ai dezapreciat evenimentul", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getActivity(), "Eroare la dezaprecierea evenimentului", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    // Utilizatorul nu a apreciat încă evenimentul, deci adaugă like-ul
                                                    likesRef.runTransaction(new Transaction.Handler() {
                                                        @NonNull
                                                        @Override
                                                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                            Long likes = currentData.getValue(Long.class);
                                                            if (likes != null) {
                                                                currentData.setValue(likes + 1);
                                                            }
                                                            return Transaction.success(currentData);
                                                        }

                                                        @Override
                                                        public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                                            if (committed) {
                                                                userLikesRef.setValue(true);
                                                                Toast.makeText(getActivity(), "Ai apreciat evenimentul", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getActivity(), "Eroare la aprecierea evenimentului", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(getActivity(), "Eroare la verificarea aprecierii evenimentului", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onAddCarToEvent(Event event) {
                                        if (user != null) {
                                            String userEmail = user.getEmail();
                                            fetchUserCars(userEmail, cars -> showCarSelectionDialog(cars, event));
                                        }
                                    }

                                    @Override
                                    public void onModifyEvent(Event event) {
                                        Intent intent = new Intent(getActivity(), ModifyEvent.class);
                                        intent.putExtra("name", event.getName());
                                        startActivity(intent);
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
                                        case "Participant":
                                            fetchRecommendedEvents(newText);
                                            break;
                                        case "Admin":
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

    private void fetchRecommendedEvents() {
        //TODO: Implement the logic to fetch recommended events from the database
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

    private void fetchRecommendedEvents(String searchTerm) {
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

    private void fetchUserCars(String userEmail, final CarsCallback callback) {
        DatabaseReference carsRef = database.getReference("cars");

        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Car> userCars = new ArrayList<>();
                for (DataSnapshot carSnapshot : snapshot.getChildren()) {
                    Car car = carSnapshot.getValue(Car.class);
                    if (car != null && car.getCarOwner().equals(userEmail)) {
                        userCars.add(car);
                    }
                }
                callback.onCallback(userCars);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("EventsFragment", "Failed to fetch user cars:", error.toException());
            }
        });
    }

    public interface CarsCallback {
        void onCallback(List<Car> cars);
    }

    private void showCarSelectionDialog(List<Car> cars, Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Selectează un autovehicul");

        String[] carNames = new String[cars.size()];
        for (int i = 0; i < cars.size(); i++) {
            carNames[i] = cars.get(i).getCarBrand() + " " + cars.get(i).getCarModel() + " - " + cars.get(i).getCarRegistration();
        }

        builder.setItems(carNames, (dialog, which) -> {
            Car selectedCar = cars.get(which);
            DatabaseReference eventRef = database.getReference("events").child(event.getName()).child("waitingList");
            eventRef.child(selectedCar.getCarBrand() + " " + selectedCar.getCarModel()).setValue(selectedCar.getCarRegistration()).addOnSuccessListener(aVoid -> {
                Toast.makeText(getActivity(), "V-ati înscris cu succes!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "O eroare s-a produs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });

        builder.show();
    }


}