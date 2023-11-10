package com.vgn.revvedup;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    EditText signupFName, signupLName, signupEmail, signupUsername, signupPassword, signupCPassword;
    TextView loginRedirectText;
    Button signupButton;
    CheckBox signupRoleUser, signupRoleEventAdmin;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signupFName = findViewById(R.id.fname);
        signupLName = findViewById(R.id.lname);
        signupUsername = findViewById(R.id.username);
        signupEmail = findViewById(R.id.email);
        signupPassword = findViewById(R.id.password);
        signupCPassword = findViewById(R.id.cpassword);
        signupButton = findViewById(R.id.register_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupRoleUser = findViewById(R.id.user);
        signupRoleEventAdmin = findViewById(R.id.event_admin);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("utilizatori");

        signupUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                checkUsername(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        signupEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                checkEmail(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fname = signupFName.getText().toString();
                final String lname = signupLName.getText().toString();
                final String username = signupUsername.getText().toString();
                final String email = signupEmail.getText().toString();
                final String password = signupPassword.getText().toString();
                final String role = (signupRoleUser.isChecked()) ? "User" : ((signupRoleEventAdmin.isChecked()) ? "EventAdmin" : "");

                if (signupUsername.getError() == null && signupEmail.getError() == null) {
                    reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(RegisterActivity.this, "Acest nume de utilizator este deja existent!", Toast.LENGTH_SHORT).show();
                            } else {
                                reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Toast.makeText(RegisterActivity.this, "Există deja un cont asociat acestei adrese de email! Reîncercați!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (isPasswordValid(password)) {
                                                HelperClass helperClass = new HelperClass(fname, lname, username, email, password, role);
                                                reference.child(username).setValue(helperClass);

                                                Toast.makeText(RegisterActivity.this, "V-ați autentificat cu succes!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivityUser.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Parola trebuie să aibă minim 8 caractere și să conțină o majusculă, o cifră și un caracter special.", Toast.LENGTH_LONG).show();
                                                signupPassword.setText("");
                                                signupCPassword.setText("");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle error
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle error
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Verificați username-ul și adresa de email!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkUsername(final String username) {
        reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    signupUsername.setError("Acest nume de utilizator este deja existent!");
                } else {
                    signupUsername.setError(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void checkEmail(final String email) {
        reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    signupEmail.setError("Există deja un cont asociat acestei adrese de email! Reîncercați!");
                } else {
                    signupEmail.setError(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private boolean isPasswordValid(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*_+=~])[A-Za-z\\d!@#$%^&*_+=~]{8,}$";
        return password.matches(pattern);
    }
}
