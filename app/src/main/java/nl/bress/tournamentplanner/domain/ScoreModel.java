package nl.bress.tournamentplanner.domain;

import java.util.List;

public class ScoreModel {
    private List<Integer> scorePlayer1;
    private List<Integer>  scorePlayer2;

    public ScoreModel(List<Integer> scorePlayer1, List<Integer> scorePlayer2) {
        this.scorePlayer1 = scorePlayer1;
        this.scorePlayer2 = scorePlayer2;
    }

    public List<Integer> getScorePlayer1() {
        return scorePlayer1;
    }

    public void setScorePlayer1(List<Integer> scorePlayer1) {
        this.scorePlayer1 = scorePlayer1;
    }

    public List<Integer> getScorePlayer2() {
        return scorePlayer2;
    }

    public void setScorePlayer2(List<Integer> scorePlayer2) {
        this.scorePlayer2 = scorePlayer2;
    }
}
