package simulation.test;

import org.json.JSONObject;

public class JsonSchema {

    public static String criarMensagem(String idRota, boolean solicitacao) {
        JSONObject json = new JSONObject();
        json.put("idRota", idRota);
        json.put("solicitacao", solicitacao);
        return json.toString();
    }

    public static String criarRotaJson(String idRota, String edges) {
        JSONObject json = new JSONObject();
        json.put("idRota", idRota);
        json.put("edges", edges);
        return json.toString();
    }

    public static String analisarMensagem(String mensagemJson, String campo) {
        JSONObject json = new JSONObject(mensagemJson);
        if (json.has(campo)) {
            return json.get(campo).toString();
        } else {
            return null; // Campo não encontrado
        }
    }
}
