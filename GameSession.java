public class GameSession {
    private ClientHandler player1;
    private ClientHandler player2;
    private boolean player1Turn = true;

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;
        System.out.println("New game: " + p1.getPlayerName() + " vs " + p2.getPlayerName());
        
        p1.sendMessage("GAME_START:RED");
        p2.sendMessage("GAME_START:BLUE");
    }
}