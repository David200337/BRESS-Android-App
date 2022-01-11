package nl.bress.tournamentplanner.view;

import static nl.bress.tournamentplanner.view.MainActivity.BASE_URL;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.data.factory.ServiceFactory;
import nl.bress.tournamentplanner.data.services.IAuth;
import nl.bress.tournamentplanner.data.services.IGame;
import nl.bress.tournamentplanner.data.services.IPlayer;
import nl.bress.tournamentplanner.data.services.ISkillLevel;
import nl.bress.tournamentplanner.domain.Game;
import nl.bress.tournamentplanner.data.models.GameResponseWrapper;
import nl.bress.tournamentplanner.data.models.LogoutModel;
import nl.bress.tournamentplanner.domain.Player;
import nl.bress.tournamentplanner.data.models.PlayerResponseWrapper;
import nl.bress.tournamentplanner.data.models.ScoreModel;
import nl.bress.tournamentplanner.domain.SkillLevel;
import nl.bress.tournamentplanner.data.models.SkillLevelResponseWrapper;
import nl.bress.tournamentplanner.data.models.UpdatePlayerModel;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrentGameActivity extends AppCompatActivity {
    // Constants
    public static final String TAG = CurrentGameActivity.class.getSimpleName();

    // Utilities
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefs_editor;
    private Gson gson;
    private String token;
    private IAuth authService;
    private IGame gameService;
    private IPlayer playerService;
    private ISkillLevel skillLevelService;

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout body;
    private ConstraintLayout empty;
    private ConstraintLayout next;
    private ImageView iv_refresh;
    private TextView tv_game_title;
    private TextView tv_game_player1;
    private TextView tv_game_player2;
    private TextView tv_game_field;
    private TextView tv_nextgame_title;
    private TextView tv_nextgame_player1;
    private TextView tv_nextgame_player2;

    //Data
    private ArrayAdapter skillLevelsAdapter;
    private SkillLevel[] skillLevels;
    private List<String> skillLevelNames;
    private Player player;
    private Game currentGame;
    private Game nextGame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_game);

        prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs_editor = prefs.edit();
        gson = new Gson();
        token = prefs.getString(MainActivity.PREFS_TOKEN, "");
        authService = ServiceFactory.createAuthService();
        gameService = ServiceFactory.createGameService(token);
        playerService = ServiceFactory.createPlayerService(token);
        skillLevelService = ServiceFactory.createSkillLevelService(token);

        getSkillLevels();
        getPlayer();

        swipeRefreshLayout = findViewById(R.id.current_game_srl);

        iv_refresh = findViewById(R.id.current_game_iv_refresh);

        body = findViewById(R.id.current_game_cl_body);
        empty = findViewById(R.id.current_game_cl_nogame);
        next = findViewById(R.id.next_game_cl_body);

        next.setVisibility(View.GONE);
        body.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);

        tv_nextgame_title = findViewById(R.id.next_game_tv_game_title);
        tv_nextgame_player1 = findViewById(R.id.next_game_tv_player1);
        tv_nextgame_player2 = findViewById(R.id.next_game_tv_player2);

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
                                .baseUrl(BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        IAuth service = retrofit.create(IAuth.class);

                        service.logout(new LogoutModel(prefs.getString("playerEmail", ""))).enqueue(new Callback<Object>() {

                            @Override
                            public void onResponse(Call<Object> call, Response<Object> response) {
                                if(response.body() != null){
                                    prefs_editor.remove(MainActivity.PREFS_TOKEN);
                                    prefs_editor.remove(MainActivity.PREFS_PLAYER_ID);
                                    prefs_editor.remove(MainActivity.PREFS_PLAYER_EMAIL);
                                    prefs_editor.apply();

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
                openAccountDialog();
            }
        });
    }

    private void getSkillLevels() {
        skillLevelService.getAllSkillLevels().enqueue(new Callback<SkillLevelResponseWrapper>() {
            @Override
            public void onResponse(Call<SkillLevelResponseWrapper> call, Response<SkillLevelResponseWrapper> response) {
                if(response.body() != null) {
                    skillLevels = response.body().getResult();

                    skillLevelNames = new ArrayList<>();
                    for(SkillLevel sl : skillLevels) {
                        skillLevelNames.add(sl.getName());
                    }
                    skillLevelsAdapter = new ArrayAdapter(CurrentGameActivity.this, R.layout.support_simple_spinner_dropdown_item, skillLevelNames);
                }
            }
            @Override
            public void onFailure(Call<SkillLevelResponseWrapper> call, Throwable t) {
                Log.d(TAG, "" + t.getMessage());
            }
        });
    }

    private void getPlayer() {
        playerService.getPlayerById(prefs.getInt(MainActivity.PREFS_PLAYER_ID, 0)).enqueue(new Callback<PlayerResponseWrapper>() {
            @Override
            public void onResponse(Call<PlayerResponseWrapper> call, Response<PlayerResponseWrapper> response) {
                if(response.body() != null) {
                    Player result = response.body().getResult();
                    player = result;
                }
            }
            @Override
            public void onFailure(Call<PlayerResponseWrapper> call, Throwable t) {
                Log.d(TAG, "" + t.getMessage());
            }
        });
    }

    private void getData(){
        swipeRefreshLayout.setRefreshing(true);
        empty.setVisibility(View.VISIBLE);
        body.setVisibility(View.GONE);
        next.setVisibility(View.GONE);

        gameService.getCurrentGame(prefs.getInt(MainActivity.PREFS_PLAYER_ID, 0)).enqueue(new Callback<GameResponseWrapper>() {

            @Override
            public void onResponse(Call<GameResponseWrapper> call, Response<GameResponseWrapper> response) {
                if(response.body() != null){
                    currentGame = response.body().result;
                    if(currentGame != null){
                        tv_game_title.setText("Wedstrijd #" + currentGame.getId());
                        tv_game_player1.setText(currentGame.getPlayer1().getName());
                        tv_game_player2.setText(currentGame.getPlayer2().getName());
                        tv_game_field.setText(currentGame.getField().getName());

                        empty.setVisibility(View.GONE);
                        body.setVisibility(View.VISIBLE);
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

        gameService.getNextGame(prefs.getInt(MainActivity.PREFS_PLAYER_ID, 0)).enqueue(new Callback<GameResponseWrapper>() {
            @Override
            public void onResponse(Call<GameResponseWrapper> call, Response<GameResponseWrapper> response) {
                if(response.body() != null){
                    nextGame = response.body().result;
                    if(nextGame != null){
                        tv_nextgame_title.setText("Wedstrijd #" + nextGame.getId());
                        tv_nextgame_player1.setText(nextGame.getPlayer1().getName());
                        tv_nextgame_player2.setText(nextGame.getPlayer2().getName());

                        empty.setVisibility(View.GONE);
                        next.setVisibility(View.VISIBLE);
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

    public void openAccountDialog() {
        // Prepare dialog
        View accountDialogView = LayoutInflater.from(this).inflate(R.layout.layout_account_dialog, null);
        AlertDialog accountDialog = new AlertDialog.Builder(this)
                .setView(accountDialogView)
                .create();
        accountDialog.show();
        Spinner spinner = accountDialogView.findViewById(R.id.edit_spinner);
        spinner.setAdapter(skillLevelsAdapter);

        // Views
        Button confirm_bn = accountDialogView.findViewById(R.id.editPlayer_bn_confirm);
        EditText name_input = accountDialogView.findViewById(R.id.editPlayer_name_input);
        name_input.setText(player.getName());

        // Close dialog
        ImageView close_btn = accountDialogView.findViewById(R.id.dialog_close);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accountDialog.dismiss();
            }
        });

        // Update player
        confirm_bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SkillLevel selectedSkillLevel = skillLevels[spinner.getSelectedItemPosition()];

                playerService.updatePlayer(player.getId(), new UpdatePlayerModel(name_input.getText().toString(), selectedSkillLevel.getId())).enqueue(new Callback<PlayerResponseWrapper>() {
                    @Override
                    public void onResponse(Call<PlayerResponseWrapper> call, Response<PlayerResponseWrapper> response) {
                        if (response.body() != null) {
                            Player updated = response.body().getResult();
                            player = updated;
                            accountDialog.dismiss();
                        }
                    }
                    @Override
                    public void onFailure(Call<PlayerResponseWrapper> call, Throwable t) {
                        Log.d(TAG, "" + t.getMessage());
                    }
                });
            }
        });
    }

    public void openDialog() {
        // Prepare dialog
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null);
        AlertDialog scoreDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Views
        Button confirm_btn = dialogView.findViewById(R.id.dialog_confirm_btn);

        TextView dialog_title = dialogView.findViewById(R.id.dialog_title);
        TextView dialog_subtitle = dialogView.findViewById(R.id.dialog_subtitle);
        dialog_title.setText("Score invullen voor wedstrijd #" + currentGame.getId());
        dialog_subtitle.setText(currentGame.getPlayer1().getName() + " tegen " + currentGame.getPlayer2().getName() + " in " + currentGame.getField().getName());

        EditText set1_player1 = dialogView.findViewById(R.id.dialog_set1).findViewById(R.id.player1_score);
        EditText set1_player2 = dialogView.findViewById(R.id.dialog_set1).findViewById(R.id.player2_score);
        EditText set2_player1 = dialogView.findViewById(R.id.dialog_set2).findViewById(R.id.player1_score);
        EditText set2_player2 = dialogView.findViewById(R.id.dialog_set2).findViewById(R.id.player2_score);
        EditText set3_player1 = dialogView.findViewById(R.id.dialog_set3).findViewById(R.id.player1_score);
        EditText set3_player2 = dialogView.findViewById(R.id.dialog_set3).findViewById(R.id.player2_score);

        set1_player1.setHint(currentGame.getPlayer1().getName());
        set2_player1.setHint(currentGame.getPlayer1().getName());
        set3_player1.setHint(currentGame.getPlayer1().getName());
        set1_player2.setHint(currentGame.getPlayer2().getName());
        set2_player2.setHint(currentGame.getPlayer2().getName());
        set3_player2.setHint(currentGame.getPlayer2().getName());

        // Close dialog
        ImageView close_btn = dialogView.findViewById(R.id.dialog_close);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scoreDialog.dismiss();
            }
        });

        // Confirm score
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> scoreA = new ArrayList<>();
                List<Integer> scoreB = new ArrayList<>();

                // Basic validation check
                if(set1_player1.getText().toString().isEmpty() || set1_player2.getText().toString().isEmpty() || set2_player1.getText().toString().isEmpty() || set2_player2.getText().toString().isEmpty()) {
                    Toast.makeText(CurrentGameActivity.this, "Vul minstens 2 sets in", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    scoreA.add(Integer.parseInt(set1_player1.getText().toString()));
                    scoreA.add(Integer.parseInt(set2_player1.getText().toString()));
                    scoreB.add(Integer.parseInt(set1_player2.getText().toString()));
                    scoreB.add(Integer.parseInt(set2_player2.getText().toString()));
                }

                if(!set3_player1.getText().toString().isEmpty() && !set3_player2.getText().toString().isEmpty()) {
                    scoreA.add(Integer.parseInt(set3_player1.getText().toString()));
                    scoreB.add(Integer.parseInt(set3_player2.getText().toString()));
                }

                // Apply score
                gameService.addScoreToCurrentGame(prefs.getInt(MainActivity.PREFS_PLAYER_ID, 0), currentGame.getId(), new ScoreModel(scoreA, scoreB)).enqueue(new Callback<Object>() {

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
                        Log.d(TAG, "" + t.getMessage());
                    }
                });
            }
        });
        scoreDialog.show();
    }
}