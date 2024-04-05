package com.vgn.revvedup;

import android.app.DatePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AddEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    //  TODO: Create the functionality so that the event admin can add the location of the event (it's better to add the location as an address than to choose the point from the map imo)

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference eventsRef, usersRef, carsRef;
    ImageView profileImageView;
    Uri selectedImageUri;
    EditText eventName, eventDetails, eventLocation;
    TextView eventStartDate, eventEndDate;
    Button pickStartDate, pickEndDate, back, addEvent, pickImage, searchLocation;
    CheckBox limbo, bestCar, loudestPipe, exhaust, performanceMods, bodykit, coilovers, rims;

    List<String> modsAllowed, eventCompetitions;
    ArrayAdapter<String> adapterEventType;
    AutoCompleteTextView autoCompleteTextView;
    GoogleMap myMap;
    SearchView mapSearchView;


    String[] event = {"Meet", "Expoziție"};

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        selectedImageUri = uri;
                        profileImageView.setImageURI(uri);
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //Firebase instances
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        user = mAuth.getCurrentUser();

        //Access to "events" database
        eventsRef = database.getReference("events");

        //Access to "users" database
        usersRef = database.getReference("users");

        //Access to "cars" database
        carsRef = database.getReference("cars");

        //Access to storage
        storageRef = storage.getReference();

        //Declaration of XML Layout components
        eventName = findViewById(R.id.eventName);
        eventDetails = findViewById(R.id.eventDetails);
        eventStartDate = findViewById(R.id.eventStartDate);
        eventEndDate = findViewById(R.id.eventEndDate);
        pickStartDate = findViewById(R.id.pickStartDate);
        pickEndDate = findViewById(R.id.pickEndDate);
        pickImage = findViewById(R.id.pickImage);
        profileImageView = findViewById(R.id.profileImageView);
        back = findViewById(R.id.back);
        addEvent = findViewById(R.id.addEvent);
        exhaust = findViewById(R.id.exhaust);
        coilovers = findViewById(R.id.coilovers);
        rims = findViewById(R.id.rims);
        bodykit = findViewById(R.id.bodykit);
        performanceMods = findViewById(R.id.performance_mods);
        limbo = findViewById(R.id.limbo);
        loudestPipe = findViewById(R.id.loudest_pipe);
        bestCar = findViewById(R.id.best_car);
        autoCompleteTextView = findViewById(R.id.autoComplete_eventtype);
        mapSearchView = findViewById(R.id.location_address);


        //Initialize the lists
        modsAllowed = new ArrayList<>();
        eventCompetitions = new ArrayList<>();

        adapterEventType = new ArrayAdapter<String>(this, R.layout.event_type, event);
        autoCompleteTextView.setAdapter(adapterEventType);

        //Logic for logo selection
        pickImage.setOnClickListener(v -> {
            // Launch the image picker activity
            imagePickerLauncher.launch("image/*");
        });


        //Mods logic
        exhaust.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedMod(exhaust.getText().toString());
            }
        });

        coilovers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedMod(coilovers.getText().toString());
            }
        });

        performanceMods.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedMod(performanceMods.getText().toString());
            }
        });

        bodykit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedMod(bodykit.getText().toString());
            }
        });

        rims.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedMod(rims.getText().toString());
            }
        });

        //Competition logic
        limbo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedCompetition(limbo.getText().toString());
            }
        });

        loudestPipe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedCompetition(loudestPipe.getText().toString());
            }
        });

        bestCar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                addSelectedCompetition(bestCar.getText().toString());
            }
        });

        //Date selection logic
        pickStartDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Update event date text
                        eventStartDate.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1));
                    }, year, month, day);
            datePickerDialog.show();
        });

        pickEndDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Update event date text
                        eventEndDate.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1));
                    }, year, month, day);
            datePickerDialog.show();
        });

        //Map logic
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(AddEventActivity.this);

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = mapSearchView.getQuery().toString();
                List<Address> addressList;

                if (location != null) {
                    Geocoder geocoder = new Geocoder(AddEventActivity.this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    myMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {


                return false;
            }
        });

        mapFragment.getMapAsync(AddEventActivity.this);


        //Button used to create an event and add it to the database
        addEvent.setOnClickListener(v -> {
            String name = eventName.getText().toString();
            String details = eventDetails.getText().toString();
            String startDate = eventStartDate.getText().toString();
            String endDate = eventEndDate.getText().toString();
            String eventOwner = user.getEmail();
            String location = eventLocation.toString();
            int noLikes = 0;
            int noCars = 0;

            if (selectedImageUri != null) {
                // Get the filename from the event name
                String filename = name.toLowerCase().replace(" ", "_") + ".jpg"; // Assuming the image format is JPEG

                // Get a reference to the Firebase Storage location where the image will be stored
                StorageReference imageRef = storageRef.child("event_images/" + filename);

                // Upload the image to Firebase Storage
                imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Image uploaded successfully
                            // Get the download URL of the image
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Save the download URL to the event object or store it wherever needed
                                String eventImage = uri.toString();
                                Event event = new Event(name, details, startDate, endDate, location, eventImage, eventOwner, modsAllowed, eventCompetitions, noLikes, noCars);
                                // Proceed with adding the event to the database
                                eventsRef.child(name).setValue(event);
                                finish();
                            }).addOnFailureListener(exception -> {
                                // Handle any errors that occurred during the process of getting the download URL
                                Toast.makeText(AddEventActivity.this, "Error uploading image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        })
                        .addOnFailureListener(exception -> {
                            // Handle any errors that occurred during the upload process
                            Toast.makeText(AddEventActivity.this, "Error uploading image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // If no image is selected, proceed with adding the event to the database without an image
                String eventImage = "";
                Event event = new Event(name, details, startDate, endDate, location, eventImage, eventOwner, modsAllowed, eventCompetitions, noLikes, noCars);
                eventsRef.child(name).setValue(event);
                finish();
            }
        });

        //Back button
        back.setOnClickListener(v -> finish());
    }


    public void addSelectedMod(String modText) {
        if (!modsAllowed.contains(modText)) {
            modsAllowed.add(modText);
        }
    }

    public void addSelectedCompetition(String competition) {
        if (!eventCompetitions.contains(competition)) {
            eventCompetitions.add(competition);
        }
    }

    @Override
    public void onMapReady(@NotNull GoogleMap googleMap) {
        myMap = googleMap;
    }


}
