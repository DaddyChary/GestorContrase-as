package com.example.biometric_chary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthException;

public class Activity_Auth extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(Activity_Auth.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                            // Pasar el UID al MainActivity como extra
                            Intent intent = new Intent(Activity_Auth.this, MainActivity.class);
                            intent.putExtra("userId", user.getUid());
                            startActivity(intent);
                            finish(); // Cerrar la actividad actual
                        }
                    } else {
                        Log.e("AuthError", "Login failed", task.getException());
                        Toast.makeText(Activity_Auth.this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(Activity_Auth.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                            // Pasar el UID al MainActivity como extra
                            Intent intent = new Intent(Activity_Auth.this, MainActivity.class);
                            intent.putExtra("userId", user.getUid());
                            startActivity(intent);
                            finish(); // Cerrar la actividad actual
                        }
                    } else {
                        Log.e("AuthError", "Registration failed", task.getException());
                        Toast.makeText(Activity_Auth.this, "Error al registrarse", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
