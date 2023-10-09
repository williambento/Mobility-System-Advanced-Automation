package simulation.versao_anterior;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64; // Importe a classe Base64 para decodificar a resposta

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.json.JSONObject;

public class Driver {

    private static SecretKey secretKeyClient;
    private static IvParameterSpec ivSpec;
    
    /*public Driver(){
        secretKeyClient = ClientHandler.getSecretKey();
    }*/
    
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // Gera a chave secreta para codificação e decodificação
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128); // Tamanho da chave AES
        secretKeyClient = keyGen.generateKey();
        
        String serverAddress = "localhost"; // Endereço do servidor (pode ser um IP ou "localhost")
        int serverPort = 12345; // Porta do servidor

        try {
            // Cria uma conexão com o servidor usando sockets
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Conexão estabelecida com o servidor.");

            // Fluxos de entrada e saída para comunicação com o servidor
            OutputStream outputStream = socket.getOutputStream(); // Objeto para saída de dados cliente ->  servidor
            InputStream inputStream = socket.getInputStream(); // Objeto para entrada de dados servidor -> cliente

            // Solicita uma rota ao servidor e obtém a resposta
            String routeRequest = requestRoute(); // Método com o JSON solicitando a rota
            byte[] requestBytes = routeRequest.getBytes(); // Conversão da mensagem em bytes

            // Vetor de inicialização usado para criptografar, cada conversa tem um iv
            byte[] ivBytes = generateRandomIV(16); 
            ivSpec = new IvParameterSpec(ivBytes);

            // Criptografia usando a chave iv
            byte[] encryptedRequest = CryptoUtils.encrypt(requestBytes, secretKeyClient, ivSpec);
            System.out.println(encryptedRequest);
            // Métodos que enviam a mensagem criptografada ao servidor
            outputStream.write(encryptedRequest);
            outputStream.flush();
            System.out.println("Solicitação de rota enviada ao servidor: " + routeRequest);

            // O buffer é criado para receber a resposta do servidor
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);

            // A descriptografia acontece usando a mesma chave iv
            byte[] decryptedResponse = CryptoUtils.decrypt(buffer, bytesRead, secretKeyClient, ivSpec);
        
            // Converte a resposta descriptografada para uma string
            String serverResponse = new String(decryptedResponse, 0, decryptedResponse.length);

            // Imprima a resposta descriptografada para depuração
            System.out.println("Resposta descriptografada: " + serverResponse);

            // Decodifica a resposta Base64
            byte[] decodedBytes = Base64.getDecoder().decode(serverResponse);

            // Converte os bytes decodificados em uma string
            String jsonResponse = new String(decodedBytes, "UTF-8");

            // Agora você pode processar a resposta como um objeto JSON
            JSONObject responseObject = new JSONObject(jsonResponse);
            System.out.println(responseObject);

            // Extrai as informações da rota do objeto JSON
            String routeId = responseObject.getString("routeId");
            String edges = responseObject.getString("edges");

            // Imprime as informações da rota
            System.out.println("Route ID: " + routeId);
            System.out.println("Edges: " + edges);

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

    public static SecretKey getSecretKey() {
        return secretKeyClient;
    }

    public static IvParameterSpec getIvParameter() {
        return ivSpec;
    }
}
