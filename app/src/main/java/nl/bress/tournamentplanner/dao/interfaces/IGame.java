package nl.bress.tournamentplanner.dao.interfaces;

import nl.bress.tournamentplanner.domain.GameResponseWrapper;
import nl.bress.tournamentplanner.domain.LogoutModel;
import nl.bress.tournamentplanner.domain.ScoreModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface IGame {

    @GET("player/{playerId}/currentGame")
    Call<GameResponseWrapper> getCurrentGame(@Path("playerId") int id);

    @Headers("Content-Type:application/json")
    @PUT("player/{playerId}/currentGame/{gameId}")
    Call<Object> addScoreToCurrentGame(@Path("playerId") int id, @Path("gameId") int gameId, @Body ScoreModel score);

}
