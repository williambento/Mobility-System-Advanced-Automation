package api.json;

import org.json.JSONObject;

import api.car.Cars;

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
    public static String solicitarRota(String _request, String _id, String _senha) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        return json.toString();
    }

    // metodo para finalizar rota
    public static String finalizar(String _request) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
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
            String[] resultado = new String[]{requisicao, loginDriver, senhaDriver};
            return resultado;
        } else if (requisicao.equals("fim")){
            String[] resultado = new String[]{requisicao};
            return resultado;
        } else if (requisicao.equals("carDados")){
            String idCar = jsonObject.getString("idCar");
            String classCar = jsonObject.getString("classeCar");
            String[] resultado = new String[]{requisicao, idCar, classCar};
            return resultado;
        }

        String[] resultado = new String[]{requisicao};
        return resultado;
    }

    public static String dadosCar(String _request, String _idCar, Class<? extends Cars> _class1) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("idCar", _idCar);
        json.put("classeCar", _class1);
        return json.toString();
    }
}
