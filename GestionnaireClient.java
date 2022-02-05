import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class GestionnaireClient implements Runnable{

    //Membre statique pour qu'il n'appartienne qu'a la classe et pas aux objet de la classe.
    //Permet de parcourir tout les clients et d'y acceder. (pour envoyer les messages à tout les clients).
    private static ArrayList<GestionnaireClient> gClientList = new ArrayList<>();


    //Permet de recevoir les données des autres clients.
    private BufferedReader bR;
    //Permet d'envoyer les données aux autres clients.
    private BufferedWriter bW;
    
    
    private Socket socket;

    //Nom de l'utilisateur.
    private String userName;





    public GestionnaireClient(Socket s){
        try {
            this.socket = s;
            this.bR = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.bW = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.userName = this.bR.readLine();
            gClientList.add(this);
        }
        catch (IOException err) {
            err.printStackTrace();
            close();
            

        }
    }


    @Override
    public void run() {
        String clientMessage;


        while(this.socket.isConnected()){
            try {
                clientMessage = this.bR.readLine();
                //Envoi aux autres client.
                broadcastMessage(clientMessage);
            }
            catch (IOException err) {
                err.printStackTrace();
                close();
                break;
            }
        }
        
    }

    /**
     * Envoie des messages d'un client à tout les autres clients.
     * @param msg Message envoyé en broadcast.
     */
    public void broadcastMessage(String msg){
        //Envoie du message à tout les clients sauf l'envoyeur.
        for (int i = 0; i < this.gClientList.size(); i++) {
            try {
                if (this.gClientList.indexOf(this) != i) {
                    this.gClientList.get(i).bW.write(msg);
                    this.gClientList.get(i).bW.newLine();

                    //Vide le flux de sortie et force l'écriture de tous les octets de sortie mis en mémoire tampon. 
                    this.gClientList.get(i).bW.flush();
                }



            }
            catch (IOException err) {
                err.printStackTrace();
                close();
            }
        }



    }


    public void clientLeft(){
        this.gClientList.remove(this);
        broadcastMessage("[Server] : Un client ("+this.userName+") à quitter le serveur :(");
    }
    

    public void close(){
        try {
            clientLeft();
            this.bR.close();
            this.bW.close();
            this.socket.close();
        }
        catch (IOException err) {
            err.printStackTrace();
        }
    }



}
