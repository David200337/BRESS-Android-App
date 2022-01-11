package nl.bress.tournamentplanner.data.services;

import nl.bress.tournamentplanner.data.models.LoginModel;
import nl.bress.tournamentplanner.data.models.LoginResponseWrapper;
import nl.bress.tournamentplanner.data.models.LogoutModel;
import nl.bress.tournamentplanner.data.models.RegisterModel;
import nl.bress.tournamentplanner.data.models.RegisterResponseWrapper;
import retrofit2.Call;
import retrofit2.http.Body;
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

    @Headers("Content-Type:application/json")
    @POST("playerregister")
    Call<RegisterResponseWrapper> register(@Body RegisterModel registerModel);
}
