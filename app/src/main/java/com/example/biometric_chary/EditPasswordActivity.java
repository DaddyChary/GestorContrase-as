package com.example.biometric_chary;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import models.Password;

public class EditPasswordActivity extends AppCompatActivity {

    private EditText siteNameEditText, usernameEditText, passwordEditText, notesEditText;
    private Button saveButton;
    private FirebaseFirestore firestore;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_password);

        // Inicializar vistas
        siteNameEditText = findViewById(R.id.site_name_input);
        usernameEditText = findViewById(R.id.username_input);
        passwordEditText = findViewById(R.id.password_input);
        notesEditText = findViewById(R.id.notes_input);
        saveButton = findViewById(R.id.save_password_button);

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance();

        // Obtener los datos de la contraseña desde el Intent
        Password password = (Password) getIntent().getSerializableExtra("password");

        // Rellenar los campos con los datos de la contraseña existente
        siteNameEditText.setText(password.getSiteName());
        usernameEditText.setText(password.getUsername());
        passwordEditText.setText(password.getPassword());
        notesEditText.setText(password.getNotes());

        // Configurar el botón de guardar
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePasswordInFirestore();
            }
        });
    }

    private void updatePasswordInFirestore() {
        // Obtener los nuevos datos del formulario
        String siteName = siteNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (siteName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(EditPasswordActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mapear los nuevos datos en un HashMap para actualizar
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("siteName", siteName);
        updatedData.put("username", username);
        updatedData.put("password", password);
        updatedData.put("notes", notes);

        // Obtener la referencia del documento en Firestore
        DocumentReference passwordRef = firestore.collection("passwords").document(documentId);

        // Actualizar la contraseña en Firestore
        passwordRef.update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPasswordActivity.this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                    finish(); // Volver a la actividad principal
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditPasswordActivity.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                    Log.e("UpdatePassword", "Error al actualizar la contraseña", e);
                });
    }

}
