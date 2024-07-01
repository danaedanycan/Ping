package fr.epita.assistants.myide.domain.service;

import fr.epita.assistants.myide.utils.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // Generate a new AES key
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(256); // Ensure the environment supports 256-bit keys, otherwise use 128
        return keyGenerator.generateKey();
    }

    // Encrypt the input string using the provided secret key
    public static String encrypt(String input, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Decrypt the input string using the provided secret key
    public static String decrypt(String input, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(input));
        Logger.log(new String(decryptedBytes, "UTF-8"));
        return new String(decryptedBytes, "UTF-8");
    }

    // Method to get a SecretKey from a base64 encoded string
    public static SecretKey getKeyFromBase64String(String keyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    // Method to convert a SecretKey to a base64 encoded string
    public static String getBase64StringFromKey(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
