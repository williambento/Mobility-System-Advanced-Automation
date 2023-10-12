package simulation.test.banco;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import simulation.test.Crypto;

import java.util.Arrays;
import java.util.HashMap;

public class AlphaBankServer extends Thread implements Serializable {
    private Map<String, ContaCorrente> contas;
    private ServerSocket serverSocket;

    public AlphaBankServer() {
        contas = new HashMap<>();
    }

    public void start(int port) throws Exception {
        try {
            serverSocket = new ServerSocket(2000);
            System.out.println("Servidor AlphaBank iniciado. Aguardando conexões...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                System.out.println("Conexão estabelecida com " + clientSocket.getInetAddress());
                
                // Receba a mensagem criptografada do cliente
                byte[] encryptedMessage = new byte[1024];
                int length = input.read(encryptedMessage);
                byte[] encryptedMessageBytes = new byte[length];
                System.arraycopy(encryptedMessage, 0, encryptedMessageBytes, 0, length);

                // Descriptografe a mensagem usando a classe Crypto
                byte[] decryptedMessageBytes = Crypto.decrypt(encryptedMessageBytes, geraChave(), geraIv());
                
                // Converte a mensagem descriptografada para String
                String decryptedMessage = new String(decryptedMessageBytes);
                System.out.println(decryptedMessage);

                // Retorno
                // Criptografe a mensagem usando a classe Crypto
                String msg = "Conta Criada";
                byte[] envio = Crypto.encrypt(msg.getBytes(), geraChave(), geraIv());
                
                // Envie a mensagem criptografada ao servidor
                output.write(envio);
                output.flush();
    
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

    public static void main(String[] args) throws Exception {
        AlphaBankServer server = new AlphaBankServer();
        server.start(2000); // Escolha uma porta adequada para o seu servidor
    }

    // Gera chave para a criptografia
    public static byte[] geraChave(){
        // Crie uma chave de 128 bits (16 bytes)
        byte[] chave = new byte[16];
        // Preencha a chave com zeros neste exemplo
        Arrays.fill(chave, (byte) 0);
        return chave;
    }

    // Gera iv para a criptografia
    public static byte[] geraIv(){
        // Crie um IV de 16 bytes (inicialização aleatória)
        byte[] iv = new byte[16];
        // Preencha o IV com zeros neste exemplo
        Arrays.fill(iv, (byte) 0);
        return iv;
    }
}
