package client;

import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) {
        final String SERVIDOR = "localhost";
        final int PORTA = 12345;

        try (Socket socket = new Socket(SERVIDOR, PORTA);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Conectado ao servidor " + SERVIDOR + " na porta " + PORTA);

            Thread inputThread = new Thread(() -> {
                try {
                    String mensagemDoServidor;
                    while ((mensagemDoServidor = in.readLine()) != null) {
                        System.out.println("Servidor diz: " + mensagemDoServidor);
                    }
                } catch (IOException e) {
                    System.err.println("Erro ao ler mensagens do servidor: " + e.getMessage());
                }
            });
            inputThread.start();

            String mensagemParaServidor;
            while (true) {
                System.out.print("Digite uma mensagem para o servidor (ou 'sair' para sair): ");
                mensagemParaServidor = consoleInput.readLine();
                out.println(mensagemParaServidor);

                if ("sair".equalsIgnoreCase(mensagemParaServidor)) {
                    break;
                }
            }

            inputThread.join();

        } catch (UnknownHostException e) {
            System.err.println("Host desconhecido: " + SERVIDOR);
        } catch (IOException e) {
            System.err.println("Erro ao conectar ao servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Thread de leitura interrompida: " + e.getMessage());
        }
    }
}
