package nl.bress.tournamentplanner.data.services;

import nl.bress.tournamentplanner.data.models.GameResponseWrapper;
import nl.bress.tournamentplanner.data.models.ScoreModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface IGame {

    @GET("player/{playerId}/currentGame")
    Call<GameResponseWrapper> getCurrentGame(@Path("playerId") int id);

    @GET("player/{playerId}/nextGame")
    Call<GameResponseWrapper> getNextGame(@Path("playerId") int id);

    @Headers("Content-Type:application/json")
    @PUT("player/{playerId}/currentGame/{gameId}")
    Call<Object> addScoreToCurrentGame(@Path("playerId") int id, @Path("gameId") int gameId, @Body ScoreModel score);

}
