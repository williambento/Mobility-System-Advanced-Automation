package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.json.JSONObject;

import app.financeiro.AlphaBank;
import app.transporte.Company;
import app.transporte.Route;
import app.Sumo;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ArrayList<Route> availableRoutes;

    public ClientHandler(Socket socket, ArrayList<Route> routes) {
        this.clientSocket = socket;
        this.availableRoutes = routes;
    }

    @Override
    public void run() {
        try {
            // Crie fluxos de entrada e saída para o cliente
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

            // Aguarde a solicitação do cliente em formato JSON
            String requestJson = (String) in.readObject();
            JSONObject request = new JSONObject(requestJson);

            if (request.has("action") && request.getString("action").equals("getRoute")) {
                String routeId = request.getString("routeId");

                // Encontre a rota correspondente com base no routeId
                Route requestedRoute = findRouteById(routeId);

                if (requestedRoute != null) {
                    // Crie um objeto JSON para as edges da rota
                    JSONObject response = new JSONObject();
                    response.put("edges", requestedRoute.getEdges());

                    // Envie a resposta JSON para o cliente
                    out.writeObject(response.toString());
                    out.writeObject(this.simulacaoSUMO());
                } else {
                    // Caso a rota não seja encontrada
                    JSONObject response = new JSONObject();
                    response.put("error", "Rota não encontrada.");

                    // Envie uma resposta de erro JSON para o cliente
                    out.writeObject(response.toString());
                }
            }

            // Feche os fluxos e o soquete
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Um método fictício para encontrar a rota pelo ID
    private Route findRouteById(String routeId) {
        for (Route route : availableRoutes) {
            if (route.getRouteID().equals(routeId)) {
                return route;
            }
        }
        return null; // Rota não encontrada
    }

    public Sumo simulacaoSUMO(){
        AlphaBank Nubank = new AlphaBank();
        Company SevenGO = new Company(Nubank, "SevenGO", "mobilidade");
        /*SevenGO.start();*/
        try {
            SevenGO.join(); // Espera que a thread SevenGO termine.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Sumo Sumo = new Sumo(SevenGO);
        Sumo.start();
        return Sumo;
    }

}
