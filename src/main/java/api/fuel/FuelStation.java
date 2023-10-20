package api.fuel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Arrays;

import api.crypto.Crypto;
import api.json.JsonSchema;

public class FuelStation extends Thread implements Serializable {
    
    public void run(){
        Socket postoSocket;
        try {
            postoSocket = new Socket("127.0.0.1", 3000);
            criarConta(postoSocket);
            while (true) {

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // requisicao para criar conta no AlphaBank
    public void criarConta(Socket _socket){
        try {
            DataOutputStream output = new DataOutputStream(_socket.getOutputStream());
            DataInputStream input = new DataInputStream(_socket.getInputStream());

            String requestCriaConta = JsonSchema.criarConta("criarConta", "fuel_station", "fuel2023", 10000.0);
            // criptografa a mensagem
            byte[] mensagemCrypto = Crypto.encrypt(requestCriaConta.getBytes(), geraChave(), geraIv());
            // envia a mensagem criptografada ao servidor
            output.write(mensagemCrypto);
            output.flush();

            // recebendo a mensagem criptografada do cliente
            byte[] mensagemCriptografada = new byte[1024];
            int length = input.read(mensagemCriptografada); // pega o tamanho
            byte[] mensagemCriptografadaBytes = new byte[length];
            System.arraycopy(mensagemCriptografada, 0, mensagemCriptografadaBytes, 0, length);
                        
            // descriptografar a mensagem usando a classe Crypto
            byte[] mensagemDescriptografadaBytes = Crypto.decrypt(mensagemCriptografadaBytes, geraChave(), geraIv());
                    
            // converte a mensagem descriptografada para string
            String mensagemDescString = new String(mensagemDescriptografadaBytes);
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // gera chave para a criptografia
    public byte[] geraChave(){
        // Crie uma chave de 128 bits (16 bytes)
        byte[] chave = new byte[16];
        // Preencha a chave com zeros neste exemplo
        Arrays.fill(chave, (byte) 0);
        return chave;
    }

    // gera iv para a criptografia
    public byte[] geraIv(){
        // Crie um IV de 16 bytes (inicialização aleatória)
        byte[] iv = new byte[16];
        // Preencha o IV com zeros neste exemplo
        Arrays.fill(iv, (byte) 0);
        return iv;
    }
}
