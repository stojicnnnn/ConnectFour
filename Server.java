import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PORT = 12345;
    
    private Map<String, ClientHandler> registeredClients = new ConcurrentHashMap<>();
    
    private List<ClientHandler> availableClients = new CopyOnWriteArrayList<>();

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Connect 4 server started");
        System.out.println("Running on port " + PORT);
        System.out.println("Waiting for connections...");
        System.out.println("...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("User connected: " + clientSocket.getInetAddress());
            
            ClientHandler clientHandler = new ClientHandler(clientSocket, this);
            new Thread(clientHandler).start();
        }
    }

    public synchronized void registerClient(String name, ClientHandler handler) {
        registeredClients.put(name, handler);
        availableClients.add(handler);
        System.out.println("Player: " + name);
        System.out.println("Total players: " + registeredClients.size());
        broadcastAvailablePlayers();
    }

    public synchronized void removeClient(String name) {
        if (name != null) {
            ClientHandler handler = registeredClients.remove(name);
            if (handler != null) {
                availableClients.remove(handler);
                System.out.println("Player disconnected: " + name);
            }
        }
        broadcastAvailablePlayers();
    }

    public synchronized void makeAvailable(ClientHandler handler) {
        if (handler != null && handler.getPlayerName() != null) {
            availableClients.add(handler);
            broadcastAvailablePlayers();
        }
    }

    public synchronized void makeUnavailable(ClientHandler handler) {
        if (handler != null) {
            availableClients.remove(handler);
            broadcastAvailablePlayers();
        }
    }

    public synchronized void broadcastAvailablePlayers() {
        StringBuilder playerList = new StringBuilder("PLAYER_LIST:");
        boolean first = true;
        for (ClientHandler handler : availableClients) {
            if (handler.getPlayerName() != null) {
                if (!first) playerList.append(",");
                playerList.append(handler.getPlayerName());
                first = false;
            }
        }
        String message = playerList.toString();
        System.out.println("Sending player list: " + message);
        
        for (ClientHandler handler : registeredClients.values()) {
            handler.sendMessage(message);
        }
    }

    public ClientHandler getClientHandler(String name) {
        return registeredClients.get(name);
    }

    public boolean isPlayerAvailable(String name) {
        ClientHandler handler = registeredClients.get(name);
        return handler != null && availableClients.contains(handler);
    }

    public static void main(String[] args) {
        try {
            Server server = new Server();
            server.start();
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}