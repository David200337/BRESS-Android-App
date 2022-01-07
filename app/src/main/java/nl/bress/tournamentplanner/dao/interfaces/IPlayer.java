package nl.bress.tournamentplanner.dao.interfaces;

import nl.bress.tournamentplanner.domain.NewPlayerModel;
import nl.bress.tournamentplanner.domain.PlayerResponseWrapper;
import nl.bress.tournamentplanner.domain.RegisterModel;
import nl.bress.tournamentplanner.domain.RegisterResponseWrapper;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IPlayer {

    @Headers("Content-Type:application/json")
    @POST("player")
    Call<PlayerResponseWrapper> createPlayer(@Body NewPlayerModel newPlayerModel);
}
