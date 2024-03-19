package com.vgn.revvedup;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


public class AddEventActivity extends AppCompatActivity {

    //  TODO: Create the functionality so that the event admin can add the location of the event (it's better to add the location as an address than to choose the point from the map imo)
    //  TODO: Create the functionality so that the event admin can add his personal photo/logo of the event

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;
    DatabaseReference eventsRef, usersRef;
    EditText eventName, eventDetails;
    TextView eventStartDate, eventEndDate;
    Button pickStartDate, pickEndDate, pickLocation, back, addEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        //Firebase instances
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //Access to "events" database
        eventsRef = database.getReference("events");

        //Access to "users" database
        usersRef = database.getReference("users");

        //Declaration of XML Layout components
        eventName = findViewById(R.id.eventname);
        eventDetails = findViewById(R.id.eventdetails);
        eventStartDate = findViewById(R.id.eventStartDate);
        eventEndDate = findViewById(R.id.eventEndDate);
        pickStartDate = findViewById(R.id.pickStartDate);
        pickEndDate = findViewById(R.id.pickEndDate);
        //pickLocation = findViewById(R.id.picklocation);
        back = findViewById(R.id.back);
        addEvent = findViewById(R.id.addevent);

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

//        pickLocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Create a FieldSelectionList to specify desired location fields
//                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LATITUDE, Place.Field.LONGITUDE);
//                FieldSelectionList fieldSelectionList = new FieldSelectionList.Builder().setFields(fields).build();
//
//                // Create a FindCurrentPlaceRequest
//                FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(fieldSelectionList);
//
//                // Use the Places Client to find the current place
//                placesClient.findCurrentPlace(request).addOnSuccessListener(new OnSuccessListener<Place>() {
//                    @Override
//                    public void onSuccess(Place place) {
//                        // Update UI with selected location details
//                        String locationName = place.getName();
//                        double latitude = place.getLatLng().latitude;
//                        double longitude = place.getLatLng().longitude;
//
//                        // Save location information in event details (adjust as needed)
//                        eventDetails.setText(eventDetails.getText() + "\nLocation: " + locationName);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Handle failure
//                        Log.e("LocationPicker", "Error finding current place", e);
//                    }
//                });
//            }
//        });

        //Button used to create an event and add it to the database
        addEvent.setOnClickListener(v -> {
            String name = eventName.getText().toString();
            String details = eventDetails.getText().toString();
            String startDate = eventStartDate.getText().toString();
            String endDate = eventEndDate.getText().toString();
            String eventOwner = user.getEmail();
            String eventImage = "";
            String location = "";

            Event event = new Event(name, details, startDate, endDate, location, eventImage, eventOwner);

            eventsRef.child(name).setValue(event);

            finish();
        });

        //Back button
        back.setOnClickListener(v -> finish());


    }
}
