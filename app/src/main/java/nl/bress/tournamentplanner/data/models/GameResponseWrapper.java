package nl.bress.tournamentplanner.data.models;

import nl.bress.tournamentplanner.domain.Game;

public class GameResponseWrapper {
    private Game result;

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
