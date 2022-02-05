import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;


public class Server{

    private ServerSocket serverSocket;
    private int nbrClient;

    public Server(ServerSocket ss){
        this.nbrClient = 0;
        this.serverSocket = ss;
    }


    public void startServer(){
        try {

            System.out.println("En attente de connexion...");
            while(!this.serverSocket.isClosed()){
                Socket sckt = this.serverSocket.accept();
                this.nbrClient++;
                System.out.println("[Server] : Un nouveau client de se connecter !");
                System.out.println("[Server] : Nombre de client : " + this.nbrClient);
                GestionnaireClient gClient = new GestionnaireClient(sckt);

                //Pour que le serveur n'est pas besoin d'attendre un message et se bloque on utilise un thread.
                Thread t = new Thread(gClient);
                t.start();
            }




            
        } catch (IOException e) {
            //Erreur serveur
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(2727);
            new Server(serverSocket).startServer();

        }
        catch (IOException err) {
            err.printStackTrace();
        }





    }





}