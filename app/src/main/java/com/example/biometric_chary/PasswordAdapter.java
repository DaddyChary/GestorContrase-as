package com.example.biometric_chary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import models.Password;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {

    private List<Password> passwordList;
    private Context context;

    public PasswordAdapter(List<Password> passwordList) {
        this.passwordList = passwordList;
        this.context = context;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        Password password = passwordList.get(position);

        // Asignar datos a las vistas
        holder.siteNameTextView.setText(password.getSiteName());
        holder.usernameTextView.setText(password.getUsername());
        holder.passwordTextView.setText(password.getPassword());
        holder.notesTextView.setText(password.getNotes());

        // Configurar botón de editar
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddEditPasswordActivity.class);
            intent.putExtra("documentId", password.getDocumentId());
            context.startActivity(intent);
        });

        // Configurar botón de eliminar
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(password, position));
    }

    private void showDeleteConfirmationDialog(Password password, int position) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta contraseña?")
                .setPositiveButton("Eliminar", (dialog, which) -> deletePassword(password, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deletePassword(Password password, int position) {
        String documentId = password.getDocumentId();

        // Verificar que el documentId no sea nulo o vacío
        if (documentId != null && !documentId.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("passwords").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Eliminar la contraseña de la lista y notificar al RecyclerView
                        passwordList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Contraseña eliminada", Toast.LENGTH_SHORT).show();
                        Log.d("PasswordAdapter", "Documento eliminado: " + documentId);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("PasswordAdapter", "Error al eliminar documento", e);
                        Toast.makeText(context, "Error al eliminar la contraseña", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("PasswordAdapter", "El documentId es nulo o vacío");
        }
    }

    @Override
    public int getItemCount() {
        // Verificar que la lista no esté vacía antes de devolver el tamaño
        return passwordList != null ? passwordList.size() : 0;
    }

    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView siteNameTextView, usernameTextView, passwordTextView, notesTextView;
        MaterialButton editButton, deleteButton;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            siteNameTextView = itemView.findViewById(R.id.site_name_text_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            passwordTextView = itemView.findViewById(R.id.password_text_view);
            notesTextView = itemView.findViewById(R.id.notes_text_view);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
