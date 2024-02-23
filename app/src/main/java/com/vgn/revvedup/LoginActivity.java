package com.vgn.revvedup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

        loginEmail = findViewById(R.id.email);
        loginPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.registerRedirectText);
        mAuth = FirebaseAuth.getInstance();

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> {

            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Bine ati venit!", Toast.LENGTH_SHORT).show();
                            fetchUserRole(email);
                        } else {
                            Toast.makeText(LoginActivity.this, "S-a produs o eroare! Reîncercați!", Toast.LENGTH_SHORT).show();
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
                        startAppropriateActivity(role);
                        return;
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Utilizatorul nu a fost găsit", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Eroare la accesarea bazei de date", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    private boolean validateEmail(String email) {
//
//        if (email.isEmpty()) {
//            loginEmail.setError("Vă rugăm să introduceți adresa de email!");
//            return false;
//        }
//
//        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            loginEmail.setError("Vă rugăm să introduceți o adresă validă de email!");
//            return false;
//        }
//
//        return true;
//    }
//
//    private boolean validatePassword() {
//        String password = loginPassword.getText().toString().trim();
//
//        if (password.isEmpty()) {
//            loginPassword.setError("Vă rugăm să introduceți parola!");
//            return false;
//        }
//
//        if (password.length() < 6) {
//            loginPassword.setError("Parola trebuie să aibă minim 6 caractere");
//            return false;
//        }
//
//        if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*_+=~])[A-Za-z\\d!@#$%^&*_+=~]{8,}$")) {
//            loginPassword.setError("Parola trebuie să conțină minim o literă mică, o literă mare, o cifră și un caracter special");
//            return false;
//        }
//
//        return true;
//    }

    private void startAppropriateActivity(String role) {
        Intent intent;
        switch (role) {
            case "Admin":
                intent = new Intent(LoginActivity.this, MainActivityAdmin.class);
                startActivity(intent);
                break;
            case "Vizitator":
                intent = new Intent(LoginActivity.this, MainActivityAdmin.class);
                startActivity(intent);
                break;
            case "Organizator":
                intent = new Intent(LoginActivity.this, MainActivityAdmin.class);
                startActivity(intent);
                break;
            case "Participant":
                intent = new Intent(LoginActivity.this, MainActivityAdmin.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
