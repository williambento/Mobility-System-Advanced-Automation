package simulation;

import java.io.*;
import java.net.*;
import java.security.Security;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Driver {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());

        String serverAddress = "localhost"; // Endereço do servidor (pode ser um IP ou "localhost")
        int serverPort = 8000; // Porta do servidor

        try {
            // Cria uma conexão com o servidor
            SecretKey secretKey = CryptoConfig.getSecretKey();
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Conexão estabelecida com o servidor.");

            // Fluxos de entrada e saída para comunicação com o servidor
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // Solicita uma rota ao servidor e obtém a resposta
            String destination = "ExampleDestination"; // Substitua pelo destino desejado
            String routeRequest = ComunicationJson.createRouteRequest(destination);
            byte[] requestBytes = routeRequest.getBytes();

            // Envia a solicitação criptografada ao servidor
            byte[] encryptedRequest = Encryption.encrypt(requestBytes, secretKey);
            outputStream.write(encryptedRequest);
            outputStream.flush();
            System.out.println("Solicitação de rota enviada ao servidor: " + routeRequest);

            // Recebe a resposta do servidor
            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);
            byte[] encryptedResponse = new byte[bytesRead];
            System.arraycopy(buffer, 0, encryptedResponse, 0, bytesRead);
            String serverResponse = Encryption.decrypt(encryptedResponse, secretKey);
            System.out.println("Resposta do servidor: " + serverResponse);

            // Fecha a conexão com o servidor
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
