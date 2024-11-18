package com.example.biometric_chary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // Configurar RecyclerView
        passwordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        passwordList = new ArrayList<>();
        // Asegúrate de pasar tanto la lista de contraseñas como el contexto
        passwordAdapter = new PasswordAdapter(passwordList, MainActivity.this);
        passwordRecyclerView.setAdapter(passwordAdapter);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Cargar contraseñas al inicio
        loadPasswordsFromFirebase();

        // Botón para agregar nueva contraseña
        addPasswordFab.setOnClickListener(v -> {
            // Redirigir a la actividad AddPasswordActivity para agregar una nueva contraseña
            startActivity(new Intent(MainActivity.this, AddEditPasswordActivity.class));
        });

        // Botón para refrescar las contraseñas
        refreshPasswordFab.setOnClickListener(v -> loadPasswordsFromFirebase());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Llamar al método para cargar las contraseñas de Firebase
        loadPasswordsFromFirebase();
    }


    private void loadPasswordsFromFirebase() {
        // Obtener el correo del usuario autenticado
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("MainActivity", "Current User ID: " + currentUserId); // Verifica que el UID sea correcto


        // Referencia a las contraseñas almacenadas en Firebase
        DatabaseReference passwordsRef = FirebaseDatabase.getInstance().getReference().child("Passwords");

        passwordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Password> passwordList = new ArrayList<>();

                // Iterar sobre los elementos de la base de datos
                for (DataSnapshot passwordSnapshot : dataSnapshot.getChildren()) {
                    // Obtener el correo del usuario almacenado en la base de datos para cada contraseña
                    String userId = passwordSnapshot.child("id").getValue(String.class);
                    Log.d("MainActivity", "Current User ID2: " + userId); // Verifica que el UID sea correcto


                    // Comprobar si el correo del usuario coincide con el correo del usuario logueado
                    if (currentUserId != null && currentUserId.equals(userId)) {
                        // Obtener los campos de la contraseña
                        String siteName = passwordSnapshot.child("siteName").getValue(String.class);
                        String username = passwordSnapshot.child("username").getValue(String.class);
                        String password = passwordSnapshot.child("password").getValue(String.class);
                        String notes = passwordSnapshot.child("notes").getValue(String.class);
                        String documentId = passwordSnapshot.child("documentId").getValue(String.class);


                        // Validar que siteName no esté vacío o sea nulo
                        if (siteName != null && !siteName.trim().isEmpty()) {
                            // Crear el objeto Password con los datos, incluyendo el documentId y userEmail
                            Password newPassword = new Password(siteName, username, password, notes, documentId, userId);
                            passwordList.add(newPassword);
                        } else {
                            // Manejar el caso en que siteName sea nulo o vacío
                            Log.w("MainActivity", "Password entry with null or empty siteName encountered.");
                        }
                    }
                }

                // Verificar si la lista de contraseñas está vacía
                if (passwordList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No tienes contraseñas guardadas.", Toast.LENGTH_SHORT).show();
                } else {
                    // Configurar el adaptador para mostrar las contraseñas en el RecyclerView
                    passwordAdapter = new PasswordAdapter(passwordList, MainActivity.this);  // Asegúrate de usar el mismo adaptador
                    passwordRecyclerView.setAdapter(passwordAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error al cargar las contraseñas: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}