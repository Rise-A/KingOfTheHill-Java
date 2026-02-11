package scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class GameProtocol {
	public static char[][] kingOfTheHillMap = {
			{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'},
			{'#',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#','.','.','.','.','.','.','.','.','.','.','0','.','.','.','.','.','.','.','.','.','.','#'},
			{'#','.','.','.','.','.','.','.','.','.','0','0','0','.','.','.','.','.','.','.','.','.','#'},
			{'#','.','.','.','.','.','.','.','.','.','.','0','.','.','.','.','.','.','.','.','.','.','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.',',','#'},
			{'#',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',',','#'},
			{'#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#','#'}
	};
	
	public static char[][] testMap = {
			{'#','.','.','.','.'},
			{'.','.','#','.','.'},
			{'.','#','#','#','.'},
			{'.','#','#','#','.'},
			{'.','.','#','.','.'},
			{'.','.','.','.','.'}
	};
	
	public static char[][] gameMap = new char[kingOfTheHillMap.length][];
	
	static {
	    gameMap = new char[kingOfTheHillMap.length][];
	    for (int i = 0; i < kingOfTheHillMap.length; i++) {
	        gameMap[i] = kingOfTheHillMap[i].clone();
	    }
	}
	
	public static char wallTile = '#';
	
	// Stores map tiles, and whether they're walkable or not
	public static char groundTile = '.';
	public static char spawnTile = ',';
	public static char hillTile = '0';
	
	public static HashMap<Character, Boolean> gameTiles = new HashMap<>();
	
	static {
		gameTiles.put(groundTile, true);
		gameTiles.put(spawnTile, true);
		gameTiles.put(hillTile, true);
		
		gameTiles.put(wallTile, false);
	}
	
	// Hill Indices
	public static List<int[]> hillIndices = new ArrayList<>();
	
	static {
	    for (int y = 0; y < gameMap.length; y++) {
	        for (int x = 0; x < gameMap[y].length; x++) {
	            if (gameMap[y][x] == hillTile) {
	                hillIndices.add(new int[]{x, y});
	            }
	        }
	    }
	}
	
//	public static void main(String[] args) {		
//		List<int[]> tiles = GetSpawnableTileIndices();
//		for (int[] i: tiles) {
//			System.out.println(Arrays.toString(i));
//		}
//	}
	
	// Main Protocol Methods
	//----------------------
	/**
	 * Starts the game protocol for the server
	 * @param clientSession
	 * @param reader
	 * @param writer
	 * @throws IOException
	 */
	public static void BeginProtocol(ClientSession clientSession, BufferedReader reader, PrintWriter writer) throws IOException {
		String loginInput = reader.readLine();
		String loginCommand[] = GameProtocol.InputCommand(loginInput);
		if (loginCommand[0].equals("LOGIN")) { // User first needs to login before doing anything
			if (UserAlreadyExists(loginCommand[1])) { // user with same username is already connected
				clientSession.writer.println("400 Failure. User already exists");
				BeginProtocol(clientSession, reader, writer);
			}
			
			else {
				writer.println(LoginMessageString(200, loginCommand[1]));
				clientSession.userName = loginCommand[1];
				clientSession.isLoggedIn = true;
				
				SpawnPlayer(clientSession, '&');
				PrintFullMap(FullMapUpdate(GetFullMap(gameMap.length)), writer);
				
		        String rawInputString;
		        while ((rawInputString = reader.readLine()) != null) {
		        	String[] inputCommand = InputCommand(rawInputString);
		        	GetCommand(rawInputString, inputCommand, clientSession);
		        	
		        	if (inputCommand[0].startsWith("QUIT")) {
		        		clientSession.isLoggedIn = false;
		        		break;
		        	}
		        }
			}
		}
		else {
			writer.println("400 Failure (Usage: LOGIN <username> )");
			BeginProtocol(clientSession, reader, writer);
		}
	}
	
	/**
	 * Gets one of the following valid user requests: LOGIN, QUIT, MOVE, MAP, MAPU, INFO, LEADERBOARD, MESSAGE
	 * @param rawInput
	 * @param input
	 * @param client
	 */
	public static void GetCommand(String rawInput, String[] input, ClientSession client) {
		switch (input[0]) {
			case "QUIT":
				Quit(client);
				break;
//			case "MOVE":
//				MovePlayerRelativeCommand(input, rawInput, client, 1);
//				break;
			// For WASD movement
				case "w":
					String[] wInput = new String[3];
					wInput[1] = "0";
					wInput[2] = "-1";
					MovePlayerRelativeCommand(wInput, rawInput, client, 1);
					break;
				case "a":
					String[] aInput = new String[3];
					aInput[1] = "-1";
					aInput[2] = "0";
					MovePlayerRelativeCommand(aInput, rawInput, client, 1);
					break;
				case "s":
					String[] sInput = new String[3];
					sInput[1] = "0";
					sInput[2] = "1";
					MovePlayerRelativeCommand(sInput, rawInput, client, 1);
					break;
				case "d":
					String[] dInput = new String[3];
					dInput[1] = "1";
					dInput[2] = "0";
					MovePlayerRelativeCommand(dInput, rawInput, client, 1);
					break;
			case "MAPU":
				PartialMapUpdateCommand(input, rawInput, client);
				break;
			case "MAP":
				PrintFullMap(FullMapUpdate(GetFullMap(gameMap.length)), client.writer);
				break;
			case "INFO":
				String infoString = GameInfo(200, Server.serverName, gameMap.length, gameMap[0].length, Server.GetServerClockTime(), Server.clientList.size());
				client.writer.println(infoString);
				break;
			case "MESSAGE":
				String message = MessageString(200, input[1], client);
				GameProtocol.BroadcastMessage(message, client, true, false);
				break;
			case "LEADERBOARD":
				List<String> leaderBoard = Leaderboard(200);
				for (String s: leaderBoard) {
					client.writer.println(s);
				}
				break;
			default:
				client.writer.println(BadRequest(400, rawInput));
				break;
		}
	}
	
	// General Input Methods
	//----------------------
	
	/**
	 * Separates a user's input command into individual strings
	 * @param input
	 * @return string array containing individual arguments of input
	 */
	public static String[] InputCommand(String input) {
		String[] inputCommand = null; 
		
		if (input != null) {
			input = input.replaceAll("\\p{Cntrl}", "");
			
			if (input.startsWith("MESSAGE")) {
				inputCommand = input.split(" ", 2);
				//inputCommand = null; // placeholder for msg. Msg can contain whitespace and commas
			}
			
			else {
				inputCommand = input.split("[ ,]+"); // Everything else that can't contain whitespace and commas
			}
		}
		
		return inputCommand;
	}
	
	// Login Methods
	// ------------------
	/**
	 * Prompts the user for a login
	 * @param client
	 */
	public static void PromptLogin(ClientSession client) {
		client.writer.println("300 Please login (Usage: LOGIN <username> )");
	}
	
	/**
	 * Returns a string confirming a successful login
	 * @param responseCode
	 * @param userName
	 * @return
	 */
	public static String LoginMessageString(int responseCode, String userName) {
		return responseCode + " " + "Ok, Hello there" + " " + userName;
	}
	
	/**
	 * Checks whether or not a user with the same username is already connected
	 * @param userName
	 * @return
	 */
	public static boolean UserAlreadyExists(String userName) {
		for (Entry<ClientSession, Integer> c: Server.clientList.entrySet()) {
		if (c != null) {
			if (c.getKey().userName.equals(userName)) {
				return true;
				}
			}
		}
		return false;
	}
	
	// Message Methods
	// ----------------
	/**
	 * Broadcasts a message to the whole server from a client. Can be configured to exclude the sender or not via the boolean excludeSender
	 * @param message
	 * @param sender 
	 * @param server
	 * @param excludeSender
	 */
	public static void BroadcastMessage(String message, ClientSession sender, boolean excludeSender, boolean isMapUpdate) {
		for (Entry<ClientSession, Integer> c: Server.clientList.entrySet()) {
			ClientSession currentClient = c.getKey();
			if (excludeSender && currentClient == sender) {
				continue;
			}
			
			currentClient.writer.println(message);
			
			if (isMapUpdate) {
				PrintFullMap(FullMapUpdate(GetFullMap(gameMap.length)), currentClient.writer);
			}
		}
	}
	
	
	/**
	 * Broadcasts a message to the entire server from the server
	 * @param message
	 */
	public static void BroadcastMessageFromServer(String message) {
		for (Entry<ClientSession, Integer> c: Server.clientList.entrySet()) {
			c.getKey().writer.println(message);
		}
	}
	
	/**
	 * Formatted string for a MESSAGE request
	 * @param responseCode
	 * @param message
	 * @param sender
	 * @return
	 */
	public static String MessageString(int responseCode, String message, ClientSession sender) {
		String finalMessage = responseCode + " MESSAGE " + sender.userName + ", " + message;
		
		return finalMessage;
	}
	
	// Player Update Methods
	// ------------------

	/**
	 * Formatted string for a player update
	 * @param responseCode
	 * @param userName
	 * @param xPos
	 * @param yPos
	 * @param state
	 * @param score
	 * @return
	 */
	public static String PlayerUpdateString(int responseCode, String userName, String state, int score, ClientSession client) {
		return responseCode + " " + "PLAYER" + " " + userName + ", " + client.currentXCoord + ", " + client.currentYCoord + "," + state + "," + score;
	}
	
	// Movement Methods
	// ------------------
	public static void MovePlayer(int xPos, int yPos, ClientSession client) {
		int prevXPos = client.currentXCoord;
		int prevYPos = client.currentYCoord;
		
		client.currentXCoord = xPos;
		client.currentYCoord = yPos;
		
		gameMap[yPos][xPos] = '&';
		gameMap[prevYPos][prevXPos] = kingOfTheHillMap[prevYPos][prevXPos];
	}
	
	public static void MovePlayerCommand(String[] input, String rawInput, ClientSession client) {
		while (true) {
			int moveXInput = 0;
			int moveYInput = 0;
			try {
				moveXInput = Integer.parseInt(input[1]);
				moveYInput = Integer.parseInt(input[2]);
			}
			catch (NumberFormatException e){
				client.writer.println(BadRequest(400, rawInput) + ", Usage: MOVE <x>,<y>");
				break;
			}
			
			if (moveXInput < gameMap[0].length && moveYInput < gameMap.length) {
				if (gameMap[moveXInput][moveYInput] == groundTile || gameMap[moveXInput][moveYInput] == hillTile) { // check that the tile is a traversable one
					try {
						// Right now, it essentially allows the player to teleport anywhere. Might want to patch this later, but for Phase III, this is fine.
						MovePlayer(moveXInput, moveYInput, client);
						String moveInput = PlayerUpdateString(200, client.userName, "active", 0, client);
						GameProtocol.BroadcastMessage(moveInput, client, false, true);
						break;
					}
					catch (NumberFormatException e) {
						client.writer.println(BadRequest(400, rawInput) + ", Usage: MOVE <x>,<y>");
						break;
					}	
				}
				else { // tile isn't traversable
					client.writer.println("400 " + moveXInput + "," + moveYInput + " isn't a traversable tile");
					break;
				}
			}
		}
	}
	
	public static void MovePlayerRelativeCommand(String[] input, String rawInput, ClientSession client, int movementSpeed) {
	    int moveXInput;
	    int moveYInput;

	    try {
	        moveXInput = Integer.parseInt(input[1]);
	        moveYInput = Integer.parseInt(input[2]);
	    } 
	    catch (Exception e) {
	        client.writer.println(BadRequest(400, rawInput) + ", Usage: MOVE <dx>,<dy>");
	        return;
	    }

	    if (!MovementIsInRange(moveXInput, moveYInput, movementSpeed)) {
	        client.writer.println(BadRequest(400, rawInput) + ", Movement out of range");
	        return;
	    }

	    int newXPos = client.currentXCoord + moveXInput;
	    int newYPos = client.currentYCoord + moveYInput;

	    if (!InBounds(newXPos, newYPos)) {
	        client.writer.println("400 Movement out of bounds");
	        return;
	    }

	    char destination = gameMap[newYPos][newXPos];

	    if (!gameTiles.getOrDefault(destination, false)) {
	    	if (gameMap[newYPos][newXPos] == '&') { // for shoving other players
	    		ShovePlayer(input, rawInput, newXPos, newYPos, moveXInput, moveYInput, 3);
	    		return;
	    	}
	    	else {
		        client.writer.println("400 Tile not walkable");
		        return;
	    	}
	    }

	    MovePlayer(newXPos, newYPos, client);
	    BroadcastMessage(PlayerUpdateString(200, client.userName, "active", 0, client), client, false, true);
	}

	
	public static boolean MovementIsInRange(int moveXInput, int moveYInput, int movementSpeed) {
		return (moveXInput >= -movementSpeed && moveXInput <= movementSpeed && moveYInput >= -movementSpeed && moveYInput <= movementSpeed);
	}
	
	// Spawning Methods
	// ------------------
	
	/**
	 * Should spawn a player at one of the random spawnable tile indices
	 */
	public static void SpawnPlayer(ClientSession client, char gameCharacter) {
		List<int[]> spawnableTileIndices = GetSpawnableTileIndices();
		
		Random rand = new Random();
		
	    while (true) {
	        int randomIndex = rand.nextInt(spawnableTileIndices.size());
	        int[] coords = spawnableTileIndices.get(randomIndex);

	        int x = coords[0];
	        int y = coords[1];

	        if (gameMap[y][x] == ',') {
	            client.currentXCoord = x;
	            client.currentYCoord = y;
	            gameMap[y][x] = gameCharacter;
	            return;
	        }
	    }
	      
	}
	
	/**
	 * Should return the available indices for spawning
	 * Spawn Area should be the tiles touching the walls
	 */
	public static List<int[]> GetSpawnableTileIndices() {
		List<int[]> spawnableTileIndices = new ArrayList<>();
		
	    for (int y = 0; y < gameMap.length; y++) {
	        for (int x = 0; x < gameMap[y].length; x++) {
	            if (gameMap[y][x] == spawnTile) {
	                spawnableTileIndices.add(new int[]{x, y});
	            }
	        }
	    }
		return spawnableTileIndices;
	}
	
	public static boolean IsValidSpawnPoint(char tile) {
		if (tile == groundTile) {
			return true;
		}
		return false;
	}
	
	// Player Actions
	// ---------------
	/**
	 * @param input
	 * @param rawInput
	 * @param shovedPlayerXCoord
	 * @param shovedPlayerYCoord
	 * @param shoveDirectionX
	 * @param shoveDirectionY
	 * @param shoveForce
	 */
	public static void ShovePlayer(String[] input, String rawInput, int shovedPlayerXCoord, int shovedPlayerYCoord, int shoveDirectionX, int shoveDirectionY, int shoveForce) {
		ClientSession shovedPlayer = GetClientFromSpace(shovedPlayerXCoord, shovedPlayerYCoord);
		MovePlayerRelativeCommand(input, rawInput, shovedPlayer, shoveForce);
	}
	
	// Client Methods
	// ----------------
	public static boolean SpaceOccupiedByPlayer(int xPos, int yPos) {
		if (gameMap[yPos][xPos] == '&') {
			return true;
		}
		return false;	
	}
	
	public static ClientSession GetClientFromSpace(int xPos, int yPos) {
		if (SpaceOccupiedByPlayer(xPos, yPos)) {
			for (Entry<ClientSession, Integer> c: Server.clientList.entrySet()) {
				ClientSession client = c.getKey();
				
				if (client.currentXCoord == xPos && client.currentYCoord == yPos) {
					return client;
				}
					
			}
		}
		return null;
	}
	
	// Map Update Methods
	// ------------------
	
	public static void PrintFullMap(String[] formattedMap, PrintWriter writer) {
		for (String s: formattedMap) {
			writer.println(s);
		}
	}
	
	/**
	 * Returns the whole updated game map
	 * @param fullMap
	 * @return
	 */
	public static String[] FullMapUpdate(String[] fullMap) {
		String[] fullMapUpdate = new String[fullMap.length];
		for (int i = 0; i < fullMap.length; i++) {
			int responseCode = 200;
			String rowNum = GetRowNumFormatted(i, fullMap.length);
			String mapTileData = fullMap[i];
			
			fullMapUpdate[i] = responseCode + " " + "MAP" + " " + rowNum + ", " + mapTileData; 
		}
		
		return fullMapUpdate;
	}
	
	/**
	 * Returns an updated map tile at a given position
	 * @param responseCode
	 * @param xPos
	 * @param yPos
	 * @return
	 */
	public static String PartialMapUpdateString(int responseCode, int xPos, int yPos) {
		String partialMapUpdate = "";
		partialMapUpdate = responseCode + " " + "MAPU" + " " + xPos + ", " + yPos + ", " + GetTileData(xPos,yPos); 
		
		return partialMapUpdate;
	}
	
	public static void PartialMapUpdateCommand(String[] input, String rawInput, ClientSession client ) {
		while (true) {
			try {
				int mapuXCoord;
				int mapYCoord;
				try {
					mapuXCoord = Integer.parseInt(input[1]);
					mapYCoord = Integer.parseInt(input[2]);
				}
				catch (NumberFormatException e){
					client.writer.println(BadRequest(400, rawInput) + ", Usage: MAPU <x>,<y>");
					break;
				}
				
				if (InBounds(mapuXCoord, mapYCoord)) {
					try {
						String partialMapUpdateString = PartialMapUpdateString(200, mapuXCoord, mapYCoord);
						client.writer.println(partialMapUpdateString);
						break;
					}
					catch (NumberFormatException e) {
						client.writer.println(BadRequest(400, rawInput) + ", Usage: MAPU <x>,<y>");
						break;
					}
				}
				else {
					client.writer.println(BadRequest(400, rawInput) + ", Map Index Out of Range");
					break;
				}
			}
			catch (ArrayIndexOutOfBoundsException e) { // incase the user types less than 3 parameters
				client.writer.println(BadRequest(400, rawInput) + ", Usage: MAPU <x>,<y>");
				break;
			}
		}
	}
	
	public static boolean InBounds(int x, int y) {
	    return y >= 0 && y < gameMap.length &&
	           x >= 0 && x < gameMap[0].length;
	}

	/**
	 * Gets the map tile at a given position
	 * @param xPos
	 * @param yPos
	 * @return
	 */
	public static char GetTileData(int xPos, int yPos) {
		try {
			return gameMap[yPos][xPos];
		}
		catch (ArrayIndexOutOfBoundsException e){
			return '?';
		}
//		return gameMap[xPos][yPos];
	}
	
	public static void UpdateTile(int xPos, int yPos, char tile) {
		gameMap[yPos][xPos] = tile;
	}
	
	/**
	 * Returns the game map's height
	 * @param gameMap
	 * @return game map height
	 */
	public static int GetMapHeight(char[][] gameMap) {
		return gameMap.length;
	}
	
	/** 
	 * Gets the full map data in string[] format, where each index is a row
	 * @param mapHeight
	 * @return Full map in String[] format
	 */
	public static String[] GetFullMap(int mapHeight) {
		String[] fullMap = new String[mapHeight];
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < mapHeight; i++) {
			for (int j = 0; j < gameMap[i].length; j++) {
				sb.append(gameMap[i][j]);
			}
			String rowToAdd = sb.toString();
			fullMap[i] = rowToAdd;
			sb.setLength(0); // clears string builder for next iteration
		}
		
		return fullMap;
		
	}
	
	/**
	 * Helper method for FullMapUpdate() to get the row number in correct format
	 * @return Formatted row number
	 */
	public static String GetRowNumFormatted(int rowNum, int mapHeight) {
		String formattedRowNum = "";
		
		int formatLength = 3; // default row number format length of 3 (ie, 000)
		if (mapHeight > 999) {
			int newFormatLength = String.valueOf(mapHeight).length();
			formatLength = newFormatLength;
		}
		
		String row = String.valueOf(rowNum);
		int lastCharsToReplace = row.length();
		
		for (int i = 0; i < formatLength - lastCharsToReplace; i++) {
			formattedRowNum += 0;
		}
		
		formattedRowNum += row;
		
		return formattedRowNum;
	}
	
	// Hill Methods
	///////////////
	
	/**
	 * Allows players to score points if they are on the hill
	 */
	public static void StartHillScoringLoop() {
	    Thread hillThread = new Thread(() -> {
	        while (true) {
	            try {
	                Thread.sleep(1000); 
	            } 
	            catch (InterruptedException e) {
	                return;
	            }

	            for (Entry<ClientSession, Integer> entry : Server.clientList.entrySet()) {
	                ClientSession client = entry.getKey();
	                if (client == null) {
	                	continue;
	                }
	                
	                int x = client.currentXCoord;
	                int y = client.currentYCoord;
	                boolean onHill = false;

	                if (!InBounds(client.currentYCoord, client.currentXCoord)) {
	                	continue;
	                }

	                for (int[] pos : hillIndices) {
	                    if (pos[0] == x && pos[1] == y) {
	                        onHill = true;
	                        break;
	                    }
	                }

	                if (!onHill) {
	                    continue;
	                }
	                
	                entry.setValue(entry.getValue() + 1);
	            }
	        }
	    });

	    hillThread.setDaemon(true);
	    hillThread.start();
	}

	/**
	 * @param responseCode
	 * @param gameName
	 * @param mapXSize
	 * @param mapYSize
	 * @param clockTime
	 * @param playerCount
	 * @return
	 */
	public static String GameInfo(int responseCode, String gameName, int mapXSize, int mapYSize, long clockTime, int playerCount) {
		return responseCode + " " + "INFO" + " " + gameName + ", " + mapXSize + ", " + mapYSize + ", " + clockTime + ", " + playerCount; 
	}
	
	public static String RoundInfo(int responseCode, int roundNum, int timeLeft) {
		return responseCode + " ROUND " + roundNum + ", TIME LEFT " + timeLeft;
	}
	
	// Leaderboard / Scoring
	////////////////////////////
	/**
	 * Returns a leaderboard of all players on the server in descending order from highest to lowest points
	 * @param responseCode
	 * @return
	 */
	public static List<String> Leaderboard(int responseCode) {
	    List<String> leaderBoard = new ArrayList<>();
	    List<Entry<ClientSession, Integer>> entries = new ArrayList<>(Server.clientList.entrySet());
	    entries.sort((a, b) -> b.getValue().compareTo(a.getValue())); // sort descending order
	    
	    for (Entry<ClientSession, Integer> e : entries) {
	        ClientSession client = e.getKey();
	        if (client == null) {
	        	continue;
	        }

	        leaderBoard.add(responseCode + " " + client.userName + ", " + e.getValue());
	    }

	    return leaderBoard;
	}
	
	/**
	 * @return Top player of the server
	 */
	public static Entry<ClientSession, Integer> GetTopPlayer() {
	    List<Entry<ClientSession, Integer>> entries = new ArrayList<>(Server.clientList.entrySet());
	    if (entries.isEmpty()) {
	    	return null;
	    }

	    entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));
	    return entries.get(0);
	}
	
	/**
	 * @param client
	 * @return String displaying top player, and score
	 */
	public static String WinnerString(ClientSession client) {
		Integer score = Server.clientList.get(client);
		return "200 " + client.userName + " has won with " + score + " points!";
	}
	
	/**
	 * Resets the score of all clients
	 */
	public static void ResetScores() {
		for (Entry<ClientSession, Integer> c: Server.clientList.entrySet()) {
			c.setValue(0);
		}
	}

	/**
	 * Broadcasts to the server that a client has quit the game
	 * @param client
	 */
	public static void Quit(ClientSession client) {
		String quitMessage = 200 + " PLAYER " + client.userName + ", inactive";
		client.isLoggedIn = false;
		BroadcastMessage(quitMessage, client, false, false);
	}
	
	// Error Protocols
	//------------------
	public static String BadRequest(int responseCode, String request) {
		return responseCode + " " + "Bad request" + " " + "\"" + request + "\"";
	}
	
	public static String ServerDown(int responseCode) {
		return responseCode + " " + "Server is down, try again later";
	}
}
