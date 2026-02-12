# King of the Hill #
Your goal is to get to the center hill of the map, and get as many points as possible before the end of the round. 
However, by attempting to move into a space occupied by another player, that player will be "shoved", forcing them to move in a certain direction.
Use this to shove players off of the hill, and try not to get shoved off the hill yourself.

## Technologies Used ##
* Java

## Installation / Compilation ##
Assuming you are on Windows:

1. Clone the repository.
```
git clone https://github.com/Rise-A/KingOfTheHill-Java.git
```
2. Navigate into the project directory.
```
cd C:\project\directory\goes\here\KingOfTheHill-Java
```
3. Once you're in the project directory, compile the java files.
```
javac GameProtocol.java Server.java ClientSession.java RoundManager.java
```

## Usage ##
Assuming you are on Windows, have telnet enabled, and compiled the project's java files:

1. Navigate to the "src" folder in the project directory.
```
cd C:\project\directory\goes\here\KingOfTheHill-Java\src
```
2. Once you are in "src", run the server with the following command:
```
java KingOfTheHill-Java.Server PORT_NUMBER 
```
A message should pop up saying: "Server Port: PORT_NUMBER".

3. Open another Command Prompt tab.
4. To connect to the server, run the following command:
```
telnet localhost PORT_NUMBER
```
5. You should now be connected to the server. Follow the instructions on screen to login.

## Controls / Commands ##
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

## License ##
MIT License
See the LICENSE file for more details.
