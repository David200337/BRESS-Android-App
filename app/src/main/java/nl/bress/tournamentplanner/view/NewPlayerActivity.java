package nl.bress.tournamentplanner.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
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
import nl.bress.tournamentplanner.data.models.LoginModel;
import nl.bress.tournamentplanner.data.models.LoginResponse;
import nl.bress.tournamentplanner.data.models.LoginResponseWrapper;
import nl.bress.tournamentplanner.data.models.NewPlayerModel;
import nl.bress.tournamentplanner.data.models.PlayerResponseWrapper;
import nl.bress.tournamentplanner.domain.SkillLevel;
import nl.bress.tournamentplanner.data.models.SkillLevelResponseWrapper;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewPlayerActivity extends AppCompatActivity {
    // Constants
    public static final String TAG = NewPlayerActivity.class.getSimpleName();

    // Utilities
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefs_editor;
    private String token;
    private IAuth authService;
    private IPlayer playerService;
    private ISkillLevel skillLevelService;

    // Views
    private Spinner spinner;
    private Button confirm_btn;
    private EditText name_input;

    // Data
    private SkillLevel[] skillLevels;
    private List<String> skillLevelNames;
    private String email;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_player);

        prefs = getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        prefs_editor = prefs.edit();
        token = prefs.getString(MainActivity.PREFS_TOKEN, "");
        authService = ServiceFactory.createAuthService();
        playerService = ServiceFactory.createPlayerService(token);
        skillLevelService = ServiceFactory.createSkillLevelService(token);

        Intent intent = getIntent();
        if(intent.hasExtra(RegisterActivity.INTENT_EMAIL)){
            email = intent.getStringExtra(RegisterActivity.INTENT_EMAIL);
        }
        if(intent.hasExtra(RegisterActivity.INTENT_PASS)) {
            pass = intent.getStringExtra(RegisterActivity.INTENT_PASS);
        }

        getSkillLevels();

        spinner = findViewById(R.id.edit_spinner);
        confirm_btn = findViewById(R.id.editPlayer_bn_confirm);
        name_input = findViewById(R.id.editPlayer_name_input);

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SkillLevel selectedSkillLevel = skillLevels[spinner.getSelectedItemPosition()];

                playerService.createPlayer(new NewPlayerModel(name_input.getText().toString(), email, selectedSkillLevel.getId())).enqueue(new Callback<PlayerResponseWrapper>() {
                    @Override
                    public void onResponse(Call<PlayerResponseWrapper> call, Response<PlayerResponseWrapper> response) {
                        if(response.body() != null) {
                            login();
                        }
                    }

                    @Override
                    public void onFailure(Call<PlayerResponseWrapper> call, Throwable t) {
                        Log.d(TAG, "" + t.getMessage());                    }
                });
            }
        });

        name_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().length() > 0) {
                    confirm_btn.setEnabled(true);
                } else {
                    confirm_btn.setEnabled(false);
                }
            }
        });

    }

    private void login() {
        final String[] fbtoken = {null};

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(NewPlayerActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                fbtoken[0] = task.getResult();


                authService.login(new LoginModel(email, pass, fbtoken[0])).enqueue(new Callback<LoginResponseWrapper>() {
                    @Override
                    public void onResponse(Call<LoginResponseWrapper> call, Response<LoginResponseWrapper> response) {
                        if(response.body() != null){
                            LoginResponse loginResponse = response.body().result;
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putString(MainActivity.PREFS_TOKEN, loginResponse.token);
                            edit.putInt(MainActivity.PREFS_PLAYER_ID, loginResponse.user.id);
                            edit.putString(MainActivity.PREFS_PLAYER_EMAIL, loginResponse.user.email);
                            edit.apply();

                            startActivity(new Intent(NewPlayerActivity.this, CurrentGameActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponseWrapper> call, Throwable t) {
                        Log.d(TAG, "" + t.getMessage());                    }
                });
            }
        });

    }

    private void getSkillLevels() {
        skillLevelService.getAllSkillLevels().enqueue(new Callback<SkillLevelResponseWrapper>() {

            @Override
            public void onResponse(Call<SkillLevelResponseWrapper> call, Response<SkillLevelResponseWrapper> response) {
                if(response.body() != null){
                    skillLevels = response.body().getResult();

                    skillLevelNames = new ArrayList<>();
                    for(SkillLevel sl : response.body().getResult()) {
                        skillLevelNames.add(sl.getName());
                    }
                    ArrayAdapter skillLevelsAdapter = new ArrayAdapter(NewPlayerActivity.this, R.layout.support_simple_spinner_dropdown_item, skillLevelNames);
                    spinner.setAdapter(skillLevelsAdapter);
                }
            }

            @Override
            public void onFailure(Call<SkillLevelResponseWrapper> call, Throwable t) {
                Toast.makeText(NewPlayerActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}