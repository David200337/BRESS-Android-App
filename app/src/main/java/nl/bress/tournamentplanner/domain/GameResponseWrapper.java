package nl.bress.tournamentplanner.domain;

public class GameResponseWrapper {
    public Game result;

    public GameResponseWrapper(Game result) {
        this.result = result;
    }

    public Game getResult() {
        return result;
    }

    public void setResult(Game result) {
        this.result = result;
    }
}
