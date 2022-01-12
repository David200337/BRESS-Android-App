package nl.bress.tournamentplanner.data.models;

public class LoginResponseWrapper {
    private LoginResponse result;

    public LoginResponseWrapper(LoginResponse result) {
        this.result = result;
    }

    public LoginResponse getResult() {
        return result;
    }

    public void setResult(LoginResponse result) {
        this.result = result;
    }
}
