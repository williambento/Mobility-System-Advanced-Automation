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
            Socket clienteSocket;
            while (true) {
                // aguarda e aceita conexões de clientes
                clienteSocket = socket.accept();
                System.out.println("AlphaBank: cliente conectado");
                request(clienteSocket);
                //clienteSocket.close();
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
            System.out.println(resposta[0]);
            // caso a requisição seja do tipo criar conta a conta é criada e um retorno de OK é dado ao cliente
            if ("criarConta".equals(resposta[0])){
                Account conta = buscarContaCorrentePorIDDriver(resposta[1]);
                if(conta == null){
                    // converte a string de retorno em double
                    double deposito = Double.parseDouble(resposta[3]);
                    criarContaCorrente(resposta[1], resposta[2], deposito);
                    String msgVerifica = "AlphaBank: conta " + resposta[1] + " criada!";
                    System.out.println(msgVerifica);
                    // criptografa a mensagem
                    byte[] mensagemCrypto = Crypto.encrypt(msgVerifica.getBytes(), geraChave(), geraIv());
                    // envia a mensagem criptografada ao servidor falando qua a conta foi criada
                    _out.write(mensagemCrypto);
                    _out.flush();
                } 
            } else if ("pagar".equals(resposta[0])){   
                processarPagamento(resposta[1], resposta[2], resposta[3], 3.25);
                mostrarSaldoContas();
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

    public Account buscarContaCorrentePorIDDriver(String idDriver) {
        for (Account conta : accounts) {
            if (conta.getLogin().equals(idDriver)) {
                return conta; // Retorna a conta correspondente ao IDDriver
            }
        }
        return null; // Retorna null se não encontrar uma conta com o IDDriver especificado
    }

    // Método para sacar dinheiro de uma conta
    public void sacar(String id, double valor) {
        Account contaDriver = buscarContaCorrentePorIDDriver(id);
        if (contaDriver != null) {
            if (contaDriver.sacar(valor)) {
                System.out.println("AlphaBank: Saque de " + valor + " da conta da MobilityCompany " + id + " realizado com sucesso.");
            } else {
                System.out.println("AlphaBank: Saque de " + valor + " da conta do motorista " + id + " não foi possível devido a saldo insuficiente.");
            }
        } else {
            System.out.println("AlphaBank: Conta do motorista " + id + " não encontrada.");
        }
    }

    // Método para depositar dinheiro em uma conta
    public void depositar(String idDriver, double valor) {
        Account contaDriver = buscarContaCorrentePorIDDriver(idDriver);
        if (contaDriver != null) {
            contaDriver.depositar(valor);
            System.out.println("AlphaBank: Depósito de " + valor + " na conta do motorista " + idDriver + " realizado com sucesso.");
        } else {
            System.out.println("AlphaBank: Conta do motorista " + idDriver + " não encontrada.");
        }
    }

    // Método para processar a solicitação de pagamento
    public void processarPagamento(String idCompany, String senhaCompany, String idDriver, double valor) {
        Account contaCompany = buscarContaCorrentePorIDDriver(idCompany);
        if (contaCompany != null && contaCompany.getSenha().equals(senhaCompany)) {
            if (contaCompany.sacar(valor)) {
                depositar(idDriver, valor);
                System.out.println("AlphaBank: Pagamento de " + valor + " realizado com sucesso da conta da empresa " + idCompany + " para a conta do motorista " + idDriver);
            } else {
                System.out.println("AlphaBank: Pagamento de " + valor + " da conta da empresa " + idCompany + " para a conta do motorista " + idDriver + " não foi possível devido a saldo insuficiente.");
            }
        } else {
            System.out.println("AlphaBank: Empresa não autorizada ou senha inválida.");
        }
    }

    // Método para mostrar o saldo de todas as contas
    public void mostrarSaldoContas() {
        System.out.println("Saldo das contas no AlphaBank:");
        for (Account conta : accounts) {
            System.out.println("Conta: " + conta.getNumeroConta() + ", Titular: " + conta.getLogin() + ", Saldo: " + conta.getSaldo());
        }
    }

}
