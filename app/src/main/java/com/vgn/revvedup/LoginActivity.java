package com.vgn.revvedup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    EditText loginEmail, loginPassword;
    Button loginButton;
    TextView signupRedirectText;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Firebase instances
        mAuth = FirebaseAuth.getInstance();

        //Declaration of XML Layout components
        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.registerRedirectText);


        loginButton.setOnClickListener(v -> {

            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            //Authentication of the user in the app
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Bine ati venit!", Toast.LENGTH_SHORT).show();
                            fetchUserRole(email);
                        } else {
                            Toast.makeText(LoginActivity.this, "Autentificarea nu s-a produs! Reîncercați!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        signupRedirectText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserRole(String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("utilizatori");
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String role = userSnapshot.child("role").getValue(String.class);
                        System.out.println(role);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                } else {
                    loginEmail.setError("Utilizatorul nu a fost găsit!");
                    loginEmail.setText("");
                    loginPassword.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

//    private void checkPassword(String password) {
//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("utilizatori");
//        usersRef.orderByChild("password").equalTo(password).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
//                        String role = userSnapshot.child("role").getValue(String.class);
//                        Intent intent = new Intent(LoginActivity.this, MainActivityAdmin.class);
//                        startActivity(intent);
//                        return;
//                    }
//                } else {
//                    loginPassword.setError("Parola introdusa este incorecta! Reincercati");
//                    loginPassword.setText("");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}
