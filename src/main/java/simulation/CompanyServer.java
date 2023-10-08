package simulation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CompanyServer {
    public static void main(String[] args) {
        int port = 12345; // Porta na qual o servidor irá escutar

        try {
            // Cria um servidor Socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Servidor está ouvindo na porta " + port);

            while (true) {
                // Aguarda uma conexão do cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Cria threads para manipular múltiplos clientes simultaneamente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Fluxos de entrada e saída para comunicação com o cliente
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();

            // Lê e escreve mensagens com o cliente
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String messageReceived = new String(buffer, 0, bytesRead);
                System.out.println("Mensagem do cliente: " + messageReceived);

                // Processa a mensagem ou responde ao cliente aqui

                // Verifica se a conexão ainda está ativa
                if (!clientSocket.isClosed()) {
                    // Exemplo de resposta ao cliente
                    String responseMessage = "Mensagem recebida com sucesso!";
                    outputStream.write(responseMessage.getBytes());
                    outputStream.flush();
                }
            }

            // Fecha a conexão com o cliente
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
