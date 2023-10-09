package simulation.versao_anterior;

import org.json.JSONObject;

public class ComunicationJson {
    // O destinatário é entrada para o método JSON
    // Método onde o Driver faz a requisição de uma rota para o CompanyServer
    public static String createRouteRequest(String destination) {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("requestType", "route");
        jsonRequest.put("destination", destination);
        return jsonRequest.toString();
    }
}
