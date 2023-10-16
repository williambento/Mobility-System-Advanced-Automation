package app.transporte;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import app.criptografia.Crypto;
import app.json.JsonSchema;

public class EmpresaMobilidade extends Thread implements Serializable{
    private String nome;
    private ServerSocket empresaSocket;

    /*public EmpresaMobilidade(){
        this.clienteHandler = new AcessoMultiplo();
    }*/

    public void start(int PORT){
        try {
            empresaSocket = new ServerSocket(PORT);
            System.out.println("Servidor da Empresa de Mobilidade aguardando conexões...");

            // Inicia a conexão com o servidor remoto como cliente
            String servidorRemotoHost = "127.0.0.1";
            int servidorRemotoPorta = 2000;  // Substitua pela porta real do servidor remoto
            Socket servidorRemotoSocket = new Socket(servidorRemotoHost, servidorRemotoPorta);

            while (true) {
                // aguarda e aceita conexões de clientes
                Socket clienteSocket = empresaSocket.accept();
                //System.out.println("Cliente conectado " + clienteSocket.getInetAddress());
                // cria uma nova thread para lidar com o cliente 
                // assim é possível lidar com vários clientes ao mesmo tempo
                AcessoMultiplo clienteHandler = new AcessoMultiplo(clienteSocket);
                //clienteHandler.setClientSocket(clienteSocket);
                clienteHandler.start();
                pagar("empresa", "22", clienteHandler.getValor(), "DRIVER1", servidorRemotoSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // pagar drivers
    public static void pagar(String _idCompany, String _senha, Double _valor, String _idDriver, Socket _socket){
        try {
            // entrada e saida de dados
            DataInputStream _in = new DataInputStream(_socket.getInputStream());
            DataOutputStream _out = new DataOutputStream(_socket.getOutputStream());
            //ObjectOutputStream objeto = new ObjectOutputStream(_socket.getOutputStream());
            
            String requestCriaConta = JsonSchema.pagar("pagar", _idCompany, _senha, _idDriver, _valor);
            // Criptografe a mensagem usando a classe Crypto
            byte[] encryptedMessage = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
        
            // Envie a mensagem criptografada ao servidor
            _out.write(encryptedMessage);
            _out.flush();
            /* 
            // Receba a resposta criptografada do servidor
            byte[] encryptedResponse = new byte[1024];
            int length = _in.read(encryptedResponse);
            byte[] encryptedResponseBytes = new byte[length];
            System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);
       
            // Descriptografe a resposta usando a classe Crypto
            byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, geraChave(), geraIv());
            // Converte a resposta descriptografada para String
            String resposta = new String(decryptedResponseBytes);
            System.out.println(resposta);*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getNome() {
        return nome;
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
