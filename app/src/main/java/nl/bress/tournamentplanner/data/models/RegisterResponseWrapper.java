package nl.bress.tournamentplanner.data.models;

public class RegisterResponseWrapper {

    private RegisterResponse result;

    public RegisterResponseWrapper(RegisterResponse result) {
        this.result = result;
    }

    public RegisterResponse getResult() {
        return result;
    }

    public void setResult(RegisterResponse result) {
        this.result = result;
    }
}
