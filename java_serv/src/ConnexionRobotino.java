import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

/**
 * G�re la connexion d'un robotino au server
 * @author lalandef
 *
 */
public class ConnexionRobotino implements Runnable {
	private ServerRobotino serverRobotino;
	private Socket socketClient;
	private PrintWriter out;
	private BufferedReader in;
	public String ipRobot;
	public String linkVideo;
	public ConnexionRobotino(ServerRobotino serverRobotino, Socket socketClient, String firstLine, BufferedReader in) {
		try {
			this.out = new PrintWriter(socketClient.getOutputStream(), true);
			this.in = in;
			out.println("{\"type\":\"init\",\"infoInit\":\"Connection accept�\"}");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.serverRobotino=serverRobotino;
		this.socketClient=socketClient;
		
		System.out.println("CoRobot\tgetIntputStreamServer: "+firstLine);
		JSONObject JSON = new JSONObject(firstLine);
		String info = JSON.getString("infoInit");
		try{
			this.ipRobot = JSON.getString("ipRobot");
		}catch(org.json.JSONException e){
			ipRobot=socketClient.getInetAddress().toString();
			System.out.println("CoRobot\tPas d'ip envoy�: "+e);
			System.out.println("CoRobot\tJSON: "+firstLine);
		}
		System.out.println("ipRobot:"+ipRobot);
		System.out.println("CoRobot\tinfo: "+info);
	}

	/**
	 * Initie la connexion et attend les requ�te du robotino
	 */
	@Override
	public void run() {
		serverRobotino.addConnexionRobotino(this);
		try {
			String inLine = "";
			while(this.serverRobotino.isServerRunning()&&inLine!=null){//lecture des nouveau message
				inLine = in.readLine();
				System.out.println("CoRobo\tgetIntputStreamServer2: "+inLine);
				this.decodeurJson(inLine);
			}
		} catch (IOException e) {/*e.printStackTrace();*/}//connexion ferm�
		System.out.println("CoRobo\ttest fin de conection par rupture de connexion: ");
		serverRobotino.removeConnexionRobotino(this);
	}

	/**
	 * Decode un message JSON pour pouvoir l'utiliser et le traite
	 * pour les autres utilisateurs/le serveur.
	 * @param j Message JSON aux format texte
	 */
	public void decodeurJson(String j) {
		try{
			JSONObject JSON = new JSONObject(j);
			String type = JSON.getString("type");
			System.out.println("CoRobot\ttype:"+type);
			
			if(type.equals("init")){//inutilis� ici, uniquement au d�but de la classe connexion
				String info = JSON.getString("infoStart");
				System.out.println("CoRobot\tinfo: "+info);
				
			}else if(type.equals("message")){//message
				String message = JSON.getString("message");
				System.out.println("CoRobo\tMessage: "+message);
			}else if(type.equals("video")){//message
				linkVideo= JSON.getString("link");
				serverRobotino.sendToAllWeb(j);
			}
		}catch(org.json.JSONException e){
			System.out.println("CoRobot\terreur decodage JSON: "+e);
			System.out.println("CoRobot\tJSON: "+j);
		}
	}
	
	/**
	 * Envoie un message JSON aux autre utilisateur(Un ou plusieur selon ce qui est pr�cis� dans le message)
	 * @param m message JSON � envoyer
	 */
	public void envoyerMessage(String m){
		out.println(m);
	}

}
