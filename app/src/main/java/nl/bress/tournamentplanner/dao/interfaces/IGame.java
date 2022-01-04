package nl.bress.tournamentplanner.dao.interfaces;

import nl.bress.tournamentplanner.domain.GameResponseWrapper;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface IGame {

    @GET("player/{playerId}/currentGame")
    Call<GameResponseWrapper> getCurrentGame(@Path("playerId") int id);
}
