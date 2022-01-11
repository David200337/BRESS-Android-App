package nl.bress.tournamentplanner.domain;

public class Player {
    private int id;
    private String name;
    private String email;
    private int score;
    private int pointBalance;
    private SkillLevel skillLevel;

    public Player(int id, String name, String email, int score, int pointBalance, SkillLevel skillLevel) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.score = score;
        this.pointBalance = pointBalance;
        this.skillLevel = skillLevel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getPointBalance() {
        return pointBalance;
    }

    public void setPointBalance(int pointBalance) {
        this.pointBalance = pointBalance;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }
}
