package nl.bress.tournamentplanner.dao.interfaces;

import nl.bress.tournamentplanner.domain.LoginModel;
import nl.bress.tournamentplanner.domain.LoginResponseWrapper;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IAuth {

    @Headers("Content-Type:application/json")
    @POST("login")
    Call<LoginResponseWrapper> login(@Body LoginModel login);
}
