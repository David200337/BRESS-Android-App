package nl.bress.tournamentplanner.domain;

public class LoginResponseWrapper {
    public LoginResponse result;

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
