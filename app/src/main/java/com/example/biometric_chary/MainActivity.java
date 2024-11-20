package com.example.biometric_chary;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biometric_chary.AddEditPasswordActivity;
import com.example.biometric_chary.PasswordAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import models.Password;

public class MainActivity extends AppCompatActivity {

    private RecyclerView passwordRecyclerView;
    private PasswordAdapter passwordAdapter;
    private ArrayList<Password> passwordList;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Usa tu XML actual

        // Inicialización de vistas
        passwordRecyclerView = findViewById(R.id.password_recycler_view);
        FloatingActionButton addPasswordFab = findViewById(R.id.add_password_fab);
        FloatingActionButton refreshPasswordFab = findViewById(R.id.refresh_password_fab);
        FloatingActionButton logoutButton = findViewById(R.id.logout_button);  // El nuevo botón de cerrar sesión

        // Configurar RecyclerView
        passwordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        passwordList = new ArrayList<>();

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Primero, pedir la contraseña del teléfono antes de cargar la actividad
        requestPhonePassword();

        // Cargar contraseñas al inicio
        loadPasswordsFromFirebase();

        // Botón para agregar nueva contraseña
        addPasswordFab.setOnClickListener(v -> {
            // Redirigir a la actividad AddPasswordActivity para agregar una nueva contraseña
            startActivity(new Intent(MainActivity.this, AddEditPasswordActivity.class));
        });

        // Botón para refrescar las contraseñas
        refreshPasswordFab.setOnClickListener(v -> loadPasswordsFromFirebase());

        // Botón de cerrar sesión
        logoutButton.setOnClickListener(v -> {
            // Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut();
            // Redirigir a la pantalla de inicio de sesión
            Intent loginIntent = new Intent(MainActivity.this, Activity_Auth.class);  // Cambia "LoginActivity" si el nombre de tu actividad es otro
            startActivity(loginIntent);
            finish();  // Terminar la actividad actual
        });
    }

    private void requestPhonePassword() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager != null && keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    "Verificación de seguridad", "Por favor, ingresa tu contraseña o usa tu huella digital");
            startActivityForResult(intent, 1);
        } else {
            // Si el dispositivo no tiene bloqueo de pantalla configurado
            Toast.makeText(this, "Por favor, configura una contraseña o huella dactilar", Toast.LENGTH_LONG).show();
            finish(); // Salir de la app si no hay bloqueo de pantalla
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // La autenticación fue exitosa
                // Puedes proceder con la carga de contraseñas o continuar con la aplicación
            } else {
                // La autenticación falló o el usuario canceló
                Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show();
                finish(); // Cerrar la aplicación si no se autentica correctamente
            }
        }
    }

    private void loadPasswordsFromFirebase() {
        // Obtener el correo del usuario autenticado
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Referencia a las contraseñas almacenadas en Firebase
        DatabaseReference passwordsRef = FirebaseDatabase.getInstance().getReference().child("Passwords");

        passwordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Password> passwordList = new ArrayList<>();
                for (DataSnapshot passwordSnapshot : dataSnapshot.getChildren()) {
                    String userId = passwordSnapshot.child("id").getValue(String.class);
                    if (currentUserId != null && currentUserId.equals(userId)) {
                        String siteName = passwordSnapshot.child("siteName").getValue(String.class);
                        String username = passwordSnapshot.child("username").getValue(String.class);
                        String password = passwordSnapshot.child("password").getValue(String.class);
                        String notes = passwordSnapshot.child("notes").getValue(String.class);
                        String documentId = passwordSnapshot.child("documentId").getValue(String.class);

                        if (siteName != null && !siteName.trim().isEmpty()) {
                            Password newPassword = new Password(siteName, username, password, notes, documentId, userId);
                            passwordList.add(newPassword);
                        }
                    }
                }

                if (passwordList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No tienes contraseñas guardadas.", Toast.LENGTH_SHORT).show();
                } else {
                    passwordAdapter = new PasswordAdapter(passwordList, MainActivity.this);
                    passwordRecyclerView.setAdapter(passwordAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
