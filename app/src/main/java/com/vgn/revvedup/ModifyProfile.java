package com.vgn.revvedup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ModifyProfile extends AppCompatActivity {

    // TODO: Create a functionality so that the user can select the profile image from the phone.

    Button backButton, modifyButton, selectProfileImage;
    ImageView imageView;
    EditText firstName, lastName, email, username, password, cpassword;
    Uri selectedImageUri;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        selectedImageUri = data.getData();
                        imageView.setImageURI(selectedImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_profile);

        backButton = findViewById(R.id.backtomore);
        modifyButton = findViewById(R.id.savechanges);
        selectProfileImage = findViewById(R.id.change_image);
        imageView = findViewById(R.id.profileImageView);
        firstName = findViewById(R.id.fname);
        lastName = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        cpassword = findViewById(R.id.cpassword);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        modifyButton.setText(R.string.modify);
        disableEditText();

        populateUserData(Objects.requireNonNull(user));


        selectProfileImage.setOnClickListener(v -> {
            // Create an Intent to choose an image from the gallery or camera
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            // Allow user to choose from gallery or camera
            Intent chooser = Intent.createChooser(intent, "Alege o fotografie de profil");

            // Launch the activity using the ActivityResultLauncher
            launcher.launch(chooser);
        });


        modifyButton.setOnClickListener(v -> {
            if (!isEditMode) {
                modifyButton.setText(R.string.save_changes);
                enableEditText();
                isEditMode = true;
            } else {
                if (cpassword.getText().toString().trim().equals(password.getText().toString().trim())) {
                    updateDataInDatabase(user);
                    modifyButton.setText(R.string.modify);
                    disableEditText();
                    isEditMode = false;
                    Toast.makeText(ModifyProfile.this, "Datele au fost modificate cu succes!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Parolele nu coincid! Reincercati", Toast.LENGTH_LONG).show();
                    password.setText("");
                    cpassword.setText("");
                    modifyButton.setText(R.string.save_changes);
                    enableEditText();
                    isEditMode = true;
                }
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void populateUserData(FirebaseUser user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("utilizatori");
        userRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String fName = userSnapshot.child("fname").getValue(String.class);
                        String lName = userSnapshot.child("lname").getValue(String.class);
                        String emailaddr = userSnapshot.child("email").getValue(String.class);
                        String uname = userSnapshot.child("username").getValue(String.class);
                        String pass = userSnapshot.child("password").getValue(String.class);

                        firstName.setText(fName);
                        lastName.setText(lName);
                        email.setText(emailaddr);
                        username.setText(uname);
                        password.setText(pass);
                        cpassword.setText(pass);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateDataInDatabase(FirebaseUser user) {
        String newFirstName = firstName.getText().toString();
        String newLastName = lastName.getText().toString();
        String newUsername = username.getText().toString();
        String newPassword = password.getText().toString();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("utilizatori");

        userRef.orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();

                        userRef.child(Objects.requireNonNull(userId)).child("fname").setValue(newFirstName);
                        userRef.child(Objects.requireNonNull(userId)).child("lname").setValue(newLastName);
                        userRef.child(Objects.requireNonNull(userId)).child("username").setValue(newUsername);
                        userRef.child(Objects.requireNonNull(userId)).child("password").setValue(newPassword);

                        // Update user data with image URL (optional)
                        if (selectedImageUri != null) {
                            // Upload the image to Firebase Storage
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();

                            // Create a reference to the user's specific subfolder
                            StorageReference userImageRef = storageReference.child("profile_images/" + user.getUid() + "/");

                            // Upload the image with the userImageRef
                            userImageRef.putFile(selectedImageUri)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        // Get the download URL of the uploaded image
                                        userImageRef.getDownloadUrl().addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                String imagePath = task.getResult().toString();

                                                // Update the user data with the image URL in the database
                                                userRef.child(userId).child("imagePath").setValue(imagePath);
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle image upload failure
                                        Toast.makeText(ModifyProfile.this, "Error uploading image!", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void enableEditText() {
        selectProfileImage.setEnabled(true);
        firstName.setEnabled(true);
        lastName.setEnabled(true);
        username.setEnabled(true);
        email.setEnabled(false);
        password.setEnabled(true);
        cpassword.setEnabled(true);
    }

    private void disableEditText() {
        selectProfileImage.setEnabled(false);
        firstName.setEnabled(false);
        lastName.setEnabled(false);
        username.setEnabled(false);
        email.setEnabled(false);
        password.setEnabled(false);
        cpassword.setEnabled(false);
    }


}