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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ModifyCar extends AppCompatActivity {
    private ImageView profileImageView1, profileImageView2, profileImageView3, profileImageView4, profileImageView5, profileImageView6;
    private EditText carBrand, carModel, carRegistration;
    private CheckBox exhaust, coilovers, bodykit, rims, performanceMods;
    private Button addCarImages, modifyButton, backButton;

    private Uri[] imageUris = new Uri[6];
    private String carRegistrationNumber;
    private DatabaseReference carRef;
    private StorageReference storageRef;

    private boolean isEditMode = false;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        imageUris[0] = uri;
                        profileImageView1.setImageURI(uri);
                    } else {
                        Toast.makeText(ModifyCar.this, "Image selection canceled", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_car);

        profileImageView1 = findViewById(R.id.profileImageView1);
        profileImageView2 = findViewById(R.id.profileImageView2);
        profileImageView3 = findViewById(R.id.profileImageView3);
        profileImageView4 = findViewById(R.id.profileImageView4);
        profileImageView5 = findViewById(R.id.profileImageView5);
        profileImageView6 = findViewById(R.id.profileImageView6);

        carBrand = findViewById(R.id.carBrand);
        carModel = findViewById(R.id.carModel);
        carRegistration = findViewById(R.id.carRegistration);

        exhaust = findViewById(R.id.exhaust);
        coilovers = findViewById(R.id.coilovers);
        bodykit = findViewById(R.id.bodykit);
        rims = findViewById(R.id.rims);
        performanceMods = findViewById(R.id.performance_mods);

        addCarImages = findViewById(R.id.addCarImages);
        modifyButton = findViewById(R.id.modify_car);
        backButton = findViewById(R.id.back);

        carRegistrationNumber = getIntent().getStringExtra("carRegistration");
        carRef = FirebaseDatabase.getInstance().getReference("cars").child(carRegistrationNumber);
        storageRef = FirebaseStorage.getInstance().getReference("car_images");

        disableEditText();
        loadCarData();

        addCarImages.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        modifyButton.setOnClickListener(v -> {
            if (!isEditMode) {
                modifyButton.setText(R.string.save_changes);
                enableEditText();
                isEditMode = true;
            } else {
                updateCarData();
                modifyButton.setText(R.string.modify);
                disableEditText();
                isEditMode = false;
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void loadCarData() {
        carRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Car car = snapshot.getValue(Car.class);
                    if (car != null) {
                        carBrand.setText(car.getCarBrand());
                        carModel.setText(car.getCarModel());
                        carRegistration.setText(car.getCarRegistration());

                        for (DataSnapshot mod : snapshot.child("modsApplied").getChildren()) {
                            String modName = mod.getValue(String.class);
                            if (modName != null) {
                                switch (modName) {
                                    case "Evacuare":
                                        exhaust.setChecked(true);
                                        break;
                                    case "Suspensii/Arcuri":
                                        coilovers.setChecked(true);
                                        break;
                                    case "Modificari ale caroseriei":
                                        bodykit.setChecked(true);
                                        break;
                                    case "Jante":
                                        rims.setChecked(true);
                                        break;
                                    case "Modificari ale motorului":
                                        performanceMods.setChecked(true);
                                        break;
                                }
                            }
                        }

                        if (car.getCarPicturesUri() != null) {
                            loadImages(car.getCarPicturesUri().toArray(new String[0]));
                        }
                    }
                } else {
                    Toast.makeText(ModifyCar.this, "Car data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ModifyCar.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImages(String[] imageUrls) {
        if (imageUrls.length > 0) Glide.with(this).load(imageUrls[0]).into(profileImageView1);
        if (imageUrls.length > 1) Glide.with(this).load(imageUrls[1]).into(profileImageView2);
        if (imageUrls.length > 2) Glide.with(this).load(imageUrls[2]).into(profileImageView3);
        if (imageUrls.length > 3) Glide.with(this).load(imageUrls[3]).into(profileImageView4);
        if (imageUrls.length > 4) Glide.with(this).load(imageUrls[4]).into(profileImageView5);
        if (imageUrls.length > 5) Glide.with(this).load(imageUrls[5]).into(profileImageView6);
    }

    private void updateCarData() {
        String newBrand = carBrand.getText().toString();
        String newModel = carModel.getText().toString();
        String newRegistration = carRegistration.getText().toString();

        List<String> mods = new ArrayList<>();
        if (exhaust.isChecked()) mods.add("Evacuare");
        if (coilovers.isChecked()) mods.add("Suspensii/Arcuri");
        if (bodykit.isChecked()) mods.add("Modificări ale caroseriei");
        if (rims.isChecked()) mods.add("Jante");
        if (performanceMods.isChecked()) mods.add("Modificări ale motorului");

        carRef.child("carBrand").setValue(newBrand);
        carRef.child("carModel").setValue(newModel);
        carRef.child("carRegistration").setValue(newRegistration);
        carRef.child("modsApplied").setValue(mods);

        for (int i = 0; i < imageUris.length; i++) {
            if (imageUris[i] != null) {
                uploadImage(imageUris[i], i);
            }
        }
    }

    private void uploadImage(Uri imageUri, int index) {
        StorageReference fileReference = storageRef.child(carRegistrationNumber).child("image" + index);
        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    carRef.child("carPicturesUri").child(String.valueOf(index)).setValue(uri.toString());
                }))
                .addOnFailureListener(e -> Toast.makeText(ModifyCar.this, "Image upload failed!", Toast.LENGTH_SHORT).show());
    }

    private void enableEditText() {
        carBrand.setEnabled(true);
        carModel.setEnabled(true);
        carRegistration.setEnabled(true);
        exhaust.setEnabled(true);
        coilovers.setEnabled(true);
        bodykit.setEnabled(true);
        rims.setEnabled(true);
        performanceMods.setEnabled(true);
        addCarImages.setEnabled(true);
    }

    private void disableEditText() {
        carBrand.setEnabled(false);
        carModel.setEnabled(false);
        carRegistration.setEnabled(false);
        exhaust.setEnabled(false);
        coilovers.setEnabled(false);
        bodykit.setEnabled(false);
        rims.setEnabled(false);
        performanceMods.setEnabled(false);
        addCarImages.setEnabled(false);
    }
}
