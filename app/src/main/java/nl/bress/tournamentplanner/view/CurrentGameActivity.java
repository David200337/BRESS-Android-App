package nl.bress.tournamentplanner.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.dao.interfaces.IAuth;
import nl.bress.tournamentplanner.dao.interfaces.IGame;
import nl.bress.tournamentplanner.domain.Game;
import nl.bress.tournamentplanner.domain.GameResponseWrapper;
import nl.bress.tournamentplanner.domain.LoginModel;
import nl.bress.tournamentplanner.domain.LoginResponse;
import nl.bress.tournamentplanner.domain.LoginResponseWrapper;
import nl.bress.tournamentplanner.domain.LogoutModel;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrentGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_game);

        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        ConstraintLayout body = findViewById(R.id.current_game_cl_body);
        ConstraintLayout empty = findViewById(R.id.current_game_cl_nogame);
        body.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);


        TextView tv_game_title = findViewById(R.id.current_game_tv_game_title);
        TextView tv_game_player1 = findViewById(R.id.current_game_tv_player1);
        TextView tv_game_player2 = findViewById(R.id.current_game_tv_player2);
        TextView tv_game_field = findViewById(R.id.current_game_tv_field);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Interceptor interceptor = new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    }
                };
                OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
                okHttpBuilder.addInterceptor(interceptor);
                OkHttpClient okHttpClient = okHttpBuilder.build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(MainActivity.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(okHttpClient)
                        .build();

                IGame service = retrofit.create(IGame.class);

                service.getCurrentGame(prefs.getInt("playerId", 0)).enqueue(new Callback<GameResponseWrapper>() {

                    @Override
                    public void onResponse(Call<GameResponseWrapper> call, Response<GameResponseWrapper> response) {
                        if(response.body() != null){
                            Game game = response.body().result;
                            tv_game_title.setText("Wedstrijd #" + game.id);
                            tv_game_player1.setText(game.player1.name);
                            tv_game_player2.setText(game.player2.name);
                            tv_game_field.setText(game.field.name);

                            empty.setVisibility(View.GONE);
                            body.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<GameResponseWrapper> call, Throwable t) {
                        Toast.makeText(CurrentGameActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();


        Button logOutButton = findViewById(R.id.current_game_bn_logout);

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(MainActivity.BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        IAuth service = retrofit.create(IAuth.class);

                        service.logout(new LogoutModel(prefs.getString("playerEmail", ""))).enqueue(new Callback<Object>() {

                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                if(response.body() != null){
                                    SharedPreferences.Editor edit = prefs.edit();
                                    edit.remove("token");
                                    edit.remove("playerId");
                                    edit.remove("playerEmail");
                                    edit.apply();

                                    startActivity(new Intent(CurrentGameActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            }

                            @Override
                            public void onFailure(Call<Object> call, Throwable t) {
                                Toast.makeText(CurrentGameActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }
}