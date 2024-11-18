package com.example.biometric_chary;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class AESHelper {

    // Método para desencriptar AES
    public static String decrypt(String encryptedData, String key) throws Exception {
        // Crear una clave AES a partir de la clave proporcionada (32 bytes)
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

        // Crear un objeto Cipher para desencriptar
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Decodificar los datos en Base64 y desencriptar
        byte[] decodedData = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decryptedData = cipher.doFinal(decodedData);

        return new String(decryptedData);
    }

    // Método para encriptar (si alguna vez necesitas encriptar)
    public static String encrypt(String data, String key) throws Exception {
        // Crear una clave AES a partir de la clave proporcionada
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

        // Crear un objeto Cipher para encriptar
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Encriptar los datos y codificar en Base64
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }
}
