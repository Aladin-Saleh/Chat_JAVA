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

    //Permet d'envoyer les données aux autres clients.
    private BufferedWriter bW;
    
    private Socket socket;
    
    //Nom de l'utilisateur.
    private String userName;


    public Client(Socket s,String uN){
        try {
            this.socket = s;
            this.userName = uN;
            this.bR = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.bW = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        }
        catch (IOException err) {
            err.printStackTrace();
        }
    }



    public void sendMessage(){

        try {
            bW.write(this.userName);
            bW.newLine();
            //Vide le flux de sortie et force l'écriture de tous les octets de sortie mis en mémoire tampon. 
            bW.flush();

            //Ecoute de l'entrée de client.
            Scanner sc = new Scanner(System.in);
            while(this.socket.isConnected()){
                System.out.print("["+this.userName+"] : ");

                String msgSend = sc.nextLine();
                bW.write("From ["+this.userName+"] : " + msgSend);
                bW.newLine();
                bW.flush();
            }
    
        }
        catch (IOException err) {
            err.printStackTrace();
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
                    }
                }                
            }
            
        }).start();
    }



    public static void main(String[] args) {
        String userName;
        Socket socket;
        

        Scanner sc = new Scanner(System.in);
        System.out.println("Entrer votre identifiant : ");
        userName = sc.nextLine();

        try {
            socket = new Socket(InetAddress.getLocalHost(),2727);
            Client client = new Client(socket, userName);
            client.listen();
            client.sendMessage();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }



}
