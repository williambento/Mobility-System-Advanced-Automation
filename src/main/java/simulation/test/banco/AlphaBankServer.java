package simulation.test.banco;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;

public class AlphaBankServer extends Thread implements Serializable {
    private Map<String, ContaCorrente> contas;
    private ServerSocket serverSocket;

    public AlphaBankServer() {
        contas = new HashMap<>();
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(2000);
            System.out.println("Servidor AlphaBank iniciado. Aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexão estabelecida com " + clientSocket.getInetAddress());

                // Crie um objeto ObjectOutputStream e ObjectInputStream para enviar e receber objetos
                /*ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                */
                // Feche a conexão com o cliente
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ContaCorrente criarConta(String login, String senha, double saldoInicial) {
        ContaCorrente conta = new ContaCorrente(login, senha, saldoInicial); // Supondo que você tenha uma classe ContaCorrente
        contas.put(login, conta);
        return conta;
    }

    public ContaCorrente autenticar(String login, String senha) {
        ContaCorrente conta = contas.get(login);
        if (conta != null && conta.autenticar(login, senha)) {
            return conta;
        } else {
            return null; // Autenticação falhou
        }
    }

    public static void main(String[] args) {
        AlphaBankServer server = new AlphaBankServer();
        server.start(2000); // Escolha uma porta adequada para o seu servidor
    }
}
