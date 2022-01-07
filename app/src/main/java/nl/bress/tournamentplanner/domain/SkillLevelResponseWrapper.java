package nl.bress.tournamentplanner.domain;

public class SkillLevelResponseWrapper {
    private SkillLevel[] result;

    public SkillLevelResponseWrapper(SkillLevel[] result) {
        this.result = result;
    }

    public SkillLevel[] getResult() {
        return result;
    }

    public void setResult(SkillLevel[] result) {
        this.result = result;
    }
}
