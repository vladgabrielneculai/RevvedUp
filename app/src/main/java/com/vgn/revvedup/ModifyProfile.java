package com.vgn.revvedup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ModifyProfile extends AppCompatActivity {

    Button backButton, changeImageButton, saveChangesButton;
    ImageView imageView;
    EditText firstName, lastName, email, username, password, cpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_profile);

        backButton = findViewById(R.id.backtomore);
        saveChangesButton = findViewById(R.id.savechanges);
        changeImageButton = findViewById(R.id.changeimage);

        imageView = findViewById(R.id.profileImageView);
        firstName = findViewById(R.id.fname);
        lastName = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        cpassword = findViewById(R.id.cpassword);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModifyProfile.this, MoreFragment.class);
            startActivity(intent);
        });

        String currentUserID = String.valueOf(FirebaseAuth.getInstance().getCurrentUser());
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("utilizatori").child(currentUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fName = snapshot.child("fname").getValue(String.class);
                    String lName = snapshot.child("lname").getValue(String.class);
                    String emailaddr = snapshot.child("email").getValue(String.class);
                    String uname = snapshot.child("username").getValue(String.class);
                    String pass = snapshot.child("password").getValue(String.class);
                    String cpass = snapshot.child("password").getValue(String.class);

                    firstName.setText(fName);
                    lastName.setText(lName);
                    email.setText(emailaddr);
                    username.setText(uname);
                    password.setText(pass);
                    cpassword.setText(cpass);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}