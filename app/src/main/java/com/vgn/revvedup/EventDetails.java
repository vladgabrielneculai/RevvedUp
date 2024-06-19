package com.vgn.revvedup;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class EventDetails extends AppCompatActivity implements OnMapReadyCallback {

    // Database & Storage
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference eventsRef, usersRef, carsRef;

    // XML Components
    Button backButton;
    TextView eventName, eventDetails, eventDate;
    ImageView eventImageView;
    GoogleMap myMap;
    SupportMapFragment mapFragment;

    // Event Location
    private LatLng eventLocation;
    private boolean mapReady = false;
    private boolean dataReady = false;

    private static final String TAG = "EventDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);

        String event_name = getIntent().getStringExtra("name");

        // Firebase instances
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        user = mAuth.getCurrentUser();

        // Access to "events" database
        eventsRef = database.getReference("events");

        // Access to "users" database
        usersRef = database.getReference("users");

        // Access to "cars" database
        carsRef = database.getReference("cars");

        // Access to storage
        storageRef = storage.getReference();

        // Declaration of XML Layout components
        eventName = findViewById(R.id.eventName);
        eventDetails = findViewById(R.id.eventDetails);
        eventDate = findViewById(R.id.eventDate);
        eventImageView = findViewById(R.id.eventImageView);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        backButton = findViewById(R.id.back);

        if (user != null) {
            eventsRef.orderByChild("name").equalTo(event_name).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Event event = snapshot.getValue(Event.class);
                        if (event != null) {
                            eventDetails.setText(Objects.requireNonNull(event).getDetails());
                            eventDate.setText(String.format("%s - %s", event.getStartDate(), event.getEndDate()));
                            eventName.setText(Objects.requireNonNull(event.getName()));

                            // Get the coordinates of the location from the database
                            double latitude = event.getLatitude();
                            double longitude = event.getLongitude();
                            eventLocation = new LatLng(latitude, longitude);
                            dataReady = true;
                            updateMap();

                            // Load and display the image using Glide
                            Glide.with(EventDetails.this)
                                    .load(event.getEventImage()) // Load image URL
                                    .placeholder(R.drawable.empty_image) // Placeholder image while loading
                                    .error(R.drawable.error_image) // Error image if loading fails
                                    .into(eventImageView); // ImageView to display the image
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        }

        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        myMap = googleMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        mapReady = true;
        updateMap();
    }

    private void updateMap() {
        if (mapReady && dataReady && eventLocation != null) {
            myMap.addMarker(new MarkerOptions().position(eventLocation).title(eventName.getText().toString()));
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));
        }
    }
}
