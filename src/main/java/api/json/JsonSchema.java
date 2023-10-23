package api.json;

import org.json.JSONObject;

public class JsonSchema {
    // padrao de requisição para criar conta
    public static String criarConta(String _request, String _login, String _senha, double _deposito) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("login", _login);
        json.put("senha", _senha);
        json.put("deposito", _deposito);
        return json.toString();
    }

    // solicitar rota
    public static String solicitarRota(String _request, String _id, String _senha, int _rangeRoutes) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        json.put("rangeRotas", _rangeRoutes);
        return json.toString();
    }

    // metodo para finalizar rota
    public static String finalizar(String _request) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        return json.toString();
    }

    // abastecer
    public static String abastecer(String _request, String _id, String _senha, double _litros) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        json.put("litros", _litros);
        return json.toString();
    }

    // converte mensagens vindas dos em dados separados
    public static String[] convertJsonString(String _objeto){
        // cria-se um objeto JSONObject a partir da string
        JSONObject jsonObject = new JSONObject(_objeto);
        // acessa cada elemento individual

        String requisicao = jsonObject.getString("request");
        // quando for uma requisição do tipo criar conta
        if (requisicao.equals("criarConta")){
            String loginCompany = jsonObject.getString("login");
            String senhaCompany = jsonObject.getString("senha");
            double depositoInicial = jsonObject.getDouble("deposito");
            String depositoInicialStr = Double.toString(depositoInicial);
            String[] resultado = new String[]{requisicao, loginCompany, senhaCompany, depositoInicialStr};
            return resultado;
        } else if (requisicao.equals("rota")){
            String loginDriver = jsonObject.getString("motorista");
            String senhaDriver = jsonObject.getString("senha");
            int rangeRota = jsonObject.getInt("rangeRotas");
            String rangeRotaStr = Integer.toString(rangeRota);
            String[] resultado = new String[]{requisicao, loginDriver, senhaDriver, rangeRotaStr};
            return resultado;
        } else if (requisicao.equals("fim")){
            String[] resultado = new String[]{requisicao};
            return resultado;
        } else if (requisicao.equals("carDados")){
            String idCar = jsonObject.getString("idAuto");
            double CO2Emission = jsonObject.getDouble("CO2Emission");
            double distanciaPercorrida = jsonObject.getDouble("distanciaPercorrida");
            String CO2EmissionStr = Double.toString(CO2Emission);
            String distanciaPercorridaStr = Double.toString(distanciaPercorrida);
            String[] resultado = new String[]{requisicao, idCar, CO2EmissionStr, distanciaPercorridaStr};
            return resultado;
        } else if (requisicao.equals("pagar")){
            String loginCompany = jsonObject.getString("login");
            String senhaCompany = jsonObject.getString("senha");
            String idDriver = jsonObject.getString("driver");
            double valorPagar = jsonObject.getDouble("pagamento");
            String valorPagarStr = Double.toString(valorPagar);
            String[] resultado = new String[]{requisicao, loginCompany, senhaCompany, idDriver, valorPagarStr};
            return resultado;
        } else if (requisicao.equals("abastecer")){
            String loginCompany = jsonObject.getString("motorista");
            String senhaCompany = jsonObject.getString("senha");
            double litros = jsonObject.getDouble("litros");
            String litrosStr = Double.toString(litros);
            String[] resultado = new String[]{requisicao, loginCompany, senhaCompany, litrosStr};
            return resultado;
        }

        String[] resultado = new String[]{requisicao};
        return resultado;
    }

    public static String pagar(String _request, String _idCompany, String _senha, String _idDriver, double _valor){
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("login", _idCompany);
        json.put("senha", _senha);
        json.put("driver", _idDriver);
        json.put("pagamento", _valor);
        return json.toString();
    }

    // dados carro
    public static String carDados(String _request, String idAuto, double co2Emission, double distanciaPercorrida) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("idAuto", idAuto);
        json.put("CO2Emission", co2Emission);
        json.put("distanciaPercorrida", distanciaPercorrida);
        return json.toString();
    }
}
