package com.vgn.revvedup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;


public class CarDetails extends AppCompatActivity {

    //Database & Storage
    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageRef;
    DatabaseReference eventsRef, usersRef, carsRef;

    //XML Components
    Button backButton;
    TextView carBrand, carModel, carRegistration;
    ImageSlider imageSlider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        String car_registration = getIntent().getStringExtra("carRegistration");

        //Firebase instances
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        user = mAuth.getCurrentUser();

        //Access to "users" database
        usersRef = database.getReference("users");

        //Access to "cars" database
        carsRef = database.getReference("cars");

        //Access to storage
        storageRef = storage.getReference();

        imageSlider = findViewById(R.id.carImageSlider);
        carBrand = findViewById(R.id.carBrand);
        carModel = findViewById(R.id.carModel);
        carRegistration = findViewById(R.id.carRegistration);
        backButton = findViewById(R.id.back);

        ArrayList<SlideModel> slideModels = new ArrayList<>();

        if (user != null) {
            carsRef.orderByChild("carRegistration").equalTo(car_registration).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Car car = snapshot.getValue(Car.class);
                        if (car != null) {
                            carRegistration.setText(Objects.requireNonNull(car).getCarRegistration());
                            carBrand.setText(Objects.requireNonNull(car).getCarBrand());
                            carModel.setText(Objects.requireNonNull(car.getCarModel()));

                            // Adăugați fiecare URL de imagine în slideModels
                            for (String imageUrl : car.getCarPicturesUri()) {
                                slideModels.add(new SlideModel(imageUrl, ScaleTypes.CENTER_CROP));
                            }
                        }
                    }
                    // Setează lista de imagini în ImageSlider după ce au fost adăugate toate URL-urile
                    imageSlider.setImageList(slideModels, ScaleTypes.CENTER_CROP);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        //Back button
        backButton.setOnClickListener(v -> finish());
    }
}