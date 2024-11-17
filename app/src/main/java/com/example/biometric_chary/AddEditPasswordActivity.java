package com.example.biometric_chary;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        // Configurar el botón de guardar
        savePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePassword();  // Llamar al método para guardar la contraseña
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

        // Generar un ID único para el documento
        String documentId = databaseReference.push().getKey(); // Genera un ID único automáticamente

        // Crear el objeto Password con los datos proporcionados, incluyendo el documentId generado y el userId
        Password newPassword = new Password(siteName, username, password, notes, userId, documentId);

        // Guardar el objeto Password en Firebase Realtime Database
        // Después de guardar la contraseña
        databaseReference.child(documentId).setValue(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Mensaje de éxito
                        Toast.makeText(AddEditPasswordActivity.this, "Contraseña guardada correctamente", Toast.LENGTH_SHORT).show();
                        finish();  // Regresar a la actividad principal
                    } else {
                        // Mensaje de error
                        Toast.makeText(AddEditPasswordActivity.this, "Error al guardar la contraseña", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
