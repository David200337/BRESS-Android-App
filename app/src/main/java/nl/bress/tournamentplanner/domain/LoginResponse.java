package nl.bress.tournamentplanner.domain;

public class LoginResponse {
    public String token;
    public String expireDate;
    public String user;
    public int id;

    public LoginResponse(String token, String expireDate, String user, int id) {
        this.token = token;
        this.expireDate = expireDate;
        this.user = user;
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
