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

public class CarsFragment extends Fragment {

    // TODO: If the user is "admin": display the cars from the database
    // TODO: If the user is "participant": display his cars (he/she might have more than one car)
    // TODO: If the user is "organizator": display all the cars that registered in his/hers event & create functionality so that the event admin can accept or refuse the car in his event

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private List<Car> cars;
    private CarsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cars, container, false);

        //Initialize Firebase Database and Event List
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        cars = new ArrayList<>();

        RecyclerView carRecyclerView = view.findViewById(R.id.carRecyclerView);
        SearchView searchView = view.findViewById(R.id.searchView);
        carRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (user != null) {
            String userEmail = user.getEmail();
            DatabaseReference usersRef = database.getReference("users");
            usersRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String roleFromDb = userSnapshot.child("role").getValue(String.class);
                            if (roleFromDb != null) {
                                adapter = new CarsAdapter(cars, roleFromDb);
                                carRecyclerView.setAdapter(adapter);
                            }

                            switch (Objects.requireNonNull(roleFromDb)) {
                                case "Participant":
                                    fetchUserCars(userEmail);
                                    break;
                                case "Admin":
                                    fetchCars();
                                    break;
                                case "Organizator":
                                    fetchCarWaitingList();
                                    break;
                            }

                            adapter.setOnItemClickListener(new CarsAdapter.OnItemClickListener() {
                                @Override
                                public void onDetailsClick(Car car) {
                                    // Deschideți EventDetailsActivity și trimiteți detalii despre eveniment
                                    Intent intent = new Intent(getActivity(), CarDetails.class);
                                    intent.putExtra("carRegistration", car.getCarRegistration());
                                    startActivity(intent);
                                }

                                @Override
                                public void onDeleteClick(Car car) {
                                    DatabaseReference carsRef = database.getReference("cars");
                                    String carRegistration = car.getCarRegistration(); // presupunând că fiecare mașină are un cheie unic în baza de date

                                    // Șterge mașina din baza de date
                                    carsRef.child(carRegistration).removeValue().addOnSuccessListener(aVoid -> {
                                        // Ștergere reușită
                                        Toast.makeText(getActivity(), "Mașina a fost ștearsă cu succes", Toast.LENGTH_SHORT).show();
                                    }).addOnFailureListener(e -> {
                                        // Întâmpinare erori în timpul ștergerii
                                        Toast.makeText(getActivity(), "Eroare la ștergerea mașinii: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onAcceptClick(Car car) {
                                    // Fetch events from the database of the current user
                                    DatabaseReference eventsRef = database.getReference("events");
                                    String currentUserEmail = user.getEmail();

                                    eventsRef.orderByChild("eventOwner").equalTo(currentUserEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            List<String> eventNames = new ArrayList<>();
                                            for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                                                String eventName = eventSnapshot.getKey();
                                                eventNames.add(eventName);
                                            }

                                            // Show a dialog to select the event
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle("Selecteaza Eveniment");

                                            String[] events = eventNames.toArray(new String[0]);

                                            builder.setItems(events, (dialog, which) -> {
                                                String selectedEventName = events[which];
                                                acceptCarForEvent(car, selectedEventName);
                                            });

                                            builder.show();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.w("CarsFragment", "Failed to fetch events for current user:", error.toException());
                                        }
                                    });
                                }


                                @Override
                                public void onDenyCar(Car car) {
                                    // Fetch events from the database of the current user
                                    DatabaseReference eventsRef = database.getReference("events");
                                    String currentUserEmail = user.getEmail();

                                    eventsRef.orderByChild("eventOwner").equalTo(currentUserEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                                                String eventName = eventSnapshot.getKey();
                                                denyCarForEvent(car, eventName);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.w("CarsFragment", "Failed to fetch events for current user:", error.toException());
                                        }
                                    });

                                }

                                @Override
                                public void onModifyClick(Car car) {
                                    Intent intent = new Intent(getActivity(), ModifyCar.class);
                                    intent.putExtra("name", car.getCarRegistration());
                                    startActivity(intent);
                                }
                            });
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
                    usersRef.child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    String roleFromDb = userSnapshot.child("role").getValue(String.class);

                                    if (roleFromDb != null) {
                                        adapter = new CarsAdapter(cars, roleFromDb);
                                        carRecyclerView.setAdapter(adapter);
                                    }

                                    switch (Objects.requireNonNull(roleFromDb)) {
                                        case "Participant":
                                            fetchUserCars(userEmail, newText);
                                            break;
                                        case "Admin":
                                        case "Organizator":
                                            fetchCars(newText);
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

    private void acceptCarForEvent(Car car, String selectedEventName) {
        DatabaseReference eventRef = database.getReference("events").child(selectedEventName);

        // Remove the car from the waiting list
        eventRef.child("waitingList").child(car.getCarRegistration()).removeValue().addOnSuccessListener(aVoid -> {
            // If removal is successful, add the car to the accepted list
            eventRef.child("acceptedList").child(car.getCarModel()).setValue(car.getCarRegistration()).addOnSuccessListener(aVoid1 -> {
                // Notify the user
                Toast.makeText(getActivity(), "Car accepted successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Notify the user if there's an error
                Toast.makeText(getActivity(), "Error accepting car: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Notify the user if there's an error
            Toast.makeText(getActivity(), "Error removing car from waiting list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void denyCarForEvent(Car car, String eventName) {
        DatabaseReference eventRef = database.getReference("events").child(eventName);

        // Remove the car from the waiting list
        eventRef.child("waitingList").child(car.getCarModel()).removeValue().addOnSuccessListener(aVoid -> {
            // If removal is successful, add the car to the denied list
            eventRef.child("deniedList").child(car.getCarModel()).setValue(car.getCarOwner()).addOnSuccessListener(aVoid1 -> {
                // Notify the user
                Toast.makeText(getActivity(), "Car denied successfully for event: " + eventName, Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Notify the user if there's an error
                Toast.makeText(getActivity(), "Error denying car for event " + eventName + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // Notify the user if there's an error
            Toast.makeText(getActivity(), "Error removing car from waiting list for event " + eventName + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchCars() {
        DatabaseReference carsRef = database.getReference("cars");

        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cars.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Car car = childSnapshot.getValue(Car.class);
                    if (car != null) {
                        cars.add(car);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("EventFragment", "Failed to fetch cars:", error.toException());
            }
        });
    }

    private void fetchCars(String searchTerm) {
        DatabaseReference carsRef = database.getReference("cars");

        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cars.clear();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Car car = childSnapshot.getValue(Car.class);
                    if (car != null && (car.getCarBrand().toLowerCase().contains(searchTerm.toLowerCase()) || car.getCarModel().toLowerCase().contains(searchTerm.toLowerCase()) || car.getCarRegistration().toLowerCase().contains(searchTerm.toLowerCase()))) {
                        cars.add(car);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("CarsFragment", "Failed to fetch cars:", error.toException());
            }
        });
    }

    private void fetchUserCars(String userEmail) {
        DatabaseReference carsRef = database.getReference("cars");

        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cars.clear();
                for (DataSnapshot carSnapshot : snapshot.getChildren()) {
                    Car car = carSnapshot.getValue(Car.class);
                    if (car != null && car.getCarOwner().equals(userEmail)) {
                        cars.add(car);
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

    private void fetchUserCars(String userEmail, String searchTerm) {
        DatabaseReference carsRef = database.getReference("cars");

        carsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cars.clear();
                for (DataSnapshot carSnapshot : snapshot.getChildren()) {
                    Car car = carSnapshot.getValue(Car.class);
                    if (car != null && car.getCarOwner().equals(userEmail) && (car.getCarBrand().toLowerCase().contains(searchTerm.toLowerCase()) || car.getCarModel().toLowerCase().contains(searchTerm.toLowerCase()) || car.getCarRegistration().toLowerCase().contains(searchTerm.toLowerCase()))) {
                        cars.add(car);
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

    private void fetchCarWaitingListForEvent(String eventName) {
        DatabaseReference eventRef = database.getReference("events").child(eventName).child("waitingList");

        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot carSnapshot : snapshot.getChildren()) {
                    String carModel = carSnapshot.getKey();
                    String carRegistration = carSnapshot.getValue(String.class);
                    if (carRegistration != null) {
                        // Retrieve the car details from the database
                        DatabaseReference carsRef = database.getReference("cars").child(carRegistration);
                        carsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot carSnapshot) {
                                Car car = carSnapshot.getValue(Car.class);
                                if (car != null) {
                                    // Check if the current user is the owner of the car
                                    if (car.getCarOwner().equals(user.getEmail())) {
                                        // Add the car to the list
                                        cars.add(car);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.w("CarsFragment", "Failed to fetch car details:", error.toException());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("CarsFragment", "Failed to fetch waiting list for event " + eventName + ":", error.toException());
            }
        });
    }

    private void fetchCarWaitingList() {
        DatabaseReference eventsRef = database.getReference("events");
        String currentUserEmail = user.getEmail();

        eventsRef.orderByChild("eventOwner").equalTo(currentUserEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    String eventName = eventSnapshot.getKey();
                    fetchCarWaitingListForEvent(eventName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("CarsFragment", "Failed to fetch events for current user:", error.toException());
            }
        });
    }



//    private void fetchCarWaitingList(String searchTerm) {
//
//    }

}

