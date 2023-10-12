package app.financeiro;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class Banco extends Thread implements Serializable{
    private String nome;
    private ServerSocket bancoSocket; // para a conexão com o cliente

    public void start(int PORT){
        try {
            bancoSocket = new ServerSocket(PORT);
            System.out.println("Servidor do Banco aguardando conexões...");
            while (true) {
                // aguarda e aceita conexões de clientes
                Socket clienteSocket = bancoSocket.accept();
                System.out.println("Cliente conectado " + clienteSocket.getInetAddress());
                
                // cria uma nova thread para lidar com o cliente 
                // assim é possível lidar com vários clientes ao mesmo tempo
                AcessoMultiplo clienteHandler = new AcessoMultiplo(clienteSocket);
                clienteHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNome() {
        return nome;
    }

}
