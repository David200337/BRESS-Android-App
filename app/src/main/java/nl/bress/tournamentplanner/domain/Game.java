package nl.bress.tournamentplanner.domain;

public class Game {
    public int id;
    public String score;
    public int winner;
    public boolean inQueue;
    public boolean gameStarted;
    public Field field;
    public Player player1;
    public Player player2;

    public Game(int id, String score, int winner, boolean inQueue, boolean gameStarted, Field field, Player player1, Player player2) {
        this.id = id;
        this.score = score;
        this.winner = winner;
        this.inQueue = inQueue;
        this.gameStarted = gameStarted;
        this.field = field;
        this.player1 = player1;
        this.player2 = player2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
}
