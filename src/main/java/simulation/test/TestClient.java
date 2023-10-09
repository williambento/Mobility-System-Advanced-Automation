package simulation.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;

public class TestClient extends Thread{
    
    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        Socket socket = new Socket("localhost", 12345);

        DataInputStream input = new DataInputStream(socket.getInputStream());
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());

        // Crie a mensagem JSON usando a classe JsonSchema
        String idRota = geraSolicitacaoRota();
        boolean solicitacao = true;
        String mensagemJson = JsonSchema.criarMensagem(idRota, solicitacao);
        System.out.println("Mensagem Cliente s/Criptografia: " + mensagemJson);

        // Crie uma chave de 128 bits (16 bytes)
        byte[] chave = new byte[16];
        // Preencha a chave com zeros neste exemplo
        Arrays.fill(chave, (byte) 0);

        // Crie um IV de 16 bytes (inicialização aleatória)
        byte[] iv = new byte[16];
        // Preencha o IV com zeros neste exemplo
        Arrays.fill(iv, (byte) 0);

        // Criptografe a mensagem usando a classe Crypto
        byte[] encryptedMessage = Crypto.encrypt(mensagemJson.getBytes(), chave, iv);
        System.out.println("Mensagem Cliente c/Criptografia: " + encryptedMessage);

        // Envie a mensagem criptografada ao servidor
        output.write(encryptedMessage);
        output.flush();

        // Receba a resposta criptografada do servidor
        byte[] encryptedResponse = new byte[1024];
        int length = input.read(encryptedResponse);
        byte[] encryptedResponseBytes = new byte[length];
        System.arraycopy(encryptedResponse, 0, encryptedResponseBytes, 0, length);

        // Descriptografe a resposta usando a classe Crypto
        byte[] decryptedResponseBytes = Crypto.decrypt(encryptedResponseBytes, chave, iv);

        // Converte a resposta descriptografada para String
        String resposta = new String(decryptedResponseBytes);

        // Imprima a resposta
        System.out.println("Resposta do servidor: " + resposta);

        // Feche a conexão com o servidor
        socket.close();
    }

    public static String geraSolicitacaoRota(){
        Random random = new Random();
        int numeroAleatorio = random.nextInt(900) + 1; // Gera um número aleatório de 1 a 900
        String idRota = "ID" + numeroAleatorio;
        return idRota;
    }
}
