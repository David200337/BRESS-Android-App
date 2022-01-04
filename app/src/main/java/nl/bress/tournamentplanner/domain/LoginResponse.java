package nl.bress.tournamentplanner.domain;

public class LoginResponse {
    public String token;
    public String expireDate;
    public User user;

    public LoginResponse(String token, String expireDate, User user, int id) {
        this.token = token;
        this.expireDate = expireDate;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
