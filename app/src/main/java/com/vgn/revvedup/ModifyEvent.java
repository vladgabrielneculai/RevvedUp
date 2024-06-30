package com.vgn.revvedup;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class ModifyEvent extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseAuth mAuth;
    FirebaseUser user;

    Button backButton, modifyButton, selectEventImage, pickStartDate, pickEndDate;
    ImageView eventImageView;
    EditText eventName, eventDetails;
    TextView startDate, endDate;
    CheckBox exhaust, coilovers, bodykit, rims, performance_mods, loudest_pipe, limbo, best_car;
    Uri selectedImageUri;
    GoogleMap googleMap;
    SearchView locationAddress;
    boolean isEditMode = false;

    String event_name;
    LatLng newEventLocation; // Variable to hold the new location

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        selectedImageUri = uri;
                        eventImageView.setImageURI(uri);
                    } else {
                        Toast.makeText(ModifyEvent.this, "Image selection canceled", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_event);

        event_name = getIntent().getStringExtra("name");

        backButton = findViewById(R.id.back);
        modifyButton = findViewById(R.id.addEvent);
        selectEventImage = findViewById(R.id.change_image);
        eventImageView = findViewById(R.id.eventImageView);
        eventName = findViewById(R.id.eventName);
        eventDetails = findViewById(R.id.eventDetails);
        exhaust = findViewById(R.id.exhaust);
        coilovers = findViewById(R.id.coilovers);
        bodykit = findViewById(R.id.bodykit);
        rims = findViewById(R.id.rims);
        performance_mods = findViewById(R.id.performance_mods);
        loudest_pipe = findViewById(R.id.loudest_pipe);
        limbo = findViewById(R.id.limbo);
        best_car = findViewById(R.id.best_car);

        startDate = findViewById(R.id.eventStartDate);
        endDate = findViewById(R.id.eventEndDate);
        pickStartDate = findViewById(R.id.pickStartDate);
        pickEndDate = findViewById(R.id.pickEndDate);

        locationAddress = findViewById(R.id.location_address);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        modifyButton.setText(R.string.modify);
        disableEditText();

        // Initialize the SupportMapFragment and request the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("ModifyEvent", "MapFragment is null");
        }


        if (user != null) {
            populateEventData(event_name);
        }

        selectEventImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        modifyButton.setOnClickListener(v -> {
            if (!isEditMode) {
                modifyButton.setText(R.string.save_changes);
                enableEditText();
                isEditMode = true;
            } else {
                updateDataInDatabase(user);
                modifyButton.setText(R.string.modify);
                disableEditText();
                isEditMode = false;
                Toast.makeText(ModifyEvent.this, "Event data has been successfully updated!", Toast.LENGTH_SHORT).show();
            }
        });

        // Date selection logic
        pickStartDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            @SuppressLint("DefaultLocale") DatePickerDialog datePickerDialog = new DatePickerDialog(ModifyEvent.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Update event date text
                        startDate.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1));
                    }, year, month, day);
            datePickerDialog.show();
        });

        pickEndDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            @SuppressLint("DefaultLocale") DatePickerDialog datePickerDialog = new DatePickerDialog(ModifyEvent.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Update event date text
                        endDate.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1));
                    }, year, month, day);
            datePickerDialog.show();
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void populateEventData(String event_name) {
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(event_name);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String details = snapshot.child("details").getValue(String.class);
                    String imageUrl = snapshot.child("eventImage").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    String startDateValue = snapshot.child("startDate").getValue(String.class);
                    String endDateValue = snapshot.child("endDate").getValue(String.class);

                    eventName.setText(name);
                    eventDetails.setText(details);

                    startDate.setText(startDateValue);
                    endDate.setText(endDateValue);

                    locationAddress.setQuery(location, false);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(ModifyEvent.this).load(imageUrl).into(eventImageView);
                    }

                    LatLng eventLocation = new LatLng(latitude, longitude);
                    newEventLocation = eventLocation;
                    if (googleMap != null) {
                        googleMap.addMarker(new MarkerOptions().position(eventLocation).title(location));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15));
                    }

                    for (DataSnapshot mod : snapshot.child("modsAllowed").getChildren()) {
                        String modName = mod.getValue(String.class);
                        if (modName != null) {
                            switch (modName) {
                                case "Evacuare":
                                    exhaust.setChecked(true);
                                    break;
                                case "Suspensii/Arcuri":
                                    coilovers.setChecked(true);
                                    break;
                                case "Modificări ale caroseriei":
                                    bodykit.setChecked(true);
                                    break;
                                case "Jante":
                                    rims.setChecked(true);
                                    break;
                                case "Modificări ale motorului":
                                    performance_mods.setChecked(true);
                                    break;
                            }
                        }
                    }

                    for (DataSnapshot competition : snapshot.child("eventCompetitions").getChildren()) {
                        String competitionName = competition.getValue(String.class);
                        if (competitionName != null) {
                            switch (competitionName) {
                                case "Loudest pipe":
                                    loudest_pipe.setChecked(true);
                                    break;
                                case "Limbo":
                                    limbo.setChecked(true);
                                    break;
                                case "Best Car of The Show":
                                    best_car.setChecked(true);
                                    break;
                            }
                        }
                    }

                    locationAddress.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            String location = locationAddress.getQuery().toString();
                            List<Address> addressList;

                            Geocoder geocoder = new Geocoder(ModifyEvent.this);

                            try {
                                addressList = geocoder.getFromLocationName(location, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return false;
                            }

                            if (addressList != null && !addressList.isEmpty()) {
                                Address address = addressList.get(0);
                                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                                newEventLocation = latLng; // Update new location
                                googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            } else {
                                Toast.makeText(ModifyEvent.this, "Location not found!", Toast.LENGTH_SHORT).show();
                            }

                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return false;
                        }
                    });
                } else {
                    Log.d("ModifyEventActivity", "No event data found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ModifyEventActivity", "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Optional: Set up map settings here, e.g., enable user location, set map type, etc.
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Since googleMap is ready, you can add the marker and move the camera
        populateEventData(event_name); // Make sure this method is only called once and modify it accordingly.
    }

    private void updateDataInDatabase(FirebaseUser user) {
        String newName = eventName.getText().toString();
        String newDetails = eventDetails.getText().toString();
        String newLocation = locationAddress.getQuery().toString();
        String newStartDate = startDate.getText().toString();
        String newEndDate = endDate.getText().toString();

        DatabaseReference oldEventRef = FirebaseDatabase.getInstance().getReference("events").child(event_name);
        DatabaseReference newEventRef = FirebaseDatabase.getInstance().getReference("events").child(newName);

        // Copy data from old node to new node
        oldEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    newEventRef.setValue(snapshot.getValue()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update new node with modified data
                            newEventRef.child("name").setValue(newName);
                            newEventRef.child("details").setValue(newDetails);
                            newEventRef.child("location").setValue(newLocation);
                            newEventRef.child("startDate").setValue(newStartDate);
                            newEventRef.child("endDate").setValue(newEndDate);

                            if (newEventLocation != null) {
                                newEventRef.child("latitude").setValue(newEventLocation.latitude);
                                newEventRef.child("longitude").setValue(newEventLocation.longitude);
                            }

                            if (selectedImageUri != null) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                StorageReference eventImageRef = storageReference.child("event_images").child(newName);
                                eventImageRef.putFile(selectedImageUri)
                                        .addOnSuccessListener(taskSnapshot -> eventImageRef.getDownloadUrl().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                String imagePath = task1.getResult().toString();
                                                newEventRef.child("eventImage").setValue(imagePath);
                                            }
                                        })).addOnFailureListener(e -> Toast.makeText(ModifyEvent.this, "Error uploading image!", Toast.LENGTH_SHORT).show());
                            }

                            // Remove old node
                            oldEventRef.removeValue().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(ModifyEvent.this, "Event data has been successfully updated!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ModifyEvent.this, "Failed to delete old event data!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(ModifyEvent.this, "Failed to copy event data to new node!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ModifyEvent.this, "Original event data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ModifyEvent.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void enableEditText() {
        selectEventImage.setEnabled(true);
        eventName.setEnabled(true);
        eventDetails.setEnabled(true);
        exhaust.setEnabled(true);
        coilovers.setEnabled(true);
        bodykit.setEnabled(true);
        rims.setEnabled(true);
        performance_mods.setEnabled(true);
        loudest_pipe.setEnabled(true);
        limbo.setEnabled(true);
        best_car.setEnabled(true);
        locationAddress.setEnabled(true);
    }

    private void disableEditText() {
        selectEventImage.setEnabled(false);
        eventName.setEnabled(false);
        eventDetails.setEnabled(false);
        exhaust.setEnabled(false);
        coilovers.setEnabled(false);
        bodykit.setEnabled(false);
        rims.setEnabled(false);
        performance_mods.setEnabled(false);
        loudest_pipe.setEnabled(false);
        limbo.setEnabled(false);
        best_car.setEnabled(false);
        locationAddress.setEnabled(false);
    }
}
