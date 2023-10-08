package simulation;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;

public class CompanyServer {
    public static void main(String[] args) {
        int port = 8000; // Porta na qual o servidor irá escutar

        try {
            // Gere a chave secreta uma vez no servidor
            // Obtém a chave secreta usando CryptoConfig
            SecretKey secretKey = CryptoConfig.getSecretKey();
            
            // Passe a chave secreta para o cliente
            CryptoConfig.setSecretKey(secretKey);

            // Cria um servidor Socket (serverSocket)
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor está ouvindo na porta " + port);

            while (true) {
                // Aguarda uma conexão do cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Cria threads para manipular múltiplos clientes simultaneamente
                // Passa o clientSocket e a chave secreta para a classe ClientHandler
                ClientHandler clientHandler = new ClientHandler(clientSocket, secretKey);

                // Roda a Thread
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
