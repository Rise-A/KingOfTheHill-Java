# HOW TO PLAY #
Your goal is to get to the center hill of the map, and get as many points as possible before the end of the round. 
However, by attempting to move into a space occupied by another player, that player will be "shoved", forcing them to move in a certain direction.
Use this to shove players off of the hill, and try not to get shoved off the hill yourself.

# COMPILE INSTRUCTIONS #
Assuming you are on Windows, and downloaded, and unzipped the zip folder from the Github:

1. Open Command Prompt (type "cmd" into the search bar)
2. cd into the folder, "project-3-multiplayer-game-server-ra" (ie, where the java files are kept)
3. Once you are in the folder, type "javac GameProtocol.java Server.java ClientSession.java RoundManager.java". The files should now compile.

# RUN INSTRUCTIONS #
Assuming you are on Windows, have telnet enabled, and compiled the java files:

1. Open Command Prompt (type "cmd" into the search bar)
2. cd into the folder, "project-3-multiplayer-game-server-ra", then cd into the folder "src" (ie, the folder that contains the folder, "scripts")
3. Once you are in "src", type "java project_3_multiplayer_game_server_ra.Server PORT_NUMBER". A message should pop up saying: "Server Port: PORT_NUMBER"
4. Open another Command Prompt tab.
5. Type "telnet localhost PORT_NUMBER".
6. You should now be connected to the server. Follow the instructions on screen to login.

# CONTROLS / COMMANDS #
All commands should be followed by the "ENTER" key.

```LOGIN <username>```
Logs you into the game. You cannot have the same username as someone already connected to the server.

```QUIT```
Quits the game.

```W```
Moves you up.

```A```
Moves you left.

```S```
Moves you down.

```D```
Moves you right.

```MAPU <x>,<y>```
Displays the tile at the given coordinates.

```MAP```
Displays the whole game map.

```INFO```
Displays info about the game, like the server name, map size, server time, and number of players connected.

```MESSAGE <msg>```
Lets you output a message that can be seen by other players.

```LEADERBOARD```
Displays a leaderboard of all players with the highest scores, in descending order.
