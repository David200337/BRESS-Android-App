package nl.bress.tournamentplanner.dao.interfaces;

import nl.bress.tournamentplanner.domain.LoginModel;
import nl.bress.tournamentplanner.domain.LoginResponseWrapper;
import nl.bress.tournamentplanner.domain.LogoutModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface IAuth {

    @Headers("Content-Type:application/json")
    @POST("playerlogin")
    Call<LoginResponseWrapper> login(@Body LoginModel login);

    @Headers("Content-Type:application/json")
    @PUT("playerlogout")
    Call<Object> logout(@Body LogoutModel logout);
}
