package nl.bress.tournamentplanner.domain;

public class LogoutModel {
    public String email;

    public LogoutModel(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
