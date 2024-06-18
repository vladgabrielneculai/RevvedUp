package com.vgn.revvedup;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

public class ModifyEvent extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseAuth mAuth;
    FirebaseUser user;

    Button backButton, modifyButton, selectEventImage;
    ImageView eventImageView;
    EditText eventName, eventDetails;
    CheckBox exhaust, coilovers, bodykit, rims, performance_mods, loudest_pipe, limbo, best_car;
    Uri selectedImageUri;
    GoogleMap googleMap;
    boolean isEditMode = false;

    String event_name;

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

        disableEditText();

        // Initialize the SupportMapFragment and request the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("ModifyEvent", "MapFragment is null");
        }

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
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

                    eventName.setText(name);
                    eventDetails.setText(details);

                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(ModifyEvent.this).load(imageUrl).into(eventImageView);
                    }

                    if (googleMap != null) {
                        LatLng eventLocation = new LatLng(latitude, longitude);
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

        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events").child(event_name);
        eventRef.child("name").setValue(newName);
        eventRef.child("details").setValue(newDetails);

        if (selectedImageUri != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference eventImageRef = storageReference.child("event_images").child(event_name);
            eventImageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> eventImageRef.getDownloadUrl().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String imagePath = task.getResult().toString();
                            eventRef.child("eventImage").setValue(imagePath);
                        }
                    })).addOnFailureListener(e -> Toast.makeText(ModifyEvent.this, "Error uploading image!", Toast.LENGTH_SHORT).show());
        }
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
    }
}