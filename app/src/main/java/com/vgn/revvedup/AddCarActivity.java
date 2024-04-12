package com.vgn.revvedup;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AddCarActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseStorage storage;
    DatabaseReference carsRef;
    StorageReference storageRef;
    ImageView profileImageView1, profileImageView2, profileImageView3, profileImageView4, profileImageView5, profileImageView6;
    Button back, addCar, addCarImages;
    EditText carBrand, carModel, carRegistrationNumber;
    CheckBox exhaust, performanceMods, bodykit, coilovers, rims;
    List<String> modsApplied;
    List<Uri> carPicturesUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(),
            new ActivityResultCallback<List<Uri>>() {
                @Override
                public void onActivityResult(List<Uri> uris) {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImageUris.addAll(uris);

                        for (int i = 0; i < Math.min(selectedImageUris.size(), profileImageViews.size()); i++) {
                            setImageUriForImageView(selectedImageUris.get(i), profileImageViews.get(i));
                        }
                    }
                }
            });

    private final List<Uri> selectedImageUris = new ArrayList<>();
    private List<ImageView> profileImageViews;

    private void initializeProfileImageViews() {
        profileImageViews = new ArrayList<>();
        profileImageViews.add(profileImageView1);
        profileImageViews.add(profileImageView2);
        profileImageViews.add(profileImageView3);
        profileImageViews.add(profileImageView4);
        profileImageViews.add(profileImageView5);
        profileImageViews.add(profileImageView6);
    }

    private void setImageUriForImageView(Uri uri, ImageView imageView) {
        if (imageView != null) {
            imageView.setImageURI(uri);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        carsRef = database.getReference("cars");

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

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
        addCarImages = findViewById(R.id.addCarImages);
        profileImageView1 = findViewById(R.id.profileImageView1);
        profileImageView2 = findViewById(R.id.profileImageView2);
        profileImageView3 = findViewById(R.id.profileImageView3);
        profileImageView4 = findViewById(R.id.profileImageView4);
        profileImageView5 = findViewById(R.id.profileImageView5);
        profileImageView6 = findViewById(R.id.profileImageView6);

        modsApplied = new ArrayList<>();
        carPicturesUri = new ArrayList<>();

        addCarImages.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        initializeProfileImageViews();

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
            // Retrieve car details from EditText fields
            String car_brand = carBrand.getText().toString();
            String car_model = carModel.getText().toString();
            String car_registration = carRegistrationNumber.getText().toString();
            String car_owner = user.getEmail();

            // List to store the download URLs of uploaded images
            List<String> imageDownloadUrls = new ArrayList<>();
            int totalImages = selectedImageUris.size();
            AtomicInteger imagesUploadedCount = new AtomicInteger(0);

            // Iterate through each selected image and upload it to Firebase Storage
            for (Uri imageUri : selectedImageUris) {
                StorageReference imageRef = storageRef.child("car_images").child(car_registration).child(Objects.requireNonNull(imageUri.getLastPathSegment()));

                // Upload the image to Firebase Storage
                imageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageDownloadUrls.add(uri.toString());

                            // Check if all images have been uploaded
                            if (imagesUploadedCount.incrementAndGet() == totalImages) {
                                // All images have been uploaded, create Car object and store in Firebase Realtime Database
                                Car car = new Car(car_brand, car_model, car_registration, car_owner, imageDownloadUrls, modsApplied);
                                // Store the car object in the Realtime Database
                                carsRef.child(car_registration).setValue(car);

                                finish();
                            }
                        }))
                        .addOnFailureListener(e -> {
                            // Handle image upload failure
                            Toast.makeText(AddCarActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        back.setOnClickListener(v -> finish());
    }

    public void addSelectedMod(String modText) {
        if (!modsApplied.contains(modText)) {
            modsApplied.add(modText);
        }
    }
}