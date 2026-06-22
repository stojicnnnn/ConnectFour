import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            System.out.println("Server -> " + playerName + ": " + message);
        }
    }

    private void handleMessage(String message) {
        System.out.println(playerName + " -> Server: " + message);
        
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        if (message.startsWith("REGISTER:")) {
            String name = message.substring(9).trim();
            if (name.isEmpty()) {
                sendMessage("ERROR: Name cannot be empty");
                return;
            }
            
            if (server.getClientHandler(name) != null) {
                sendMessage("ERROR: Name '" + name + "' is already taken");
                return;
            }
            
            this.playerName = name;
            server.registerClient(name, this);
            sendMessage("REGISTERED:" + name);
            System.out.println("Successfull registration: " + name);
            return;
        }

        if (message.startsWith("INVITE:")) {
            String opponentName = message.substring(7).trim();
            ClientHandler opponent = server.getClientHandler(opponentName);
            
            if (opponent == null) {
                sendMessage("ERROR: Player '" + opponentName + "' does not exist");
                return;
            }
            
            if (!server.isPlayerAvailable(opponentName)) {
                sendMessage("ERROR: Player '" + opponentName + "' is not available");
                return;
            }
            
            opponent.sendMessage("INVITED_BY:" + playerName);
            sendMessage("INVITE_SENT:" + opponentName);
            System.out.println("Player " + playerName + " invites " + opponentName);
            return;
        }

        if (message.equals("ACCEPT")) {
            System.out.println("Player " + playerName + " has accepted the invite");
            return;
        }

        if (message.equals("DECLINE")) {
            System.out.println("Player " + playerName + " has declined the invite");
            return;
        }

        // Nepoznata poruka
        System.out.println(" Unknown message from " + playerName + ": " + message);
        sendMessage("ERROR: Unknown command");
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Player " + playerName + " has disconnected");
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (playerName != null) {
                server.removeClient(playerName);
            }
        }
    }
}