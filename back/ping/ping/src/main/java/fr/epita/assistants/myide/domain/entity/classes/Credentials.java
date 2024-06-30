package fr.epita.assistants.myide.domain.entity.classes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.epita.assistants.myide.domain.service.EncryptionUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Base64;

public class Credentials {
    private String identifiant;
    private String key;
    private static SecretKey secretKey;

    static {
        try {
            // Generate a key (in practice, you should securely store and retrieve this key)
            secretKey = EncryptionUtils.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Credentials() {
    }

    // Constructor with parameters
    @JsonCreator
    public Credentials(@JsonProperty("identifiant") String id, @JsonProperty("key") String key) {
        this.identifiant = id;
        setKey(key); // Encrypt and set the key
    }
    // Getters and setters
    public String getUsername() {
        return identifiant;
    }

    public void setUsername(String path) {
        this.identifiant = path;
    }

    public String getKey() {
        try {
            return EncryptionUtils.decrypt(this.key, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setKey(String key) {
        try {
            this.key = EncryptionUtils.encrypt(key, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(getUsername());
            writer.newLine();
            writer.write(this.key); // write encrypted key
            writer.newLine();
            writer.write(Base64.getEncoder().encodeToString(secretKey.getEncoded()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean readFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String username = reader.readLine();
            String encryptedKey = reader.readLine();
            String encodedSecretKey = reader.readLine();

            if (username != null && encryptedKey != null && encodedSecretKey != null) {
                setUsername(username);
                setKey(encryptedKey);
                setSecretKey(encodedSecretKey);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setSecretKey(String encodedSecretKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedSecretKey);
        secretKey = new SecretKeySpec(decodedKey, "AES");
    }
}
