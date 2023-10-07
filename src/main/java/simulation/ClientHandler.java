package simulation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.Security;
import javax.crypto.SecretKey;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private SecretKey secretKey; // Adicione uma referência para a chave secreta

    public ClientHandler(Socket clientSocket, SecretKey secretKey) {
        this.clientSocket = clientSocket;
        this.secretKey = secretKey; // Receba a chave secreta como argumento
    }

    @Override
    public void run() {
        Security.addProvider(new BouncyCastleProvider());

        try {
            // Fluxos de entrada e saída para comunicação com o cliente
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            // Lê e escreve mensagens com o cliente
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Descriptografa a mensagem recebida usando a chave secreta
                byte[] encryptedData = new byte[bytesRead];
                System.arraycopy(buffer, 0, encryptedData, 0, bytesRead);
                String messageReceived = Encryption.decrypt(encryptedData, secretKey);

                System.out.println("Mensagem do cliente (descriptografada): " + messageReceived);

                // Processa a mensagem ou responde ao cliente aqui

                // Exemplo de resposta ao cliente
                String responseMessage = "Mensagem recebida com sucesso!";
                byte[] encryptedResponse = Encryption.encrypt(responseMessage.getBytes(), secretKey);

                // Envia a resposta criptografada ao cliente
                outputStream.write(encryptedResponse);
                outputStream.flush();
            }

            // Fecha a conexão com o cliente
            clientSocket.close();
        } catch (IOException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
