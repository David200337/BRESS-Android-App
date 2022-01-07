package nl.bress.tournamentplanner.domain;

public class PlayerResponseWrapper {
    private Player result;

    public PlayerResponseWrapper(Player result) {
        this.result = result;
    }

    public Player getResult() {
        return result;
    }

    public void setResult(Player result) {
        this.result = result;
    }
}
