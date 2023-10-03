package client;

import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class ClientDriver {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Endereço do servidor
        int serverPort = 12345; // Porta do servidor

        try {
            Socket socket = new Socket(serverAddress, serverPort);

            // Crie fluxos de entrada e saída para o servidor
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Crie um objeto JSON com a solicitação de rota
            JSONObject request = new JSONObject();
            request.put("action", "getRoute"); // Ação que indica a solicitação de rota
            request.put("routeId", "ID1"); // ID da rota desejada

            // Envie a solicitação JSON para o servidor
            out.writeObject(request.toString());

            // Receba a resposta do servidor (edges da rota)
            String response = (String) in.readObject();
            JSONObject jsonResponse = new JSONObject(response);

            if (jsonResponse.has("edges")) {
                String edges = jsonResponse.getString("edges");
                System.out.println("Edges da rota: " + edges);
            } else {
                System.out.println("Rota não encontrada.");
            }

            // Feche os fluxos e o soquete
            in.close();
            out.close();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
