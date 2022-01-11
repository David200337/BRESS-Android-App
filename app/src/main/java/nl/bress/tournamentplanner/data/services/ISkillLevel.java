package nl.bress.tournamentplanner.data.services;

import nl.bress.tournamentplanner.data.models.SkillLevelResponseWrapper;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ISkillLevel {
    @GET("SkillLevel")
    Call<SkillLevelResponseWrapper> getAllSkillLevels();
}
