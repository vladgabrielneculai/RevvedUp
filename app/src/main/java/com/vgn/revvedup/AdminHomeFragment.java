package com.vgn.revvedup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminHomeFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final double RADIUS_IN_KM = 150.0;

    private DatabaseReference databaseReference;
    private PieChart modsPieChart, accepteddeniedPieChart;
    private PieDataSet modsDataSet, accepteddeniedDataSet;
    private List<PieEntry> modsPC, accepteddeniedPC;

    public AdminHomeFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        modsPieChart = view.findViewById(R.id.pieChartMods);
        accepteddeniedPieChart = view.findViewById(R.id.pieChartAcceptedDenied);

        modsPC = new ArrayList<>();
        accepteddeniedPC = new ArrayList<>();

        modsDataSet = new PieDataSet(modsPC, "Modificări auto");
        accepteddeniedDataSet = new PieDataSet(accepteddeniedPC, "Acceptat/Refuzat");


        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("cars");

        // Initialize Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());


        // Retrieve data for Mods PieChart
        retrieveModsData();
        retrieveAcceptedDeniedData();

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Request location permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, retrieve user's location
            getUserLocation();
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            // User's location retrieved successfully
                            double userLatitude = location.getLatitude();
                            double userLongitude = location.getLongitude();

                            // Query nearby events from the database
                            queryNearbyEvents(userLatitude, userLongitude);
                        }
                    });
        }
    }

    private void queryNearbyEvents(double userLatitude, double userLongitude) {
        // Assuming databaseReference points to the root of your database
        DatabaseReference eventsReference = databaseReference.child("events");
        eventsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    double eventLatitude = eventSnapshot.child("latitude").getValue(Double.class);
                    double eventLongitude = eventSnapshot.child("longitude").getValue(Double.class);

                    // Calculate the distance between user's location and event location
                    float[] results = new float[1];
                    Location.distanceBetween(userLatitude, userLongitude, eventLatitude, eventLongitude, results);
                    float distanceInKm = results[0] / 1000;

                    if (distanceInKm <= RADIUS_IN_KM) {
                        // Event is within the specified radius
                        String eventName = eventSnapshot.child("name").getValue(String.class);
                        String eventLocation = eventSnapshot.child("location").getValue(String.class);

                        // Add a marker on the map for the nearby event
                        LatLng eventLatLng = new LatLng(eventLatitude, eventLongitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(eventLatLng)
                                .title(eventName)
                                .snippet(eventLocation));
                    }
                }

                // Move the camera to the user's location
                LatLng userLatLng = new LatLng(userLatitude, userLongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 8));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retrieve user's location
                getUserLocation();
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void retrieveModsData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the previous data
                modsPC.clear();

                // Count the number of each modification
                Map<String, Integer> modsCount = new HashMap<>();
                for (DataSnapshot carSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot modsSnapshot = carSnapshot.child("modsApplied");
                    for (DataSnapshot modSnapshot : modsSnapshot.getChildren()) {
                        String modName = modSnapshot.getValue(String.class);
                        if (modsCount.containsKey(modName)) {
                            modsCount.put(modName, modsCount.get(modName) + 1);
                        } else {
                            modsCount.put(modName, 1);
                        }
                    }
                }

                // Add data to modsPC list
                for (Map.Entry<String, Integer> entry : modsCount.entrySet()) {
                    modsPC.add(new PieEntry(entry.getValue(), entry.getKey()));
                }

                // Set a different color for each entry
                List<Integer> colors = new ArrayList<>();
                for (int color : ColorTemplate.COLORFUL_COLORS) {
                    colors.add(color);
                }

                // Create a data set for Mods PieChart
                modsDataSet = new PieDataSet(modsPC, "Modificări auto");
                modsDataSet.setColors(colors);
                modsDataSet.setValueTextSize(18);
                modsDataSet.setDrawValues(true);

                // Update Mods PieChart
                updateModsPieChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if any
            }
        });
    }

    private void retrieveAcceptedDeniedData() {

    }


    private void updateModsPieChart() {
        // Set options for modsDataSet
        // For example, colors, text size, etc.

        // Assemble data
        PieData modsData = new PieData(modsDataSet);

        // Set data for Mods PieChart
        modsPieChart.setData(modsData);

        // Refresh Mods PieChart
        modsPieChart.invalidate();

        //Get rid of the Description Label
        modsPieChart.getDescription().setEnabled(false);

        //Set center text
        modsPieChart.setCenterText("Modificări auto");
        modsPieChart.setDrawEntryLabels(true);
        modsPieChart.setHoleRadius(50);
        modsPieChart.setCenterTextSize(16);

        Legend legend = modsPieChart.getLegend();
        legend.setEnabled(false);

    }

    private void updateAcceptedDeniedPieChart() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
