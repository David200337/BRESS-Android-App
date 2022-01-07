package nl.bress.tournamentplanner.domain;

public class RegisterResponse {
    private boolean succeeded;
    private String token;
    private boolean playerExists;

    public RegisterResponse(boolean succeeded, String token, boolean playerExists) {
        this.succeeded = succeeded;
        this.token = token;
        this.playerExists = playerExists;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean playerExists() {
        return playerExists;
    }

    public void setPlayerExists(boolean playerExists) {
        this.playerExists = playerExists;
    }
}
