package app.transporte;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class EmpresaMobilidade extends Thread implements Serializable {
    private String nome;
    private ServerSocket empresaSocket;

    public void start(int PORT){
        try {
            empresaSocket = new ServerSocket(PORT);
            System.out.println("Servidor da Empresa de Mobilidade aguardando conexões...");
            while (true) {
                // aguarda e aceita conexões de clientes
                Socket clienteSocket = empresaSocket.accept();
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
