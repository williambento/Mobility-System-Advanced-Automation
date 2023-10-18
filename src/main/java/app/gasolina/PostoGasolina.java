package app.gasolina;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class PostoGasolina implements Serializable{
    private String nome;
    private double valorGasolina;
    private ServerSocket postoSocket;

    public void start(int PORT){
        try {
            postoSocket = new ServerSocket(PORT);
            System.out.println("Servidor do Posto de Gasolina aguardando conexões...");
            while (true) {
                // aguarda e aceita conexões de clientes
                Socket clienteSocket = postoSocket.accept();
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
