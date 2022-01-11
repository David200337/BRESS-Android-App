package nl.bress.tournamentplanner.dao.interfaces;

import nl.bress.tournamentplanner.domain.NewPlayerModel;
import nl.bress.tournamentplanner.domain.Player;
import nl.bress.tournamentplanner.domain.PlayerResponseWrapper;
import nl.bress.tournamentplanner.domain.RegisterModel;
import nl.bress.tournamentplanner.domain.RegisterResponseWrapper;
import nl.bress.tournamentplanner.domain.UpdatePlayerModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface IPlayer {

    @Headers("Content-Type:application/json")
    @POST("player")
    Call<PlayerResponseWrapper> createPlayer(@Body NewPlayerModel newPlayerModel);

    @GET("Player/{playerId}/get")
    Call<PlayerResponseWrapper> getPlayerById(@Path("playerId") int playerId);

    @Headers("Content-Type:application/json")
    @PUT("Player/{playerId}/update")
    Call<PlayerResponseWrapper> updatePlayer(@Path("playerId") int playerId, @Body UpdatePlayerModel updatePlayerModel);
}
