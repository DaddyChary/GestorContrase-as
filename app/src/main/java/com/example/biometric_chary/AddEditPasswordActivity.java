package com.example.biometric_chary;

import android.os.Bundle;
import android.util.Base64;
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

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import models.Password;

public class AddEditPasswordActivity extends AppCompatActivity {

    private static final String AES_ALGORITHM = "AES";
    private static final String ENCRYPTION_KEY = "1234567890123456"; // Cambia esta clave por una más segura

    private EditText siteNameEditText, usernameEditText, passwordEditText, notesEditText;
    private MaterialButton savePasswordButton;
    private DatabaseReference databaseReference;

    private FirebaseAuth mAuth;

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

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Passwords");
        mAuth = FirebaseAuth.getInstance();

        String documentId = getIntent().getStringExtra("documentId");

        if (documentId != null && !documentId.isEmpty()) {
            loadPasswordData(documentId);
        }

        savePasswordButton.setOnClickListener(v -> savePassword());
    }

    private void loadPasswordData(String documentId) {
        databaseReference.child(documentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Password password = snapshot.getValue(Password.class);
                    if (password != null) {
                        siteNameEditText.setText(password.getSiteName());
                        usernameEditText.setText(password.getUsername());
                        try {
                            // Desencriptar la contraseña antes de mostrarla
                            String decryptedPassword = decrypt(password.getPassword());
                            passwordEditText.setText(decryptedPassword);
                        } catch (Exception e) {
                            Toast.makeText(AddEditPasswordActivity.this, "Error al desencriptar contraseña", Toast.LENGTH_SHORT).show();
                            Log.e("DecryptionError", e.getMessage());
                        }
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
        String siteName = siteNameEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        if (siteName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        if (userId.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String documentId = getIntent().getStringExtra("documentId");
        if (documentId == null || documentId.isEmpty()) {
            documentId = databaseReference.push().getKey();
        }

        try {
            // Encriptar la contraseña antes de guardar
            String encryptedPassword = encrypt(password);
            Password passwordObject = new Password(siteName, username, encryptedPassword, notes, userId, documentId);

            databaseReference.child(documentId).setValue(passwordObject)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Contraseña guardada correctamente", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error al encriptar contraseña", Toast.LENGTH_SHORT).show();
            Log.e("EncryptionError", e.getMessage());
        }
    }

    private String encrypt(String data) throws Exception {
        Key key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
    }

    private String decrypt(String encryptedData) throws Exception {
        Key key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
}
