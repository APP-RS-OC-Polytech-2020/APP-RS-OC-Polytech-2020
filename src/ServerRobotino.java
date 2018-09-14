import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class ServerRobotino {
	private int portServeur;
	private ServerSocket socketServer = null;
	private ArrayList<ConnexionJava> connexions = new ArrayList<ConnexionJava>();
	//private ArrayList<Connexion> connexionsRobotino = new ArrayList<Connexion>();

	//private Thread t1;
	private boolean serverRunning = true;
	public ServerRobotino(int port) {
		try {
			this.portServeur=port;
			//ip = InetAddress.getLocalHost ().getHostAddress ();
			//nom = "Server Robotion v1";
			socketServer = new ServerSocket(this.portServeur);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.waitNewConnexion();
	}
	private void waitNewConnexion() {
		try {
			System.out.println("Server lanc�");
			while(serverRunning){
				Socket socketClient = socketServer.accept();//Quelque chose essai de se connecter
				String firstLine =new BufferedReader(new InputStreamReader(socketClient.getInputStream())).readLine();//on regarde la premi�re ligne que nous envoi la client
				if(serverRunning){
					if(firstLine.startsWith("{")){//connexion classique en java,on passe � la suite
						new Thread(new ConnexionJava(this,socketClient,firstLine)).start();
					}
				}else{
					new PrintWriter(socketClient.getOutputStream(), true).println("Connexion canceled cause server is stopping");
					socketClient.close();
				}
			}
		} catch (IOException e) {
			System.out.println("Arr�t d'�coute de nouvelle connexion");
			//e.printStackTrace();//affiche erreur en cas d'arr�t forc�
		}
	}
	public boolean isServerRunning() {
		return serverRunning;
	}
	public void setServerRunning(boolean serverRunning) {
		this.serverRunning = serverRunning;
	}
}