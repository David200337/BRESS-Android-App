package nl.bress.tournamentplanner.data.models;

public class UpdatePlayerModel {
    private String firstName;
    private String lastName;
    private int skillLevelId;

    public UpdatePlayerModel(String firstName, String lastName, int skillLevelId) {
        this.firstName = firstName;
        this.lastName = lastName;
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

    public int getSkillLevelId() {
        return skillLevelId;
    }

    public void setSkillLevelId(int skillLevelId) {
        this.skillLevelId = skillLevelId;
    }
}
