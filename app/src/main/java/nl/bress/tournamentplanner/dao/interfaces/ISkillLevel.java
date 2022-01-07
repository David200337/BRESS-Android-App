package nl.bress.tournamentplanner.dao.interfaces;

import nl.bress.tournamentplanner.domain.GameResponseWrapper;
import nl.bress.tournamentplanner.domain.SkillLevelResponseWrapper;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ISkillLevel {
    @GET("SkillLevel")
    Call<SkillLevelResponseWrapper> getAllSkillLevels();
}
