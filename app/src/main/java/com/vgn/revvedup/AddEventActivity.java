package com.vgn.revvedup;

import android.annotation.SuppressLint;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class AddEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Database & Storage
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference eventsRef, usersRef, carsRef;

    //XML Components
    ImageView profileImageView;
    Uri selectedImageUri;
    EditText eventName, eventDetails;
    TextView eventStartDate, eventEndDate;
    Button pickStartDate, pickEndDate, back, addEvent, pickImage;
    CheckBox limbo, bestCar, loudestPipe, exhaust, performanceMods, bodykit, coilovers, rims;
    AutoCompleteTextView autoCompleteTextView;
    GoogleMap myMap;
    SearchView mapSearchView;

    //Lists and Adapters
    List<String> modsAllowed, eventCompetitions;
    ArrayAdapter<String> adapterEventType;
    String[] event = {"Meet", "Expoziție"};

    //Image Selection Logic
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

    //Main Layout
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

        //Image
        profileImageView = findViewById(R.id.profileImageView);
        pickImage = findViewById(R.id.pickImage);

        //Event details (name, description,etc.)
        eventName = findViewById(R.id.eventName);
        eventDetails = findViewById(R.id.eventDetails);
        autoCompleteTextView = findViewById(R.id.autoComplete_eventtype);

        //Mods
        exhaust = findViewById(R.id.exhaust);
        coilovers = findViewById(R.id.coilovers);
        rims = findViewById(R.id.rims);
        bodykit = findViewById(R.id.bodykit);
        performanceMods = findViewById(R.id.performance_mods);

        //Competitions
        limbo = findViewById(R.id.limbo);
        loudestPipe = findViewById(R.id.loudest_pipe);
        bestCar = findViewById(R.id.best_car);

        //Date of event
        eventStartDate = findViewById(R.id.eventStartDate);
        eventEndDate = findViewById(R.id.eventEndDate);
        pickStartDate = findViewById(R.id.pickStartDate);
        pickEndDate = findViewById(R.id.pickEndDate);

        //Map
        mapSearchView = findViewById(R.id.location_address);

        //Buttons
        back = findViewById(R.id.back);
        addEvent = findViewById(R.id.addEvent);

        //Initialize the lists
        modsAllowed = new ArrayList<>();
        eventCompetitions = new ArrayList<>();

        //Type of event selection
        adapterEventType = new ArrayAdapter<>(this, R.layout.event_type, event);
        autoCompleteTextView.setAdapter(adapterEventType);

        //Logo selection
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

            @SuppressLint("DefaultLocale") DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
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

            @SuppressLint("DefaultLocale") DatePickerDialog datePickerDialog = new DatePickerDialog(AddEventActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        // Update event date text
                        eventEndDate.setText(String.format("%d/%d/%d", dayOfMonth, monthOfYear + 1, year1));
                    }, year, month, day);
            datePickerDialog.show();
        });

        //Map logic
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_map);
        Objects.requireNonNull(mapFragment).getMapAsync(AddEventActivity.this);

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String location = mapSearchView.getQuery().toString();
                List<Address> addressList;

                Geocoder geocoder = new Geocoder(AddEventActivity.this);

                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Address address = Objects.requireNonNull(addressList).get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                myMap.addMarker(new MarkerOptions().position(latLng).title(location));
                myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

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
            String location = mapSearchView.getQuery().toString();
            String eventType = autoCompleteTextView.getText().toString();
            LatLng latLng = getLatLngFromAddress(location);

            double latitude = Objects.requireNonNull(latLng).latitude;
            double longitude = Objects.requireNonNull(latLng).longitude;

            int noLikes = 0;
            int noCars = 0;
            List<String> participantsWaitingList = new ArrayList<>();
            List<String> participantsAcceptedList = new ArrayList<>();
            List<String> participantsRejectedList = new ArrayList<>();
            HashMap<String, Boolean> userLikes = new HashMap<>();
            double score = 0.0d;


            if (selectedImageUri != null) {
                String filename = name.toLowerCase().replace(" ", "_") + ".jpg";

                StorageReference imageRef = storageRef.child("event_images/" + filename);

                imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String eventImage = uri.toString();
                            Event event = new Event(name, details, startDate, endDate, location, eventImage, eventOwner, eventType, noLikes, noCars, modsAllowed, eventCompetitions, userLikes, participantsWaitingList, participantsAcceptedList, participantsRejectedList, latitude, longitude, score);
                            eventsRef.child(name).setValue(event);
                            finish();
                        }).addOnFailureListener(exception -> Toast.makeText(AddEventActivity.this, "S-a produs o eroare la încărcarea imaginii. Vă rugăm reîncercați!", Toast.LENGTH_SHORT).show()))
                        .addOnFailureListener(exception -> Toast.makeText(AddEventActivity.this, "S-a produs o eroare la încărcarea imaginii. Vă rugăm reîncercați!", Toast.LENGTH_SHORT).show());
            } else {
                String eventImage = "";
                Event event = new Event(name, details, startDate, endDate, location, eventImage, eventOwner, eventType, noLikes, noCars, modsAllowed, eventCompetitions, userLikes, participantsWaitingList, participantsAcceptedList, participantsRejectedList, latitude, longitude, score);
                eventsRef.child(name).setValue(event);
                finish();
            }
        });

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

    private LatLng getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(AddEventActivity.this);
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address addr = addressList.get(0);
                return new LatLng(addr.getLatitude(), addr.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
