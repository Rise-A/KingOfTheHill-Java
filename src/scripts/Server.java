package scripts;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
	
	public static HashMap<ClientSession, Integer> clientList = new HashMap<>(); // Key is the client, value is the client's score
	public static String serverName = "Rise's Server";
	private static Server server = new Server();
	private static GameProtocol gameProtocol = new GameProtocol();
	
	private static long startTime = 0;	
	private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // needed to send regular messages to the whole server

	public static void main(String[] args) {
		if (args.length < 1) {
			return;
		}
		
		int port = Integer.parseInt(args[0]); // For the port argument
		
		try (ServerSocket serverSocket = new ServerSocket(port)){
			System.out.println("100 Server started with Port: " + port);
			
			startTime = System.currentTimeMillis();
			RoundManager.SetServer(server);
			
			PeriodicallyBroadcastServerInfo(scheduler, 60); // send server info every 60 seconds
			RoundManager.PeriodicallyBroadcastRoundInfo(scheduler, 1);

			GameProtocol.StartHillScoringLoop();
			
			while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("100 Client connected");            
                
                ClientSession client = new ClientSession(socket, gameProtocol, server);
//                clients.add(client);
                clientList.put(client, 0);
                
                new Thread(client).start();
			}
		} 
		catch (IOException e) {
//			for (TestClient c: clients) {
//				c.writer.println(GameProtocol.ServerDown(500));
//			}
			e.printStackTrace();
		}
	}
	
	private static void BroadcastServerInfo() {
		String message = GameProtocol.GameInfo(200, serverName, GameProtocol.gameMap.length, GameProtocol.gameMap[0].length, GetServerClockTime(), clientList.size());
		GameProtocol.BroadcastMessageFromServer("");
		GameProtocol.BroadcastMessageFromServer(message);
	}
	
	private static void PeriodicallyBroadcastServerInfo(ScheduledExecutorService scheduler, int timeInterval) {
		scheduler.scheduleAtFixedRate(Server::BroadcastServerInfo, 0, timeInterval, TimeUnit.SECONDS);
	}
	
	public static long GetServerClockTime() {
		return (System.currentTimeMillis() - startTime) / 1000;
	}
}
