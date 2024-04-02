package com.vgn.revvedup;

import android.media.Image;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class AddCarActivity extends AppCompatActivity {

    // TODO: Create a functionality so that the user can add images with his car into the Cloud Firebase and then it saves the reference to the Firebase Realtime Database.

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;
    DatabaseReference carsRef, usersRef;
    ImageView profileImageView1, profileImageView2, profileImageView3, profileImageView4, profileImageView5, profileImageView6;
    Button back, addCar;
    EditText carBrand, carModel, carRegistrationNumber;
    CheckBox exhaust, performanceMods, bodykit, coilovers, rims;
    List<String> modsApplied;
    List<Image> carPictures;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        //Firebase instances
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //Access to "users" database
        usersRef = database.getReference("users");

        //Access to "cars" database
        carsRef = database.getReference("cars");

        //Declaration of XML Layout components
        carBrand = findViewById(R.id.carBrand);
        carModel = findViewById(R.id.carModel);
        carRegistrationNumber = findViewById(R.id.carRegistration);
        exhaust = findViewById(R.id.exhaust);
        performanceMods = findViewById(R.id.performance_mods);
        bodykit = findViewById(R.id.bodykit);
        coilovers = findViewById(R.id.coilovers);
        rims = findViewById(R.id.rims);
        back = findViewById(R.id.back);
        addCar = findViewById(R.id.addCar);
        profileImageView1 = findViewById(R.id.profileImageView1);
        profileImageView2 = findViewById(R.id.profileImageView2);
        profileImageView3 = findViewById(R.id.profileImageView3);
        profileImageView4 = findViewById(R.id.profileImageView4);
        profileImageView5 = findViewById(R.id.profileImageView5);
        profileImageView6 = findViewById(R.id.profileImageView6);

        //Initialize the lists
        carPictures = new ArrayList<>();
        modsApplied = new ArrayList<>();

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

        addCar.setOnClickListener(v -> {
            String car_brand = carBrand.getText().toString();
            String car_model = carModel.getText().toString();
            String car_registration = carRegistrationNumber.getText().toString();
            String car_owner = user.getEmail();

            Car car = new Car(car_brand, car_model, car_registration, car_owner);
            carsRef.child(car_registration).setValue(car);

            finish();
        });

        //Back button
        back.setOnClickListener(v -> finish());
    }

    public void addSelectedMod(String modText) {
        if (!modsApplied.contains(modText)) {
            modsApplied.add(modText);
        }
    }
}