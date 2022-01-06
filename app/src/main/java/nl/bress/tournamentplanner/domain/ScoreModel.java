package nl.bress.tournamentplanner.domain;

public class ScoreModel {
    private boolean[] sets;

    public ScoreModel(boolean[] sets) {
        this.sets = sets;
    }

    public boolean[] getSets() {
        return sets;
    }

    public void setSets(boolean[] sets) {
        this.sets = sets;
    }
}
