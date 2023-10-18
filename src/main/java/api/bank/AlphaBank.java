package api.bank;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import api.crypto.Crypto;
import api.json.JsonSchema;

public class AlphaBank extends Thread implements Serializable{
    private ServerSocket socket;
    private int PORT;
    private ArrayList<Account> accounts;

    public AlphaBank(int PORT){
        this.PORT = PORT;
        this.accounts = new ArrayList<Account>();
    }

    public void run(){
        try {
            socket = new ServerSocket(PORT);
            System.out.println("AlphaBank online...");

            while (true) {
                // aguarda e aceita conexões de clientes
                Socket clienteSocket = socket.accept();
                System.out.println("AlphaBank: cliente conectado");
                request(clienteSocket);
                clienteSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // metodo para receber mensagens
    public void request(Socket _socket){
        try {
            // entrada e saida
            DataInputStream _in = new DataInputStream(_socket.getInputStream());
            DataOutputStream _out = new DataOutputStream(_socket.getOutputStream());

            // recebendo a mensagem criptografada do cliente
            byte[] mensagemCriptografada = new byte[1024];
            int length = _in.read(mensagemCriptografada); // pega o tamanho
            byte[] mensagemCriptografadaBytes = new byte[length];
            System.arraycopy(mensagemCriptografada, 0, mensagemCriptografadaBytes, 0, length);
            
            // descriptografar a mensagem usando a classe Crypto
            byte[] mensagemDescriptografadaBytes = Crypto.decrypt(mensagemCriptografadaBytes, geraChave(), geraIv());
        
            // converte a mensagem descriptografada para string
            String mensagemDescString = new String(mensagemDescriptografadaBytes);
            // torna os dados acessiveis
            String[] resposta = JsonSchema.convertJsonString(mensagemDescString);
    
            // caso a requisição seja do tipo criar conta a conta é criada e um retorno de OK é dado ao cliente
            if ("criarConta".equals(resposta[0])){
                // converte a string de retorno em double
                double deposito = Double.parseDouble(resposta[3]);
                criarContaCorrente(resposta[1], resposta[2], deposito);
                String msgVerifica = "AlphaBank: conta " + resposta[1] + " criada!";
                // criptografa a mensagem
                byte[] mensagemCrypto = Crypto.encrypt(msgVerifica.getBytes(), geraChave(), geraIv());
                // envia a mensagem criptografada ao servidor falando qua a conta foi criada
                _out.write(mensagemCrypto);
                _out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // método para criar uma nova conta corrente
    public void criarContaCorrente(String _login, String _senha, double saldoInicial) {
        int novoNumeroConta = generateUniqueAccountNumber(); // Implemente a lógica para gerar números de conta únicos
        Account novaConta = new Account(_login, _senha, novoNumeroConta);
        novaConta.depositar(saldoInicial);
        accounts.add(novaConta);
    }

    // método para gerar números de conta diferente
    private int generateUniqueAccountNumber() {
        Random random = new Random(System.currentTimeMillis()); // Use a hora atual como semente
        return 1000 + random.nextInt(9000); // Gera números entre 1000 e 9999
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
