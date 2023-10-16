package app.json;

import javax.print.DocFlavor.STRING;

import org.json.JSONObject;

public class JsonSchema {

    // dados carro
    public static String carDados(String _request, String idAuto, double co2Emission, double distanciaPercorrida) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("idAuto", idAuto);
        json.put("CO2Emission", co2Emission);
        json.put("distanciaPercorrida", distanciaPercorrida);
        return json.toString();
    }

    // solicitar rota
    public static String solicitarRota(String _request, String _id, String _senha) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        return json.toString();
    }

    // abastecer
    public static String abastecer(String _request, String _id, String _senha) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        return json.toString();
    }

    // padrao de requisição para criar conta no Banco
    public static String criarConta(String _request, String _id, String _senha) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        return json.toString();
    }

    // padrao de requisição para buscarContar
    public static String buscarConta(String _request, String _id, String _senha, int _litros) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        json.put("litros", _litros);
        return json.toString();
    }

    // padrao de requisição para criar conta no Banco
    public static String pagar(String _request, String _idCompany, String _senha, String _idDriver, double _valor) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("empresa", _idCompany);
        json.put("senha", _senha);
        json.put("driver", _idDriver);
        json.put("valor", _valor);
        return json.toString();
    }
    
    // converte dados vindos da requisição criar conta
    public static String[] convertJsonString(String _objeto){
        // Crie um objeto JSONObject a partir da string
        JSONObject jsonObject = new JSONObject(_objeto);
        // acessa cada elemento individual
        String requisicao = jsonObject.getString("request");
        if (requisicao.equals("rota")){
            String motorista = jsonObject.getString("motorista");
            String senha = jsonObject.getString("senha");
            String[] resultado = new String[]{requisicao, motorista, senha};
            return resultado;
        } else if (requisicao.equals("dataCar")) {
            String idCar = jsonObject.getString("idAuto");
            double co2 = jsonObject.getDouble("CO2Emission");
            double distancia = jsonObject.getDouble("distanciaPercorrida");
            String co2Str = Double.toString(co2);
            String distanciaStr = Double.toString(distancia);
            // cria um array de strings e coloque os valores nele
            String[] resultado = new String[]{requisicao, idCar, co2Str, distanciaStr};
            return resultado;
        } else if (requisicao.equals("pagar")){
            String company = jsonObject.getString("empresa");
            String senha = jsonObject.getString("senha");
            String driver = jsonObject.getString("driver");
            double valor = jsonObject.getDouble("valor");
            String valorStr = Double.toString(valor);
            String[] resultado = new String[]{requisicao, company, senha, driver, valorStr};
            return resultado;
        } else if (requisicao.equals("abastecer")){
            String driver = jsonObject.getString("motorista");
            String senha = jsonObject.getString("senha");
            int litros = jsonObject.getInt("litros");
            String litrosStr = Integer.toString(litros);
            String[] resultado = new String[]{requisicao, driver, senha, litrosStr};
            return resultado;
        }
        String[] resultado = new String[]{requisicao};
        return resultado;
    }

    // padrao de requisição para buscarContar
    public static String finalizar(String _request) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        return json.toString();
    }
}
