package nl.bress.tournamentplanner.data.models;

public class LoginModel {
    private String email;
    private String password;
    private String fbtoken;

    public LoginModel(String email, String password, String fbtoken) {
        this.email = email;
        this.password = password;
        this.fbtoken = fbtoken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFbtoken() {
        return fbtoken;
    }

    public void setFbtoken(String fbtoken) {
        this.fbtoken = fbtoken;
    }
}
