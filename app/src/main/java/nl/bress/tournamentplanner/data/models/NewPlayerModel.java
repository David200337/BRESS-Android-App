package nl.bress.tournamentplanner.data.models;

public class NewPlayerModel {
    private String name;
    private String email;
    private int skillLevelId;

    public NewPlayerModel(String name, String email, int skillLevelId) {
        this.name = name;
        this.email = email;
        this.skillLevelId = skillLevelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
