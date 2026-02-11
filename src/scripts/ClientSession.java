package scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class ClientSession extends Thread {

	private Socket socket;
	public Server server;
	
	public PrintWriter writer;
	public String userName = "";
	public int currentXCoord;
	public int currentYCoord;
	public boolean isLoggedIn = false;
	
	public ClientSession (Socket socket, GameProtocol gameProtocol, Server server) {
		this.socket = socket;
		this.server	= server;
	}
	
	@Override
	public void run() {
		try {
			OutputStream output = socket.getOutputStream(); // Handles client output
			writer = new PrintWriter(output, true);
			
			writer.println("300 Please login (Usage: LOGIN <username> )");
			writer.flush();
			
			InputStream input = socket.getInputStream(); // Handles client input
			BufferedReader reader = new BufferedReader(new InputStreamReader(input)); // Handles what the server sees
			
			GameProtocol.BeginProtocol(this, reader, writer);
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			Disconnect();
		}
	}
	
	public void Disconnect() {
		Server.clientList.remove(this);
		System.out.println("100 Client disconnected");
	}

}
