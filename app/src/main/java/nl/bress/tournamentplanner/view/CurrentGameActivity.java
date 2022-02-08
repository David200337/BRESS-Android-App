package nl.bress.tournamentplanner.view;

import androidx.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import nl.bress.tournamentplanner.R;
import nl.bress.tournamentplanner.data.factory.ServiceFactory;
import nl.bress.tournamentplanner.data.models.ScoreModel;
import nl.bress.tournamentplanner.data.services.IAuth;
import nl.bress.tournamentplanner.data.services.IGame;
import nl.bress.tournamentplanner.data.services.IPlayer;
import nl.bress.tournamentplanner.data.services.ISkillLevel;
import nl.bress.tournamentplanner.domain.Game;
import nl.bress.tournamentplanner.data.models.GameResponseWrapper;
import nl.bress.tournamentplanner.data.models.LogoutModel;
import nl.bress.tournamentplanner.domain.Player;
import nl.bress.tournamentplanner.data.models.PlayerResponseWrapper;
import nl.bress.tournamentplanner.domain.SkillLevel;
import nl.bress.tournamentplanner.data.models.SkillLevelResponseWrapper;
import nl.bress.tournamentplanner.data.models.UpdatePlayerModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentGameActivity extends AppCompatActivity {
    // Constants
    public static final String TAG = CurrentGameActivity.class.getSimpleName();

    // Utilities
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;
    private IGame gameService;
    private IAuth authService;
    private IPlayer playerService;
    private ISkillLevel skillLevelService;

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout body;
    private ConstraintLayout empty;
    private ConstraintLayout next;
    private TextView tvGameTitle;
    private TextView tvGamePlayer1;
    private TextView tvGamePlayer2;
    private TextView tvGameField;
    private TextView tvNextGameTitle;
    private TextView tvNextGamePlayer1;
    private TextView tvNextGamePlayer2;

    //Data
    private ArrayAdapter<String> skillLevelsAdapter;
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
        prefsEditor = prefs.edit();
        String token = prefs.getString(MainActivity.PREFS_TOKEN, "");
        authService = ServiceFactory.createAuthService();
        gameService = ServiceFactory.createGameService(token);
        playerService = ServiceFactory.createPlayerService(token);
        skillLevelService = ServiceFactory.createSkillLevelService(token);

        getSkillLevels();
        getPlayer();

        swipeRefreshLayout = findViewById(R.id.current_game_srl);

        ImageView ivAccount = findViewById(R.id.current_game_iv_refresh);

        body = findViewById(R.id.current_game_cl_body);
        empty = findViewById(R.id.current_game_cl_nogame);
        next = findViewById(R.id.next_game_cl_body);

        next.setVisibility(View.GONE);
        body.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);

        tvNextGameTitle = findViewById(R.id.next_game_tv_game_title);
        tvNextGamePlayer1 = findViewById(R.id.next_game_tv_player1);
        tvNextGamePlayer2 = findViewById(R.id.next_game_tv_player2);

        tvGameTitle = findViewById(R.id.current_game_tv_game_title);
        tvGamePlayer1 = findViewById(R.id.current_game_tv_player1);
        tvGamePlayer2 = findViewById(R.id.current_game_tv_player2);
        tvGameField = findViewById(R.id.current_game_tv_field);
        Button btnGameScore = findViewById(R.id.current_game_bn_score);
        btnGameScore.setOnClickListener(view -> openDialog());

        getData();

        Button logOutButton = findViewById(R.id.current_game_bn_logout);
        logOutButton.setOnClickListener(View -> authService.logout(new LogoutModel(prefs.getString(MainActivity.PREFS_PLAYER_EMAIL, ""))).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if(response.body() != null){
                    prefsEditor.remove(MainActivity.PREFS_TOKEN);
                    prefsEditor.remove(MainActivity.PREFS_PLAYER_ID);
                    prefsEditor.remove(MainActivity.PREFS_PLAYER_EMAIL);
                    prefsEditor.apply();
                    startActivity(new Intent(CurrentGameActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            }
            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                Toast.makeText(CurrentGameActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }));

        swipeRefreshLayout.setOnRefreshListener(this::getData);

        ivAccount.setOnClickListener(view -> openAccountDialog());
    }

    private void getSkillLevels() {
        skillLevelService.getAllSkillLevels().enqueue(new Callback<SkillLevelResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<SkillLevelResponseWrapper> call, @NonNull Response<SkillLevelResponseWrapper> response) {
                if(response.body() != null) {
                    skillLevels = response.body().getResult();

                    skillLevelNames = new ArrayList<>();
                    for(SkillLevel sl : skillLevels) {
                        skillLevelNames.add(sl.getName());
                    }
                    skillLevelsAdapter = new ArrayAdapter<>(CurrentGameActivity.this, R.layout.support_simple_spinner_dropdown_item, skillLevelNames);
                }
            }
            @Override
            public void onFailure(@NonNull Call<SkillLevelResponseWrapper> call, @NonNull Throwable t) {
                Log.d(TAG, "" + t.getMessage());
            }
        });
    }

    private void getPlayer() {
        playerService.getPlayerById(prefs.getInt(MainActivity.PREFS_PLAYER_ID, 0)).enqueue(new Callback<PlayerResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<PlayerResponseWrapper> call, @NonNull Response<PlayerResponseWrapper> response) {
                if(response.body() != null) {
                    player = response.body().getResult();
                }
            }
            @Override
            public void onFailure(@NonNull Call<PlayerResponseWrapper> call, @NonNull Throwable t) {
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
            public void onResponse(@NonNull Call<GameResponseWrapper> call, @NonNull Response<GameResponseWrapper> response) {
                if(response.body() != null){
                    currentGame = response.body().getResult();
                    if(currentGame != null){
                        tvGameTitle.setText("Wedstrijd #" + currentGame.getId());
                        tvGamePlayer1.setText(currentGame.getPlayer1().getFirstName() + " " + currentGame.getPlayer1().getLastName());
                        tvGamePlayer2.setText(currentGame.getPlayer2().getFirstName() + " "  + currentGame.getPlayer2().getLastName());
                        tvGameField.setText(currentGame.getField().getName());

                        empty.setVisibility(View.GONE);
                        body.setVisibility(View.VISIBLE);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<GameResponseWrapper> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(CurrentGameActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        gameService.getNextGame(prefs.getInt(MainActivity.PREFS_PLAYER_ID, 0)).enqueue(new Callback<GameResponseWrapper>() {
            @Override
            public void onResponse(@NonNull Call<GameResponseWrapper> call, @NonNull Response<GameResponseWrapper> response) {
                if(response.body() != null){
                    nextGame = response.body().getResult();
                    if(nextGame != null){
                        tvNextGameTitle.setText("Wedstrijd #" + nextGame.getId());
                        tvNextGamePlayer1.setText(nextGame.getPlayer1().getFirstName() + " "  + nextGame.getPlayer1().getLastName());
                        tvNextGamePlayer2.setText(nextGame.getPlayer2().getFirstName() + " "  + nextGame.getPlayer2().getLastName());

                        empty.setVisibility(View.GONE);
                        next.setVisibility(View.VISIBLE);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<GameResponseWrapper> call, @NonNull Throwable t) {
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

        // Views
        Spinner spinner = accountDialogView.findViewById(R.id.edit_spinner);
        spinner.setAdapter(skillLevelsAdapter);
        Button confirm_bn = accountDialogView.findViewById(R.id.editPlayer_bn_confirm);
        EditText first_name = accountDialogView.findViewById(R.id.editPlayer_firstName_input);
        EditText last_name = accountDialogView.findViewById(R.id.editPlayer_lastName_input);

        first_name.setText(player.getFirstName());
        last_name.setText(player.getLastName());

        for(int i = 0; i < skillLevels.length; i ++) {
            SkillLevel selected = skillLevels[i];
            if(player.getSkillLevel().getId() == selected.getId()) {
                spinner.setSelection(i);
            }
        }

        // Close dialog
        ImageView close_btn = accountDialogView.findViewById(R.id.dialog_close);
        close_btn.setOnClickListener(view -> accountDialog.dismiss());

        // Update player
        confirm_bn.setOnClickListener(view -> {
            SkillLevel selectedSkillLevel = skillLevels[spinner.getSelectedItemPosition()];

            playerService.updatePlayer(player.getId(), new UpdatePlayerModel(first_name.getText().toString(), last_name.getText().toString(), selectedSkillLevel.getId())).enqueue(new Callback<PlayerResponseWrapper>() {
                @Override
                public void onResponse(@NonNull Call<PlayerResponseWrapper> call, @NonNull Response<PlayerResponseWrapper> response) {
                    if (response.body() != null) {
                        Toast.makeText(getBaseContext(),"Account aangepast", Toast.LENGTH_SHORT).show();
                        player = response.body().getResult();
                        accountDialog.dismiss();
                    }
                }
                @Override
                public void onFailure(@NonNull Call<PlayerResponseWrapper> call, @NonNull Throwable t) {
                    Log.d(TAG, "" + t.getMessage());
                }
            });
        });
    }

    /**
     * Validates one single set based on the score
     *
     * @param score1 Player1 points from set 1
     * @param score2 Player2 points from set 1
     * @return If score is invalid it wil return an error message, else it will return a null
     */
    private String validateSet(int score1, int score2) {
        if (score1 < 11 && score2 < 11) {
            // Winner must have at least 11 points or higher
            return "Winnaar heeft minimaal 11 punten nodig";
        }

        if (Math.abs(score1 - score2) < 2) {
            // Point difference of 2 is needed to end (win) a match
            return "Minimum puntenverschil is 2";
        }

        if(Math.abs(score1 - score2) != 2 && (score1 > 11 || score2 > 11)) {
            // If a score exceeds 11 points the maximum point difference can only be 2
            return "Bij een score hoger dan 11 moet het verschil 2 zijn";
        }

        return null;
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
        dialog_subtitle.setText(currentGame.getPlayer1().getFirstName() + " tegen " + currentGame.getPlayer2().getFirstName() + " in " + currentGame.getField().getName());

        View set1_layout = dialogView.findViewById(R.id.dialog_set1);
        View set2_layout = dialogView.findViewById(R.id.dialog_set2);
        View set3_layout = dialogView.findViewById(R.id.dialog_set3);

        EditText et_set1_player1 = set1_layout.findViewById(R.id.player1_score);
        EditText et_set1_player2 = set1_layout.findViewById(R.id.player2_score);
        EditText et_set2_player1 = set2_layout.findViewById(R.id.player1_score);
        EditText et_set2_player2 = set2_layout.findViewById(R.id.player2_score);
        EditText et_set3_player1 = set3_layout.findViewById(R.id.player1_score);
        EditText et_set3_player2 = set3_layout.findViewById(R.id.player2_score);

        et_set1_player1.setHint(currentGame.getPlayer1().getFirstName());
        et_set2_player1.setHint(currentGame.getPlayer1().getFirstName());
        et_set3_player1.setHint(currentGame.getPlayer1().getFirstName());
        et_set1_player2.setHint(currentGame.getPlayer2().getFirstName());
        et_set2_player2.setHint(currentGame.getPlayer2().getFirstName());
        et_set3_player2.setHint(currentGame.getPlayer2().getFirstName());

        // Close dialog
        ImageView close_btn = dialogView.findViewById(R.id.dialog_close);
        close_btn.setOnClickListener(view -> scoreDialog.dismiss());

        // Confirm score
        confirm_btn.setOnClickListener(view -> {
            ProgressBar progressBar = dialogView.findViewById(R.id.progress);
            progressBar.setVisibility(View.VISIBLE);
            List<Integer> scoreA = new ArrayList<>();
            List<Integer> scoreB = new ArrayList<>();
            
            int set1_player1 = -1;
            int set1_player2 = -1;
            int set2_player1 = -1;
            int set2_player2 = -1;
            int set3_player1 = -1;
            int set3_player2 = -1;

            try {
                set1_player1 = Integer.parseInt(et_set1_player1.getText().toString());
                set1_player2 = Integer.parseInt(et_set1_player2.getText().toString());
                set2_player1 = Integer.parseInt(et_set2_player1.getText().toString());
                set2_player2 = Integer.parseInt(et_set2_player2.getText().toString());
                set3_player1 = Integer.parseInt(et_set3_player1.getText().toString());
                set3_player2 = Integer.parseInt(et_set3_player2.getText().toString());
            } catch (Exception e) { }

            // Validate
            TextView error = dialogView.findViewById(R.id.score_error);
            boolean isValid = true;

            if(set1_player1 < 0 || set1_player2 < 0 || set2_player1 < 0 || set2_player2 < 0) {
                // validation first 2 sets for empty fields && negative digits
                error.setText("Vul minstens 2 sets in (geen negatieve getallen)");
                error.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                isValid = false;
            }

            String set1 = validateSet(set1_player1, set1_player2);
            String set2 = validateSet(set2_player1, set2_player2);

            if(isValid && set1 != null) {
                error.setText(set1);
                error.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                isValid = false;
            } else if (isValid && set2 != null) {
                error.setText(set2);
                error.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                isValid = false;
            } else if (isValid) {
                scoreA.add(set1_player1);
                scoreA.add(set2_player1);
                scoreB.add(set1_player2);
                scoreB.add(set2_player2);
            }

            // set 3 validation
            boolean extraSetNeeded = false;

            if (set1_player1 > set1_player2 && set2_player1 < set2_player2) {
                extraSetNeeded = true;
            }  else if (set1_player1 < set1_player2 && set2_player1 > set2_player2) {
                extraSetNeeded = true;
            } else {
                extraSetNeeded = false;
            }

            if(isValid && extraSetNeeded) {
                if(set3_player1 < 0 || set3_player2 < 0) {
                    // Missing scores
                    error.setText("Set 3 vereist bij een gelijke stand");
                    error.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    isValid = false;
                } else {
                    String set3 = validateSet(set3_player1, set3_player2);
                    if(set3 != null) {
                        error.setText(set3);
                        error.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        isValid = false;
                    } else if (isValid) {
                        scoreA.add(set3_player1);
                        scoreB.add(set3_player2);
                    }
                }
            } else if(isValid && !extraSetNeeded && (set3_player1 > -1 || set3_player2 > -1)) {
                error.setText("3e set is overbodig");
                error.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                isValid = false;
            }

            if(isValid) {
                // Apply score
                gameService.addScoreToCurrentGame(prefs.getInt(MainActivity.PREFS_PLAYER_ID, 0), currentGame.getId(), new ScoreModel(scoreA, scoreB)).enqueue(new Callback<Object>() {

                    @Override
                    public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                        progressBar.setVisibility(View.INVISIBLE);

                        if(response.body() != null){
                            Toast.makeText(getBaseContext(),"Score toegevoegd", Toast.LENGTH_SHORT).show();
                            error.setVisibility(View.INVISIBLE);
                            scoreDialog.dismiss();
                            getData();
                        } else if (response.errorBody() != null) {
                            error.setText("Score is al ingevuld");
                            error.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                        Log.d(TAG, "" + t.getMessage());
                    }
                });
                error.setVisibility(View.INVISIBLE);
            }
        });
        scoreDialog.show();
    }
}