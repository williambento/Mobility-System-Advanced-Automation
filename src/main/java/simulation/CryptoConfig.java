package simulation;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class CryptoConfig {
    private static SecretKey secretKey;
    private static SecretKey key;

    public static SecretKey getSecretKey() throws NoSuchAlgorithmException {
        if (secretKey == null) {
            // Gera a chave da criptografia caso a mesma ainda não tenha sido gerada
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // Tamanho da chave AES
            secretKey = keyGen.generateKey();
        }
        return secretKey;
    }
    
    public static void setSecretKey(SecretKey _key) {
        key = _key;
    }

    public static SecretKey getKey(){
        return key;
    }
}
