package simulation.test;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import simulation.test.banco.CriarContaRequest;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;

public class TestClient extends Thread{
    
    public static void main(String[] args) throws Exception {

        // Configuração do servidor Company
        String companyHost = "localhost";
        int companyPort = 3000;

        connectToCompany(companyHost, companyPort);

        // Configuração do servidor AlphaBank
        String alphabankHost = "localhost";
        int alphabankPort = 2000;

        //connectToAlphaBank(alphabankHost, alphabankPort);
        
    }

    public static void connectToCompany(String host, int port) {
        try {
            Security.addProvider(new BouncyCastleProvider());

            Socket socket = new Socket(host, port);

            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            // Crie a mensagem JSON usando a classe JsonSchema
            String idRota = geraSolicitacaoRota();
            boolean solicitacao = true;
            String mensagemJson = JsonSchema.criarMensagem(idRota, solicitacao);
            //System.out.println("Mensagem Cliente s/Criptografia: " + mensagemJson);

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
            //System.out.println("Mensagem Cliente c/Criptografia: " + encryptedMessage);

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
            //System.out.println("Resposta do servidor: " + resposta);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            TestServer receivedTestServer = (TestServer) objectInputStream.readObject();
            System.out.println("Rota recebida e pronto para iniciar!");

            
            TestSumo s1 = new TestSumo(receivedTestServer);
            s1.start();

            // Feche a conexão com o servidor
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connectToAlphaBank(String host, int port){
        try {
            Security.addProvider(new BouncyCastleProvider());
            Socket socket = new Socket(host, port);

            
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            criarConta(output);

            socket.close();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String geraSolicitacaoRota(){
        Random random = new Random();
        int numeroAleatorio = random.nextInt(900) + 1; // Gera um número aleatório de 1 a 900
        String idRota = "ID" + numeroAleatorio;
        return idRota;
    }

    public static void criarConta(DataOutputStream output){
        // Crie uma solicitação de criação de conta
        CriarContaRequest criarContaRequest = new CriarContaRequest("login", "senha",2000);

        // Envie a solicitação de criação de conta para o AlphaBankServer
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(output);
            objectOutputStream.writeObject(criarContaRequest);
            System.out.println("Conta Driver criada!");
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
