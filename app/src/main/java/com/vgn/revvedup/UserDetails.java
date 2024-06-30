package com.vgn.revvedup;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserDetails extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference usersRef;

    FirebaseStorage storage;
    StorageReference storageRef;

    Button backButton;
    TextView fname, lname, email, username, role;
    ImageView userImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize Database
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Initialize Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize UI elements
        fname = findViewById(R.id.fname);
        lname = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        role = findViewById(R.id.role);
        backButton = findViewById(R.id.back);
        userImageView = findViewById(R.id.userImageView);

        // Get username from intent extras
        String usernameExtra = getIntent().getStringExtra("username");

        // Fetch user details based on username
        Query query = usersRef.orderByChild("username").equalTo(usernameExtra);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String firstName = snapshot.child("fname").getValue(String.class);
                        String lastName = snapshot.child("lname").getValue(String.class);
                        String emailAddress = snapshot.child("email").getValue(String.class);
                        String userName = snapshot.child("username").getValue(String.class);
                        String userRole = snapshot.child("role").getValue(String.class);
                        String imageUrl = snapshot.child("imagePath").getValue(String.class);

                        fname.setText("First Name: " + firstName);
                        lname.setText("Last Name: " + lastName);
                        email.setText("Email: " + emailAddress);
                        username.setText("Username: " + userName);
                        role.setText("Role: " + userRole);

                        // Load image using Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(UserDetails.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.empty_image)
                                    .error(R.drawable.error_image) // Optional: Set error drawable
                                    .into(userImageView);
                        }
                    }
                } else {
                    // Handle case where user data is not found
                    // This typically shouldn't happen if username is properly managed
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

        // Handle back button
        backButton.setOnClickListener(view -> {
            // Define back button behavior here
            finish();
        });
    }
}
