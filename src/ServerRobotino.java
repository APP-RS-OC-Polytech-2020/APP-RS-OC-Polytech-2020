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

/**
 * Classe qui represente le serveur principal.
 * Tout le monde s'y connecte, il redistribue les messages, 
 * ouvre les connexions qui vont bien toussa.
 * 
 *
 */
public class ServerRobotino {
	private int portServeur;
	private ServerSocket socketServer = null;
	private ArrayList<ConnexionJava> connexionsJava = new ArrayList<ConnexionJava>();
	private ArrayList<ConnexionRobotino> connexionsRobotino = new ArrayList<ConnexionRobotino>();
	private ArrayList<ConnexionWeb> connexionsWeb = new ArrayList<ConnexionWeb>();
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
			System.out.println("Server lancé");
			while(serverRunning){
				Socket socketClient = socketServer.accept();//Quelque chose essai de se connecter
				BufferedReader in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));;
				String firstLine = in.readLine();
				if(serverRunning){
					if(firstLine.startsWith("{")){//connexion classique en java,on passe à la suite
						new Thread(new ConnexionJava(this,socketClient,firstLine,in)).start();
					}else if(firstLine.startsWith("GET")){ //Ca commence par GET, c'est une Websocket
						new Thread(new ConnexionWeb(this,socketClient,firstLine,in)).start();
					}
					else{
						PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);
						out.println("L( ¨° 3¨° )J----#:`* no socket for u");
						socketClient.close();
					}
				}else{
					new PrintWriter(socketClient.getOutputStream(), true).println("Connexion canceled cause server is stopping");
					socketClient.close();
				}
			}
		} catch (IOException e) {
			System.out.println("Arrêt d'écoute de nouvelle connexion");
			//e.printStackTrace();//affiche erreur en cas d'arrêt forcé
		}
	}
	public boolean isServerRunning() {
		return serverRunning;
	}
	public void setServerRunning(boolean serverRunning) {
		this.serverRunning = serverRunning;
	}
	public synchronized void addConnexionJava(ConnexionJava connexion) {
		this.connexionsJava.add(connexion);
	}
	public synchronized void removeConnexionJava(ConnexionJava connexion) {
		this.connexionsJava.remove(connexion);
	}
	public synchronized void addConnexionRobotino(ConnexionRobotino connexion) {
		this.connexionsRobotino.add(connexion);
	}
	public synchronized void removeConnexionRobotino(ConnexionRobotino connexion) {
		this.connexionsRobotino.remove(connexion);
	}
	public synchronized void addConnexionWeb(ConnexionWeb connexion) {
		this.connexionsWeb.add(connexion);
	}
	public synchronized void removeConnexionWeb(ConnexionWeb connexion) {
		this.connexionsWeb.remove(connexion);
	}
}
