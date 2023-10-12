package app.motoristas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Motoristas extends Thread implements Serializable{
    private ArrayList<Motorista> listaMotoristas;

    public Motoristas(){
        listaMotoristas = new ArrayList<Motorista>();
        gerarMotoristas();
    }

    // aqui uma nova thread é gerada e roda o que está dentro do run
    public void start(String HOST, int PORT, String _id, String _senha, int _opcao){
        conectaBanco(HOST, PORT, _id, _senha, _opcao);
    }

    // criar conexao com o Banco
    public void conectaBanco(String _host, int PORT, String _id, String _senha, int _opcao){
        ExecutorService executor = Executors.newFixedThreadPool(100); // Cria um pool de threads com 100 threads
        try {
            for (Motorista motorista : listaMotoristas) {
                executor.execute(new ConectServer(_host, PORT, motorista, _id, _senha, _opcao));
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown(); // Aguarda a conclusão de todas as threads
        while (!executor.isTerminated()) {
        }
    }
    /*public void conectaBanco(String _host, int PORT, String _id, String _senha, int _opcao){
        try {
         
            ConectServer motorista = new ConectServer(_host, PORT, _id, _senha, _opcao);
            Thread.sleep(100);
        
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    // aqui inicializa os 100 motoristas e armazena no Array, sendo identificados pelo ID
    public void gerarMotoristas(){
        for(int i = 0; i < 100; i++){
            String id =  "DRIVER" + (i + 1);
            String senha = "a" + i;
            Motorista motorista =  new Motorista(id, senha);
            listaMotoristas.add(motorista);
        }
    }

    // retornar a lista de motoristas cadastrados
    public ArrayList<Motorista> retornaListaMotoristas(){
        return listaMotoristas;
    }

}
