package com.vgn.revvedup;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

public class AdminHomeFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location currentLocation;
    private static final double EARTH_RADIUS_KM = 6371.0;

    private DatabaseReference databaseReference;
    private PieChart modsPieChart;
    private PieDataSet modsDataSet;
    private List<PieEntry> modsPC, accepteddeniedPC;

    // Liste pentru latitudini și longitudini
    private List<Double> latitudes;
    private List<Double> longitudes;

    public AdminHomeFragment() {
        // Inițializează listele
        latitudes = new ArrayList<>();
        longitudes = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_admin, container, false);
        modsPieChart = view.findViewById(R.id.pieChartMods);

        modsPC = new ArrayList<>();
        accepteddeniedPC = new ArrayList<>();

        modsDataSet = new PieDataSet(modsPC, "Modificări auto");

        // Initialize Firebase Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("cars");

        // Initialize Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);

        // Retrieve data for Mods PieChart
        retrieveModsData();

        // Map test
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fetchLocation();

        return view;
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map);
                    if (mapFragment != null) {
                        mapFragment.getMapAsync(this);
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10));
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            fetchEventsFromFirebase();
        }
    }

    private void fetchEventsFromFirebase() {
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        eventsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                List<LatLng> eventLocations = new ArrayList<>();

                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Double latitude = eventSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = eventSnapshot.child("longitude").getValue(Double.class);

                    if (latitude != null && longitude != null) {
                        latitudes.add(latitude);
                        longitudes.add(longitude);

                        LatLng eventLatLng = new LatLng(latitude, longitude);
                        if (isWithinRadius(currentLocation, eventLatLng, 300)) {
                            eventLocations.add(eventLatLng);
                            mMap.addMarker(new MarkerOptions().position(eventLatLng).title(eventSnapshot.getKey()));
                        }
                    }
                }
                Log.d("AdminHomeFragment", "Latitudes: " + latitudes);
                Log.d("AdminHomeFragment", "Longitudes: " + longitudes);
            } else {
                Log.e("AdminHomeFragment", "Error getting events data", task.getException());
            }
        });
    }

    private boolean isWithinRadius(Location currentLocation, LatLng eventLatLng, int radiusKm) {
        double lat1 = currentLocation.getLatitude();
        double lon1 = currentLocation.getLongitude();
        double lat2 = eventLatLng.latitude;
        double lon2 = eventLatLng.longitude;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_KM * c;

        return distance <= radiusKm;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
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

    private void updateModsPieChart() {
        PieData modsData = new PieData(modsDataSet);
        modsPieChart.setData(modsData);
        modsPieChart.invalidate(); // Refresh chart

        // Customize legend
        Legend legend = modsPieChart.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(14f);
        legend.setForm(Legend.LegendForm.CIRCLE);

        modsPieChart.getDescription().setEnabled(false);
        modsPieChart.setEntryLabelTextSize(12f);
        modsPieChart.setEntryLabelColor(R.color.black);
    }
}
