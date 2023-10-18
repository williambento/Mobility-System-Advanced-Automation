package api.car;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

public class Car extends Thread implements Serializable {

    public void run(){
        Socket motoristaSocket;
        try {
            motoristaSocket = new Socket("127.0.0.1", 2000);
            // entrada e saida de dados
            DataInputStream input = new DataInputStream(motoristaSocket.getInputStream());
            DataOutputStream output = new DataOutputStream(motoristaSocket.getOutputStream());
            ObjectInputStream objeto = new ObjectInputStream(motoristaSocket.getInputStream());
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
