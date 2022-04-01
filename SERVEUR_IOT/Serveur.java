
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;



public class Serveur {



    public static void main(String[] args) {
        try {
            // Création du serveur
            ServerSocket serverSocket = new ServerSocket(8060);
            System.out.println("Serveur lancé sur le port 8060");
            int indice = 0;
            
            while(!serverSocket.isClosed()){
                // Attente d'une connexion
                Socket socket = serverSocket.accept();
                System.out.println("Connexion établie");
                // Création d'un flux d'entrée
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // Création d'un flux de sortie
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                //Affichage du bufferedReader
                
                String line = in.readLine();
                while(line != null){
                    //System.out.println(line);
                    if(indice == 8){
                        String[] l = line.split(":");
                        String decodedPayloadLine = l[30] + " " + l[31] + " " + l[32] + " " + l[33].split(",")[0];
                        System.out.println(decodedPayloadLine);
                    }
                    line = in.readLine();
                    indice++;
                }


            }
            
            
           // socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
