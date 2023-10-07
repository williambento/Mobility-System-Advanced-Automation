package simulation;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Encryption {

    private static SecretKey secretKey; // Chave secreta da criptografia

    // Método para criptografar as mensagens
    public static byte[] encrypt(byte[] plaintext, SecretKey _secretKey) throws Exception {
        if (secretKey == null) {
            // Gera a chave da criptografia caso a mesma ainda não tenha sido gerada
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // Tamanho da chave AES
            secretKey = keyGen.generateKey();
        }

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plaintext);
    }

    // Método para descriptografar a mensagem
    public static String decrypt(byte[] ciphertext, SecretKey _secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(ciphertext);
        return new String(decryptedBytes);
    }
}
