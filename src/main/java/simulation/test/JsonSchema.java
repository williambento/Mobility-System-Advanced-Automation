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

    public static String carDadosJson(String idAuto, double co2Emission, double distanciaPercorrida) {
        JSONObject json = new JSONObject();
        json.put("idAuto", idAuto);
        json.put("CO2Emission", co2Emission);
        json.put("DistanciaPercorrida", distanciaPercorrida);
        return json.toString();
    }

    public static String criarConta(String _id, String _senha){
        JSONObject json = new JSONObject();
        json.put("idDriver", _id);
        json.put("senha", _senha);
        return json.toString();
    }

    public static String pagar(String _id, String _senha, double _valor, String _idDestino){
        JSONObject json = new JSONObject();
        json.put("idOrigem", _id);
        json.put("senha", _senha);
        json.put("valor", _valor);
        json.put("idDestino", _idDestino);
        return json.toString();
    }
}
