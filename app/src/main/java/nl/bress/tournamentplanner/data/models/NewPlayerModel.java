package nl.bress.tournamentplanner.data.models;

public class NewPlayerModel {
    private String firstName;
    private String lastName;
    private String email;
    private int skillLevelId;

    public NewPlayerModel(String firstName, String lastName, String email, int skillLevelId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.skillLevelId = skillLevelId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getSkillLevelId() {
        return skillLevelId;
    }

    public void setSkillLevelId(int skillLevelId) {
        this.skillLevelId = skillLevelId;
    }
}
