import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ConnexionSendFluxWebcam  implements Runnable {
	private ServerRobotino serverRobotino;
	private WaitNewConnexionSendFluxWebcam waitNewConnexionSendFluxWebcam;
	public Socket socketClient;
	public PrintWriter out;
	private BufferedReader in;
	//public String ipWeb;
	public String nameWebcam = "";
	
	
	//INUTILE
	public ConnexionSendFluxWebcam(ServerRobotino serverRobotino, Socket socketClient, String firstLine, BufferedReader in) {
		try {
			this.out = new PrintWriter(socketClient.getOutputStream(), true);
			this.in = in;
			this.serverRobotino=serverRobotino;
			this.socketClient=socketClient;
			System.out.println("CSFW\tNE DOIT PAS ETRE AFFICHER firstLine: "+firstLine);
			//out.println("{\"type\":\"init\",\"infoInit\":\"Connection accept�\"}");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*try {
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No nameWebcam found, default name set: ");
		}*/
		out.println("HTTP/1.0 200 OK");
		out.println("Connection: close");
		out.println("Max-Age: 0");
		out.println("Expires: 0");
		out.println("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
		out.println("Content-Type: multipart/x-mixed-replace; boundary=stream");
		out.println("");
		//waitNewConnexionSendFluxWebcam.addConnexionSendFluxWebcam(this);
		//this.run();

	}
	
	
	
	public ConnexionSendFluxWebcam(WaitNewConnexionSendFluxWebcam waitNewConnexionSendFluxWebcam, Socket socketClient) {
		try {
			this.out = new PrintWriter(socketClient.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
			this.serverRobotino=waitNewConnexionSendFluxWebcam.serverRobotino;
			this.socketClient=socketClient;
			this.waitNewConnexionSendFluxWebcam=waitNewConnexionSendFluxWebcam;
			//out.println("{\"type\":\"init\",\"infoInit\":\"Connection accept�\"}");
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*try {
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No nameWebcam found, default name set: ");
		}*/
		out.println("HTTP/1.0 200 OK");
		out.println("Connection: close");
		out.println("Max-Age: 0");
		out.println("Expires: 0");
		out.println("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
		out.println("Content-Type: multipart/x-mixed-replace; boundary=stream");
		out.println("");
		//waitNewConnexionSendFluxWebcam.addConnexionSendFluxWebcam(this);
		//this.run();

	}
	public void run() {
		waitNewConnexionSendFluxWebcam.addConnexionSendFluxWebcam(this);
		System.out.println("CSFW\test run: ");
		try {
			String inLine = "";
			while(this.serverRobotino.isServerRunning()&&inLine!=null){//lecture des nouveau message
				inLine = in.readLine();
				//System.out.println("CSFW\tgetIntputStreamServer2: "+inLine);
				//this.decodeurJson(inLine);
			}
		} catch (IOException e) {/*e.printStackTrace();*/}//connexion ferm�
		waitNewConnexionSendFluxWebcam.removeConnexionSendFluxWebcam(this);
		//System.out.println("CSFW\ttest fin de conection par rupture de connexion: ");
		//System.out.println("waitNewConnexionSendFluxWebcam.connexionsSendFluxWebcam.size()"+waitNewConnexionSendFluxWebcam.connexionsSendFluxWebcam.size());
		//removeConnexionSendFluxWebcam();
	}
	/*public void removeConnexionSendFluxWebcam(){
		try {
			System.out.println("333"+waitNewConnexionSendFluxWebcam);
			waitNewConnexionSendFluxWebcam.removeConnexionSendFluxWebcam(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	public void envoyerBytes(byte[] bytes){
		try {
			socketClient.getOutputStream().write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
