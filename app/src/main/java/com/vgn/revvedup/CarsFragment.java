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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CarsFragment extends Fragment {

    // TODO: If the user is "admin": display the cars from the database
    // TODO: If the user is "participant": display his cars (he/she might have more than one car)
    // TODO: If the user is "organizator": display all the cars that registered in his/hers event & create functionality so that the event admin can accept or refuse the car in his event

    private FirebaseDatabase database;
    private List<Car> cars;
    private CarsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cars, container, false);

        //Initialize Firebase Database and Event List
        database = FirebaseDatabase.getInstance();
        cars = new ArrayList<>();

        RecyclerView carRecyclerView = view.findViewById(R.id.carRecyclerView);
        SearchView searchView = view.findViewById(R.id.searchView);
        carRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CarsAdapter(cars);
        carRecyclerView.setAdapter(adapter);

        fetchCars();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fetchCars(newText);
                return false;
            }
        });

        adapter.setOnItemClickListener(new CarsAdapter.OnItemClickListener() {
            @Override
            public void onDetailsClick(Car car) {
                    // Deschideți EventDetailsActivity și trimiteți detalii despre eveniment
                    Intent intent = new Intent(getActivity(), CarDetails.class);
                    intent.putExtra("carRegistration", car.getCarRegistration());
                    startActivity(intent);
            }

            @Override
            public void onDeleteClick(Car car){
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
        });

        return view;
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
}
