package com.vgn.revvedup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ModifyProfile extends AppCompatActivity {

    Button backButton, modifyButton;
    ImageView imageView;
    EditText firstName, lastName, email, username, password, cpassword;

    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_profile);

        backButton = findViewById(R.id.backtomore);
        modifyButton = findViewById(R.id.savechanges);
        imageView = findViewById(R.id.profileImageView);
        firstName = findViewById(R.id.fname);
        lastName = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        cpassword = findViewById(R.id.cpassword);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        System.out.println(user.getEmail());


        modifyButton.setText(R.string.modify);
        disableEditText();

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModifyProfile.this, MainActivity.class);
            startActivity(intent);
        });

        populateUserData(user);

        modifyButton.setOnClickListener(v -> {
            if (!isEditMode) {
                modifyButton.setText(R.string.save_changes);
                enableEditText();
                isEditMode = true;
            } else {
                if (cpassword.equals(password)) {
                    updateDataInDatabase();
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

    private void updateDataInDatabase() {
        String newFirstName = firstName.getText().toString();
        String newLastName = lastName.getText().toString();
        String newEmail = email.getText().toString();
        String newUsername = username.getText().toString();
        String newPassword = password.getText().toString();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("utilizatori");
        userRef.child("fname").setValue(newFirstName);
        userRef.child("lname").setValue(newLastName);
        userRef.child("email").setValue(newEmail);
        userRef.child("username").setValue(newUsername);
        userRef.child("password").setValue(newPassword);


    }

    private void enableEditText() {
        firstName.setEnabled(true);
        lastName.setEnabled(true);
        username.setEnabled(true);
        email.setEnabled(true);
        password.setEnabled(true);
        cpassword.setEnabled(true);
    }

    private void disableEditText() {
        firstName.setEnabled(false);
        lastName.setEnabled(false);
        username.setEnabled(false);
        email.setEnabled(false);
        password.setEnabled(false);
        cpassword.setEnabled(false);
    }


}