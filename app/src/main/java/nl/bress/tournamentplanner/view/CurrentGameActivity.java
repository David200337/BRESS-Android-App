package nl.bress.tournamentplanner.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import nl.bress.tournamentplanner.domain.ScoreModel;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrentGameActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_game);

        prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");

        ConstraintLayout body = findViewById(R.id.current_game_cl_body);
        ConstraintLayout empty = findViewById(R.id.current_game_cl_nogame);
        body.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);


        TextView tv_game_title = findViewById(R.id.current_game_tv_game_title);
        TextView tv_game_player1 = findViewById(R.id.current_game_tv_player1);
        TextView tv_game_player2 = findViewById(R.id.current_game_tv_player2);
        TextView tv_game_field = findViewById(R.id.current_game_tv_field);
        Button btn_game_score = findViewById(R.id.current_game_bn_score);
        btn_game_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                            if(game != null){
                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putInt("gameId", game.getId());
                                edit.apply();

                                tv_game_title.setText("Wedstrijd #" + game.getId());
                                tv_game_player1.setText(game.getPlayer1().getName());
                                tv_game_player2.setText(game.getPlayer2().getName());
                                tv_game_field.setText(game.getField().getName());

                                empty.setVisibility(View.GONE);
                                body.setVisibility(View.VISIBLE);
                            }
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

    public void openDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null);
        AlertDialog scoreDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Score invullen")
                .setNegativeButton("Annuleren", null)
                .setPositiveButton("ok", null)
                .create();

        RadioButton rb1 = dialogView.findViewById(R.id.RB_1);
        RadioButton rb2 = dialogView.findViewById(R.id.RB_2);
        RadioButton rb3 = dialogView.findViewById(R.id.RB_3);
        RadioGroup rg1 = dialogView.findViewById(R.id.RG_1);
        RadioGroup rg2 = dialogView.findViewById(R.id.RG_2);
        RadioGroup rg3 = dialogView.findViewById(R.id.RG_3);

        scoreDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = ((AlertDialog) scoreDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(rg1.getCheckedRadioButtonId() != -1 && rg2.getCheckedRadioButtonId() != -1 && rg3.getCheckedRadioButtonId() != -1) {
                            boolean[] score = new boolean[3];
                            score[0] = rb1.isChecked();
                            score[1] = rb2.isChecked();
                            score[2] = rb3.isChecked();

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
                                    Log.d("Data sent:", "PlayerId: " + prefs.getInt("playerId", 0) + " gameId: " + prefs.getInt("gameId", 0));
                                    service.addScoreToCurrentGame(prefs.getInt("playerId", 0), prefs.getInt("gameId", 0), new ScoreModel(score)).enqueue(new Callback<Object>() {

                                        @Override
                                        public void onResponse(Call<Object> call, Response<Object> response) {
                                            if(response.body() != null){
                                                Toast.makeText(getBaseContext(),"Score toegevoegd", Toast.LENGTH_SHORT).show();
                                                scoreDialog.dismiss();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Object> call, Throwable t) {
                                            Log.d("test", "onFailure: ");
                                            Toast.makeText(CurrentGameActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).start();
                        } else {
                            Toast.makeText(view.getContext(), "Vul alles in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        scoreDialog.show();
    }
}