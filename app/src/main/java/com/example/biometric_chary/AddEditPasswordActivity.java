package com.example.biometric_chary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.button.MaterialButton;

import models.Password;  // Asegúrate de importar correctamente la clase Password

public class AddEditPasswordActivity extends AppCompatActivity {

    private EditText siteNameEditText, usernameEditText, passwordEditText, notesEditText;
    private MaterialButton savePasswordButton;
    private DatabaseReference databaseReference;  // Referencia a la base de datos

    private FirebaseAuth mAuth;  // Para obtener el correo del usuario autenticado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_password);

        // Inicializar los campos de la interfaz
        siteNameEditText = findViewById(R.id.site_name_input);
        usernameEditText = findViewById(R.id.username_input);
        passwordEditText = findViewById(R.id.password_input);
        notesEditText = findViewById(R.id.notes_input);
        savePasswordButton = findViewById(R.id.save_password_button);

        // Inicializar la referencia a la base de datos de Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Passwords");

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Obtener el documentId del intent
        String documentId = getIntent().getStringExtra("documentId");

        // Si hay un documentId, cargar los datos para edición
        if (documentId != null && !documentId.isEmpty()) {
            loadPasswordData(documentId);
        }

        // Configurar el botón de guardar
        savePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePassword();  // Llamar al método para guardar la contraseña
            }
        });
    }

    private void loadPasswordData(String documentId) {
        // Consultar el documento en Firebase Realtime Database
        databaseReference.child(documentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Mapear los datos del snapshot al objeto Password
                    Password password = snapshot.getValue(Password.class);
                    if (password != null) {
                        // Llenar los EditTexts con los datos del documento
                        siteNameEditText.setText(password.getSiteName());
                        usernameEditText.setText(password.getUsername());
                        passwordEditText.setText(password.getPassword());
                        notesEditText.setText(password.getNotes());
                    }
                } else {
                    Toast.makeText(AddEditPasswordActivity.this, "Documento no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddEditPasswordActivity.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePassword() {
        // Obtener los datos ingresados en los EditTexts
        String siteName = siteNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Validación de los campos obligatorios
        if (siteName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(AddEditPasswordActivity.this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el UID del usuario autenticado
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        // Validar si el usuario está autenticado
        if (userId.isEmpty()) {
            Toast.makeText(AddEditPasswordActivity.this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si hay un documentId pasado para editar
        String documentId = getIntent().getStringExtra("documentId");
        Log.d("AddEditPasswordActivity", "documentId: " + documentId);

        if (documentId == null || documentId.isEmpty()) {
            // Si no hay un documentId, crear uno nuevo
            documentId = databaseReference.push().getKey(); // Genera un ID único automáticamente
        }

        // Crear el objeto Password con los datos proporcionados
        Password passwordObject = new Password(siteName, username, password, notes, userId, documentId);

        // Guardar o actualizar el objeto Password en Firebase Realtime Database
        databaseReference.child(documentId).setValue(passwordObject)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (getIntent().hasExtra("documentId")) {
                            Toast.makeText(AddEditPasswordActivity.this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AddEditPasswordActivity.this, "Contraseña guardada correctamente", Toast.LENGTH_SHORT).show();
                        }
                        finish(); // Regresar a la actividad principal
                    } else {
                        Toast.makeText(AddEditPasswordActivity.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
