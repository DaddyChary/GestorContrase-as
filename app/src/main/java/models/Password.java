package models;

public class Password {
    private String siteName;
    private String username;
    private String password;
    private String notes;
    private String id;
    private String documentId; // Este campo es para almacenar el ID del documento de Firestore

    // Constructor vacío para Firebase
    public Password() {
    }

    // Constructor con todos los campos, incluyendo documentId
    public Password(String siteName, String username, String password, String notes, String id, String documentId) {
        this.siteName = siteName;
        this.username = username;
        this.password = password;
        this.id = id;
        this.notes = notes;
        this.documentId = documentId;  // Establecer el documentId
    }

    // Getters
    public String getSiteName() {
        return siteName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNotes() {
        return notes;
    }

    public String getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId; // Devuelve el ID del documento
    }

    // Setters
    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId; // Establece el ID del documento
    }
}
