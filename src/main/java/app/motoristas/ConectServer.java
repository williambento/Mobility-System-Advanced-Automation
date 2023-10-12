package app.motoristas;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ConnectException;

import app.criptografia.Crypto;
import app.json.JsonSchema;

public class ConectServer implements Runnable{
    private String host;
    private int port;
    private Motorista motorista;
    private String id;
    private String senha;
    private int opcao;

    public ConectServer(String host, int port, Motorista motorista, String _id, String _senha, int _opcao) {
        this.host = host;
        this.port = port;
        this.motorista = motorista;
        this.id = _id;
        this.senha = _senha;
        this.opcao = _opcao;
    }

    @Override
    public void run() {
        try {
            Socket motoristaSocket = new Socket(host, port);
            //System.out.println("Conexão criada para " + motorista.getIdMotorista());

            // entrada e saida de dados
            DataInputStream input = new DataInputStream(motoristaSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(motoristaSocket.getOutputStream());
            switch (opcao) {
                case 1:
                    criarConta("criarConta", motorista.getIdMotorista(), motorista.getSenhaMotorista(), motoristaSocket, input, output);
                    break;
                case 2:
                    saldo("saldo", id, senha, motoristaSocket, input, output);
                    break;
                case 3:
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }

       
            motoristaSocket.close();
        } catch (ConnectException e){
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void criarConta(String _request, String _id, String _senha,Socket _socket, DataInputStream _in, DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.criarConta(_request, _id, _senha);
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

            // Receba a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = _in.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);
       
            // Descriptografe a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, geraChave(), geraIv());
            // Converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);
            
            // fecha conexao
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pagar(String _request, String _idOrigem, String _senha, String _idDestino, double _valor, Socket _socket, DataInputStream _in, DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.pagar(_request, _idOrigem, _senha, _idDestino, _valor);
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

            // Receba a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = _in.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);
       
            // Descriptografe a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, geraChave(), geraIv());
            // Converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);

            //_socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saldo(String _request, String _idOrigem, String _senha, Socket _socket, DataInputStream _in, DataOutputStream _out){
        try{
            String requestCriaConta = JsonSchema.buscarConta(_request, _idOrigem, _senha);
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();

            // Receba a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = _in.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);
       
            // Descriptografe a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, geraChave(), geraIv());
            // Converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);
            System.out.println(resposta);
            //_socket.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // gera chave para a criptografia
    public static byte[] geraChave(){
        // Crie uma chave de 128 bits (16 bytes)
        byte[] chave = new byte[16];
        // Preencha a chave com zeros neste exemplo
        Arrays.fill(chave, (byte) 0);
        return chave;
    }

    // gera iv para a criptografia
    public static byte[] geraIv(){
        // Crie um IV de 16 bytes (inicialização aleatória)
        byte[] iv = new byte[16];
        // Preencha o IV com zeros neste exemplo
        Arrays.fill(iv, (byte) 0);
        return iv;
    }
}
