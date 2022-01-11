package nl.bress.tournamentplanner.data.models;

import nl.bress.tournamentplanner.domain.SkillLevel;

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
