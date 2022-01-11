package nl.bress.tournamentplanner.data.models;

public class UpdatePlayerModel {
    private String name;
    private int skillLevelId;

    public UpdatePlayerModel(String name, int skillLevelId) {
        this.name = name;
        this.skillLevelId = skillLevelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSkillLevelId() {
        return skillLevelId;
    }

    public void setSkillLevelId(int skillLevelId) {
        this.skillLevelId = skillLevelId;
    }
}
