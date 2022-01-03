package nl.bress.tournamentplanner.domain;

public class Player {
    public int id;
    public String name;
    public String email;
    public String score;
    public String pointBalance;
    public SkillLevel skillLevel;

    public Player(int id, String name, String email, String score, String pointBalance, SkillLevel skillLevel) {
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

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPointBalance() {
        return pointBalance;
    }

    public void setPointBalance(String pointBalance) {
        this.pointBalance = pointBalance;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }
}
