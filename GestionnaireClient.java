import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    private Date h = new Date();

    DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT
    );

    private BufferedWriter logWritter;
    private FileWriter fW;
    private File file = new File("log.txt");




    public GestionnaireClient(Socket s){
        try {
            
            if(!file.exists()){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            this.socket = s;
            this.bR = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.bW = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.userName = this.bR.readLine();
            gClientList.add(this);
            broadcastMessage("[Server]("+shortDateFormat.format(h)+") : Un nouvelle utilisateur : "+ this.userName + " vient de se connecter.");

        }
        catch (IOException err) {
            closeAll(this.socket,this.bR,this.bW,this.logWritter);

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
                closeAll(socket,bR,bW,logWritter);
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
        System.out.println(msg);
        try {
            this.fW = new FileWriter(file.getAbsoluteFile(),true);
            this.logWritter = new BufferedWriter(fW);
            this.logWritter.write(msg);
            this.logWritter.newLine();
            this.logWritter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (containCommand(msg)) {
            commandHandler(msg, gClientList, socket, bR, bW, logWritter);
        }else{
            for (int i = 0; i < gClientList.size(); i++) {
                try {
                    if (gClientList.indexOf(this) != i) {
                        gClientList.get(i).bW.write(msg);
                        gClientList.get(i).bW.newLine();
                        //Vide le flux de sortie et force l'écriture de tous les octets de sortie mis en mémoire tampon. 
                        gClientList.get(i).bW.flush();
                    }
    
                }
                catch (IOException err) {
                    closeAll(this.socket,this.bR,this.bW,this.logWritter);
                }
            }
        }
    }
    

    public void clientLeft(){
        gClientList.remove(this);
        broadcastMessage("[Server] : Un client ("+this.userName+") a quitter le serveur :(");
    }
    

    public void closeAll(Socket socket,BufferedReader bR,BufferedWriter bW,BufferedWriter logWritter){
        clientLeft();
        try {

            if (bR != null) {
                bR.close();
            }

            if (bW != null) {
                bW.close();
            }

            if (logWritter != null) {
                logWritter.close();
            }
            
            if (socket != null) {
                socket.close();
            }

        }
        catch (IOException err) {
            err.printStackTrace();
        }
    }


    public boolean containCommand(String msg){
        return msg.contains(": /");
    }


    public void commandHandler(String cmd,ArrayList<GestionnaireClient> gC, Socket s,BufferedReader bR,BufferedWriter bW,BufferedWriter logWritter){
        String[] commande = cmd.split("/");
        for (int i = 0; i < commande.length; i++) {
            if (commande[i].equals("quit")) {
                closeAll(socket, bR, bW, logWritter); 
            }
            if (commande[i].contains("kick")){
                String kicked = cmd.split("kick")[1];
                for (int j = 0; j < gC.size(); j++) {
                    System.out.println(gC.get(j).userName+"=="+kicked.replaceAll("\\s", ""));
                    if (gC.get(j).userName.equals(kicked.replaceAll("\\s", ""))) {
                        broadcastMessage(gC.get(j).userName+" vient de se faire kick.");
                        closeAll(gC.get(j).socket,gC.get(j).bR, gC.get(j).bW, gC.get(j).logWritter);
                    }
                }
            }
            if (commande[i].equals("help")) {
                int ind = gClientList.indexOf(this);
                try{
                    gClientList.get(ind).bW.write("Voici les commandes :\n/quit : Permet de fermer le programme.\n/kick : (Bug)Permet de kick un utilisateur.");
                    gClientList.get(ind).bW.newLine();
                    //Vide le flux de sortie et force l'écriture de tous les octets de sortie mis en mémoire tampon. 
                    gClientList.get(ind).bW.flush();
                }
                catch(IOException e){
                    e.printStackTrace();
                }
            }



        }
    }
    



}
