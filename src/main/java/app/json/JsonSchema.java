package app.json;

import org.json.JSONObject;

public class JsonSchema {

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
    public static String buscarConta(String _request, String _id, String _senha) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        return json.toString();
    }

    // padrao de requisição para criar conta no Banco
    public static String pagar(String _request, String _id, String _senha, String _idDestino, double _valor) {
        JSONObject json = new JSONObject();
        json.put("request", _request);
        json.put("motorista", _id);
        json.put("senha", _senha);
        json.put("posto", _idDestino);
        json.put("valor", _valor);
        return json.toString();
    }

    // converte dados vindos da requisição criar conta
    public static String[] convertJsonString(String _objeto){
        // Crie um objeto JSONObject a partir da string
        JSONObject jsonObject = new JSONObject(_objeto);
        // acessa cada elemento individual
        String requisicao = jsonObject.getString("request");
        String motorista = jsonObject.getString("motorista");
        String senha = jsonObject.getString("senha");
        // se a requisição for do tipo pagar, novos campos são adicionados
        /*if("saldo".equals(requisicao)){
            String posto = jsonObject.getString("posto");
            String valor = jsonObject.getString("valor");
            String[] resultado = new String[]{motorista, requisicao, senha, posto, valor};
            return resultado;
        }*/
        // cria um array de strings e coloque os valores nele
        String[] resultado = new String[]{motorista, requisicao, senha};
        // retorna o array de strings
        return resultado;
    }
}
