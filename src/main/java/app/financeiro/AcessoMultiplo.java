package app.financeiro;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import app.json.JsonSchema;
import app.criptografia.Crypto;

public class AcessoMultiplo extends Thread {
    private Socket clienteEmpresaSocket;
    private Socket clienteDriverSocket;
    private int numeroConta;
    private ArrayList<ContaCorrente> contasMotoristas;

    public AcessoMultiplo(Socket clienteSocket) {
        this.clienteEmpresaSocket = clienteSocket;
        this.contasMotoristas = new ArrayList<ContaCorrente>();
    }

    @Override
    public void start() {
        try {
            // entrada e saida de dados para o cliente empresa
            DataInputStream inputEmpresa = new DataInputStream(clienteEmpresaSocket.getInputStream());
            DataOutputStream outputEmpresa = new DataOutputStream(clienteEmpresaSocket.getOutputStream());

            // entrada e saida de dados para o cliente driver
            DataInputStream inputDriver = new DataInputStream(clienteEmpresaSocket.getInputStream());
            DataOutputStream outputDriver = new DataOutputStream(clienteEmpresaSocket.getOutputStream());

            // IMPLEMENTAR INTERAÇÃO COM O CLIENTE AQUI
            request(inputEmpresa, outputEmpresa, inputDriver, outputDriver);

            clienteEmpresaSocket.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void request(DataInputStream _in, DataOutputStream _out, DataInputStream _inDriver, DataOutputStream _outDriver){
        try {
            // recebendo a mensagem criptografada do cliente
            byte[] mensagemCriptografada = new byte[1024];
            int length = _in.read(mensagemCriptografada); // pega o tamanho
            byte[] mensagemCriptografadaBytes = new byte[length];
            System.arraycopy(mensagemCriptografada, 0, mensagemCriptografadaBytes, 0, length);
            
            // descriptografar a mensagem usando a classe Crypto
            byte[] mensagemDescriptografadaBytes = Crypto.decrypt(mensagemCriptografadaBytes, geraChave(), geraIv());
        
            // Converte a mensagem descriptografada para String
            String mensagemDescString = new String(mensagemDescriptografadaBytes);
            String[] resposta = JsonSchema.convertJsonString(mensagemDescString);
    
            //System.out.println(resposta[1]);
            if ("criarConta".equals(resposta[0])){
                criarConta(resposta[0], resposta[2], gerarNumeroConta());
                
                String msg = "Conta Criada!";
                byte[] envio = Crypto.encrypt(msg.getBytes(), geraChave(), geraIv());
                            
                // Envie a mensagem criptografada ao servidor
                _out.write(envio);
                _out.flush();
                
            } else if ("pagar".equals(resposta[0])){
                //transacao();
                System.out.println(resposta[0]);
                // Envie a mensagem criptografada ao servidor
                /*_out.write(envio);
                _out.flush();*/

            } else if ("saldo".equals(resposta[1])){
                ContaCorrente contaMotorista = buscarContaPorID(resposta[0]);
                //double saldo = contaMotorista.getSaldo();
                System.out.println(contaMotorista);
                // Converter o saldo em uma string
                //String saldoString = Double.toString(saldo);
                String saldoString = "Sucessful";
                //System.out.println(saldoString);
                byte[] envio = Crypto.encrypt(saldoString.getBytes(), geraChave(), geraIv());
                            
                // Envie a mensagem criptografada ao servidor
                _out.write(envio);
                _out.flush();
                
            }
                
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // criar Conta Corrente dos motoristas
    public void criarConta(String _login, String _senha, int _numero){
        ContaCorrente contaMotorista = new ContaCorrente(_login, _senha, _numero);
        contasMotoristas.add(contaMotorista);
        //System.out.println(contaMotorista);
    }

    // gera o numero da conta por Random
    private int gerarNumeroConta() {
        Random random = new Random();
        int numeroAleatorio = random.nextInt(9000) + 1000;
        return numeroAleatorio;
    }

    public ArrayList<ContaCorrente> getContasMotoristas(){
        return contasMotoristas;
    }

    // buscar conta por id
    public ContaCorrente buscarContaPorID(String id) {
        for (ContaCorrente conta : contasMotoristas) {
            if (conta.getLogin().trim().equalsIgnoreCase(id.trim())) {
                return conta; // Retorna a conta se encontrada
            }
        }
        return null; // Retorna null se a conta não for encontrada
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
