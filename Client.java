import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    //Permet de recevoir les données des autres clients.
    private BufferedReader bR;

    private BufferedReader lectureEcran;

    //Permet d'envoyer les données aux autres clients.
    private BufferedWriter bW;
    
    private Socket socket;
    
    //Nom de l'utilisateur.
    private String userName;


    public Client(Socket s,String uN){
        try {
            this.socket = s;
            this.lectureEcran = new BufferedReader(new InputStreamReader(System.in));
            this.bR = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.bW = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.userName = uN;
        }
        catch (IOException err) {
            err.printStackTrace();
            closeAll(this.socket,this.bR,this.bW);
        }
    }



    public void sendMessage(){

        try {
            bW.write(this.userName);
            bW.newLine();
            //Vide le flux de sortie et force l'écriture de tous les octets de sortie mis en mémoire tampon. 
            bW.flush();

            //Ecoute de l'entrée de client.
            //Scanner sc = new Scanner(System.in);
            while(this.socket.isConnected()){
                String msgSend = this.readScreen();
                if (msgSend.equals("/quit")) {
                    System.exit(0);
                }
                bW.write("["+this.userName+"]("+this.socket.getInetAddress()+") : " + msgSend);
                bW.newLine();
                bW.flush();
            }
    
        }
        catch (IOException err) {
            err.printStackTrace();
            closeAll(this.socket,this.bR,this.bW);
        }


    }

    /**
     * Thread qui ecoute les autres clients.
     * L'utilisation de thread permet de gerer la concurrence.
     */
    public void listen(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                String otherClientMsg;
                while(socket.isConnected()){
                    try {
                        otherClientMsg = bR.readLine();
                        System.out.println(otherClientMsg);
                    }
                    catch (IOException err) {
                        err.printStackTrace();
                        closeAll(socket,bR,bW);
                    }
                }                
            }
            
        }).start();
    }

    public void closeAll(Socket socket,BufferedReader bR,BufferedWriter bW){
        try {
            if (socket != null) {
                socket.close();
            }
            if (bR != null) {
                bR.close();
            }

            if (bW != null) {
                bW.close();
            }
        }
        catch (IOException err) {
            err.printStackTrace();
        }
    }

    //Rmplace le Scanner.
    public String readScreen(){
        try {
            return this.lectureEcran.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }



    public static void main(String[] args) throws UnknownHostException, IOException {
        String userName;
        Socket socket;
        

        Scanner sc = new Scanner(System.in);
        System.out.println("Entrer votre identifiant : ");
        userName = sc.nextLine();

        socket = new Socket(InetAddress.getLocalHost(),2727);
        Client client = new Client(socket, userName);
        client.listen();
        client.sendMessage();




    }



}
