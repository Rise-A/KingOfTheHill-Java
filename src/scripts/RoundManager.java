package scripts;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RoundManager {
	
	private static int roundNum = 1;
	private static long startTime = 0;
	private static int maxRoundTime = 60;
	private static int currentRoundTimeLeft = maxRoundTime;
	
	private static Server server;
	
	private static void BroadcastRoundInfo() {
	    if (server == null) {
	    	return;
	    }

	    String message = GameProtocol.RoundInfo(200, roundNum, currentRoundTimeLeft);

	    for (ClientSession clientSession : Server.clientList.keySet()) {
	    	if (clientSession.isLoggedIn) {
		        clientSession.writer.println(message);
		        GameProtocol.PrintFullMap(GameProtocol.FullMapUpdate(GameProtocol.GetFullMap(GameProtocol.gameMap.length)), clientSession.writer);
	    	}
	    }
	}
	
	public static void PeriodicallyBroadcastRoundInfo(ScheduledExecutorService scheduler, int timeInterval) {
		scheduler.scheduleAtFixedRate(() -> {
			SubtractRoundTime(timeInterval);
			BroadcastRoundInfo();
			
			if (GetRoundTimeLeft() <= 0) {
				ClientSession topPlayer = GameProtocol.GetTopPlayer().getKey();
				
				if (topPlayer != null) {
					GameProtocol.BroadcastMessageFromServer(GameProtocol.WinnerString(topPlayer));
				}
				GameProtocol.ResetScores();
				IncrementRoundNumber();
				ResetRoundClockTime();
			}
			
		}, 0, timeInterval, TimeUnit.SECONDS);
	}
	
	public static long GetRoundClockTime() {
		return (System.currentTimeMillis() - startTime) / 1000;
	}
	
	public static void IncrementRoundNumber() {
		roundNum += 1;
	}
	
	public static void ResetRoundClockTime() {
		startTime = 0;
		currentRoundTimeLeft = maxRoundTime;
	}
	
	public static int GetRoundTimeLeft() {
		return currentRoundTimeLeft;
	}
	
	public static void SubtractRoundTime(int timeToSubtract) {
		currentRoundTimeLeft -= timeToSubtract;
	}
	
	public static void SetServer(Server s) {
		server = s;
	}

}
