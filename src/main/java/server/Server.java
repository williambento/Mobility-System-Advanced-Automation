package server;

import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {
        final int PORTA = 12345;

        try (ServerSocket servidorSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor esperando por conexões na porta " + PORTA);

            while (true) {
                try (Socket clienteSocket = servidorSocket.accept();
                     PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()))) {

                    System.out.println("Cliente conectado.");

                    String mensagemDoCliente;
                    while ((mensagemDoCliente = in.readLine()) != null) {
                        System.out.println("Cliente diz: " + mensagemDoCliente);
                        out.println("Servidor diz: Olá, cliente!");
                    }

                } catch (IOException e) {
                    System.err.println("Erro ao lidar com a conexão do cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}
