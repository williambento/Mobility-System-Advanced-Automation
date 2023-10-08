package simulation;
// Driver.java
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.json.JSONObject;

public class Driver {

    private static SecretKey secretKey;
    
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Tamanho da chave AES
        secretKey = keyGen.generateKey();
        
        String serverAddress = "localhost"; // Endereço do servidor (pode ser um IP ou "localhost")
        int serverPort = 12345; // Porta do servidor

        try {
            // Cria uma conexão com o servidor
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Conexão estabelecida com o servidor.");

            // Fluxos de entrada e saída para comunicação com o servidor
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // Solicita uma rota ao servidor e obtém a resposta
            String routeRequest = requestRoute();
            byte[] requestBytes = routeRequest.getBytes();

            byte[] ivBytes = generateRandomIV(16); 
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            // Aqui você deve criptografar a solicitação antes de enviá-la
            byte[] encryptedRequest = CryptoUtils.encrypt(requestBytes, secretKey, ivSpec);
            outputStream.write(encryptedRequest);
            outputStream.flush();
            System.out.println("Solicitação de rota enviada ao servidor: " + routeRequest);

            // Recebe a resposta do servidor
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);

            // Aqui você deve descriptografar a resposta do servidor
            byte[] decryptedResponse = CryptoUtils.decrypt(buffer, bytesRead, secretKey, ivSpec);
            
            String serverResponse = new String(decryptedResponse, 0, decryptedResponse.length);
            System.out.println("Resposta do servidor: " + serverResponse);

            // Fecha a conexão com o servidor
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para criar uma solicitação de rota em formato JSON
    private static String requestRoute() {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("requestType", "route");
        jsonRequest.put("destination", "ExampleDestination");
        return jsonRequest.toString();
    }

    // Método para gerar um IV aleatório
    public static byte[] generateRandomIV(int ivSize) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[ivSize];
        secureRandom.nextBytes(iv);
        return iv;
    }
}
