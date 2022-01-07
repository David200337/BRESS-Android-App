package nl.bress.tournamentplanner.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.dao.interfaces.IAuth;
import nl.bress.tournamentplanner.dao.interfaces.IGame;
import nl.bress.tournamentplanner.domain.Game;
import nl.bress.tournamentplanner.domain.GameResponseWrapper;
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
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout body;
    private ConstraintLayout empty;

    private ImageView iv_refresh;

    private TextView tv_game_title;
    private TextView tv_game_player1;
    private TextView tv_game_player2;
    private TextView tv_game_field;
    private Button btn_game_score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_game);

        prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");

        swipeRefreshLayout = findViewById(R.id.current_game_srl);

        iv_refresh = findViewById(R.id.current_game_iv_refresh);

        body = findViewById(R.id.current_game_cl_body);
        empty = findViewById(R.id.current_game_cl_nogame);
        body.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);


        tv_game_title = findViewById(R.id.current_game_tv_game_title);
        tv_game_player1 = findViewById(R.id.current_game_tv_player1);
        tv_game_player2 = findViewById(R.id.current_game_tv_player2);
        tv_game_field = findViewById(R.id.current_game_tv_field);
        Button btn_game_score = findViewById(R.id.current_game_bn_score);
        btn_game_score.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        getData();

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

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });

        iv_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });
    }

    private void getData(){
        swipeRefreshLayout.setRefreshing(true);
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
                                Gson gson = new Gson();
                                String json = gson.toJson(game);
                                edit.putString("currentGame", json);

                                edit.apply();

                                tv_game_title.setText("Wedstrijd #" + game.getId());
                                tv_game_player1.setText(game.getPlayer1().getName());
                                tv_game_player2.setText(game.getPlayer2().getName());
                                tv_game_field.setText(game.getField().getName());

                                empty.setVisibility(View.GONE);
                                body.setVisibility(View.VISIBLE);
                            } else {
                                empty.setVisibility(View.VISIBLE);
                                body.setVisibility(View.GONE);
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<GameResponseWrapper> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(CurrentGameActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    public void openDialog() {
        // Prepare dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null);
        AlertDialog scoreDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        Gson gson = new Gson();
        String json = prefs.getString("currentGame", "");
        Game game = gson.fromJson(json, Game.class);

        TextView dialog_title = dialogView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = dialogView.findViewById(R.id.dialog_subtitle);

        RadioButton rb1 = dialogView.findViewById(R.id.RB_1);
        RadioButton rb2_1 = dialogView.findViewById(R.id.RB2_1);
        RadioButton rb2 = dialogView.findViewById(R.id.RB_2);
        RadioButton rb2_2 = dialogView.findViewById(R.id.RB2_2);
        RadioButton rb3 = dialogView.findViewById(R.id.RB_3);
        RadioButton rb2_3 = dialogView.findViewById(R.id.RB2_3);

        RadioGroup rg1 = dialogView.findViewById(R.id.RG_1);
        RadioGroup rg2 = dialogView.findViewById(R.id.RG_2);
        RadioGroup rg3 = dialogView.findViewById(R.id.RG_3);

        dialog_title.setText("Score invullen voor wedstrijd #" + game.getId());
        dialog_subtitle.setText(game.getPlayer1().getName() + " tegen " + game.getPlayer2().getName() + " in " + game.getField().getName());

        rb1.setText(game.getPlayer1().getName());
        rb2.setText(game.getPlayer1().getName());
        rb2_1.setText(game.getPlayer2().getName());
        rb2_2.setText(game.getPlayer2().getName());

        TextView set3 = dialogView.findViewById(R.id.dialog_set3);

        //Listeners for showing set 3
        rg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(R.id.RB_1 == i) {
                    if (rb2.isChecked()) {
                        rb3.setText("Niemand");
                        rb3.setChecked(true);
                        rb3.setBackgroundResource(R.drawable.radio_single);
                        rb2_3.setVisibility(View.GONE);
                    } else {
                        rb3.setText(game.getPlayer1().getName());
                        rb3.setBackgroundResource(R.drawable.radio_left_selector);
                        rb2_3.setText(game.getPlayer2().getName());
                        rb2_3.setVisibility(View.VISIBLE);
                    }
                } else if (R.id.RB2_1 == i) {
                    if(rb2_2.isChecked()) {
                        rb3.setText("Niemand");
                        rb3.setChecked(true);
                        rb3.setBackgroundResource(R.drawable.radio_single);
                        rb2_3.setVisibility(View.GONE);
                    } else {
                        rb3.setText(game.getPlayer1().getName());
                        rb3.setBackgroundResource(R.drawable.radio_left_selector);
                        rb2_3.setText(game.getPlayer2().getName());
                        rb2_3.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        rg2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(R.id.RB_2 == i) {
                    if (rb1.isChecked()) {
                        rb3.setText("Niemand");
                        rb3.setChecked(true);
                        rb3.setBackgroundResource(R.drawable.radio_single);
                        rb2_3.setVisibility(View.GONE);
                    } else {
                        rb3.setText(game.getPlayer1().getName());
                        rb3.setBackgroundResource(R.drawable.radio_left_selector);
                        rb2_3.setText(game.getPlayer2().getName());
                        rb2_3.setVisibility(View.VISIBLE);
                    }
                } else if (R.id.RB2_2 == i) {
                    if(rb2_1.isChecked()) {
                        rb3.setText("Niemand");
                        rb3.setChecked(true);
                        rb3.setBackgroundResource(R.drawable.radio_single);
                        rb2_3.setVisibility(View.GONE);
                    } else {
                        rb3.setText(game.getPlayer1().getName());
                        rb3.setBackgroundResource(R.drawable.radio_left_selector);
                        rb2_3.setText(game.getPlayer2().getName());
                        rb2_3.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        // Close dialog
        ImageView close_btn = dialogView.findViewById(R.id.dialog_close);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scoreDialog.dismiss();
            }
        });

        // Confirm score
        Button confirm_btn = dialogView.findViewById(R.id.dialog_confirm_btn);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int setsPlayed = 2;
                if(!(rb1.isChecked() && rb2.isChecked() || rb2_1.isChecked() && rb2_2.isChecked())) {
                    setsPlayed ++;
                }
                boolean[] score = new boolean[setsPlayed];

                score[0] = rb1.isChecked();
                score[1] = rb2.isChecked();

                if(score.length > 2) {
                    score[2] = rb3.isChecked();
                }

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
                        service.addScoreToCurrentGame(prefs.getInt("playerId", 0), game.getId(), new ScoreModel(score)).enqueue(new Callback<Object>() {

                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                if(response.body() != null){
                                    Toast.makeText(getBaseContext(),"Score toegevoegd", Toast.LENGTH_SHORT).show();
                                    scoreDialog.dismiss();
                                    getData();
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
        scoreDialog.show();
    }
}